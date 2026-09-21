IF DB_ID(N'pos_db') IS NULL
    BEGIN
        CREATE DATABASE pos_db;
    END


GO
USE pos_db;


GO
IF OBJECT_ID(N'dbo.roles', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.roles (
            id          INT            IDENTITY (1, 1) NOT NULL PRIMARY KEY,
            name        NVARCHAR (50)  NOT NULL UNIQUE,
            description NVARCHAR (255) NULL
        );
    END


GO
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.users (
            id                   INT            IDENTITY (1, 1) NOT NULL PRIMARY KEY,
            username             NVARCHAR (50)  NOT NULL UNIQUE,
            password_hash        NVARCHAR (100) NOT NULL,
            full_name            NVARCHAR (100) NULL,
            role_id              INT            NOT NULL,
            active               BIT            CONSTRAINT df_users_active DEFAULT 1 NOT NULL,
            must_change_password BIT            CONSTRAINT df_users_must_change DEFAULT 0 NOT NULL,
            last_login_at        DATETIME2      NULL,
            created_at           DATETIME2      CONSTRAINT df_users_created DEFAULT SYSDATETIME() NOT NULL,
            updated_at           DATETIME2      CONSTRAINT df_users_updated DEFAULT SYSDATETIME() NOT NULL,
            CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES dbo.roles (id)
        );
    END


GO
IF OBJECT_ID(N'dbo.permissions', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.permissions (
            id          INT            IDENTITY (1, 1) NOT NULL PRIMARY KEY,
            code        NVARCHAR (60)  NOT NULL UNIQUE,
            description NVARCHAR (255) NULL
        );
    END


GO
IF OBJECT_ID(N'dbo.role_permissions', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.role_permissions (
            role_id       INT NOT NULL,
            permission_id INT NOT NULL,
            CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
            CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES dbo.roles (id) ON DELETE CASCADE,
            CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES dbo.permissions (id) ON DELETE CASCADE
        );
    END


GO
IF OBJECT_ID(N'dbo.categories', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.categories (
            id   INT            IDENTITY (1, 1) NOT NULL PRIMARY KEY,
            name NVARCHAR (100) NOT NULL UNIQUE
        );
    END


GO
IF OBJECT_ID(N'dbo.products', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.products (
            id              INT             IDENTITY (1, 1) NOT NULL PRIMARY KEY,
            name            NVARCHAR (150)  NOT NULL,
            barcode         NVARCHAR (50)   NULL UNIQUE,
            price           DECIMAL (10, 2) CONSTRAINT df_products_price DEFAULT 0 NOT NULL,
            stock_quantity  INT             CONSTRAINT df_products_stock DEFAULT 0 NOT NULL,
            min_stock_level INT             CONSTRAINT df_products_min_stock DEFAULT 5 NOT NULL,
            category_id     INT             NULL,
            created_at      DATETIME2       CONSTRAINT df_products_created DEFAULT SYSDATETIME() NOT NULL,
            CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES dbo.categories (id)
        );
    END


GO
IF OBJECT_ID(N'dbo.sales', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.sales (
            id             INT             IDENTITY (1, 1) NOT NULL PRIMARY KEY,
            user_id        INT             NOT NULL,
            subtotal       DECIMAL (10, 2) NOT NULL,
            tax            DECIMAL (10, 2) CONSTRAINT df_sales_tax DEFAULT 0 NOT NULL,
            discount       DECIMAL (10, 2) CONSTRAINT df_sales_discount DEFAULT 0 NOT NULL,
            total          DECIMAL (10, 2) NOT NULL,
            payment_method NVARCHAR (10)   CONSTRAINT df_sales_payment DEFAULT N'CASH' NOT NULL,
            created_at     DATETIME2       CONSTRAINT df_sales_created DEFAULT SYSDATETIME() NOT NULL,
            CONSTRAINT fk_sales_user FOREIGN KEY (user_id) REFERENCES dbo.users (id)
        );
    END


GO
IF OBJECT_ID(N'dbo.sale_items', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.sale_items (
            id           INT             IDENTITY (1, 1) NOT NULL PRIMARY KEY,
            sale_id      INT             NOT NULL,
            product_id   INT             NOT NULL,
            product_name NVARCHAR (150)  NOT NULL,
            quantity     INT             NOT NULL,
            unit_price   DECIMAL (10, 2) NOT NULL,
            line_total   DECIMAL (10, 2) NOT NULL,
            CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES dbo.sales (id) ON DELETE CASCADE,
            CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id) REFERENCES dbo.products (id),
            CONSTRAINT chk_sale_items_quantity CHECK (quantity > 0)
        );
    END


GO
MERGE INTO dbo.roles
 AS target
USING (VALUES (N'ADMIN', N'Full system access'), (N'SALES', N'Point of sale operator')) AS source(name, description) ON target.name = source.name
WHEN MATCHED THEN UPDATE 
SET description = source.description
WHEN NOT MATCHED THEN INSERT (
    name,
    description
) VALUES (source.name, source.description);


GO
MERGE INTO dbo.permissions
 AS target
USING (VALUES (N'USER_VIEW', N'View the user list'), (N'USER_MANAGE', N'Manage users'), (N'ROLE_MANAGE', N'Manage roles'), (N'PRODUCT_VIEW', N'View products'), (N'PRODUCT_MANAGE', N'Manage products and stock'), (N'SALE_CREATE', N'Create sales'), (N'SALE_CANCEL', N'Cancel sales'), (N'PAYMENT_PROCESS', N'Process payments'), (N'REPORT_VIEW', N'View reports'), (N'DASHBOARD_VIEW', N'View dashboard')) AS source(code, description) ON target.code = source.code
WHEN MATCHED THEN UPDATE 
SET description = source.description
WHEN NOT MATCHED THEN INSERT (
    code,
    description
) VALUES (source.code, source.description);


GO
INSERT INTO dbo.role_permissions (
    role_id,
    permission_id
)
SELECT r.id,
       p.id
FROM   dbo.roles AS r CROSS JOIN dbo.permissions AS p
WHERE  r.name = N'ADMIN'
       AND NOT EXISTS (SELECT 1
                       FROM   dbo.role_permissions AS rp
                       WHERE  rp.role_id = r.id
                              AND rp.permission_id = p.id);

INSERT INTO dbo.role_permissions (
    role_id,
    permission_id
)
SELECT r.id,
       p.id
FROM   dbo.roles AS r
       INNER JOIN
       dbo.permissions AS p
       ON p.code IN (N'PRODUCT_VIEW', N'SALE_CREATE', N'PAYMENT_PROCESS')
WHERE  r.name = N'SALES'
       AND NOT EXISTS (SELECT 1
                       FROM   dbo.role_permissions AS rp
                       WHERE  rp.role_id = r.id
                              AND rp.permission_id = p.id);


GO
INSERT INTO dbo.users (
    username,
    password_hash,
    full_name,
    role_id,
    active,
    must_change_password
)
SELECT N'admin',
       N'$2a$12$2w4GAtGUncQKm7t9JAjNeewPQuLwTGa/sUiJGdydnqh4v6UQbQK1a',
       N'System Administrator',
       r.id,
       1,
       1
FROM   dbo.roles AS r
WHERE  r.name = N'ADMIN'
       AND NOT EXISTS (SELECT 1
                       FROM   dbo.users
                       WHERE  username = N'admin');

INSERT INTO dbo.users (
    username,
    password_hash,
    full_name,
    role_id,
    active,
    must_change_password
)
SELECT N'cashier',
       N'$2a$12$H6GqR8l52D19XKatwMcvLepUSVumKY0LbPYUzJaCkIks9qfjKF4V.',
       N'Demo Cashier',
       r.id,
       1,
       1
FROM   dbo.roles AS r
WHERE  r.name = N'SALES'
       AND NOT EXISTS (SELECT 1
                       FROM   dbo.users
                       WHERE  username = N'cashier');