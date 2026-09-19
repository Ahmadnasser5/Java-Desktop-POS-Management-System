-- =====================================================================
--  POS & Sales Management System
--  Module : Authentication & Users  (Engineer 1)
--  File   : 01_auth_schema.sql
--  Engine : MySQL 8.x
-- =====================================================================
--  Run order:  01_auth_schema.sql  ->  02_seed_data.sql
--
--  NOTE FOR THE TEAM
--  -----------------
--  users.id is INT and will stay INT. Other modules should reference it:
--      sales.user_id            -> users(id)   (Engineer 3)
--      payments.created_by      -> users(id)   (Engineer 4)
--      stock_movements.user_id  -> users(id)   (Engineer 2)
--  Always use ON DELETE RESTRICT. Users are NEVER hard-deleted while they
--  own historical records - they are deactivated (active = FALSE).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS pos_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE pos_db;

-- ---------------------------------------------------------------------
-- roles
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NULL,
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    username             VARCHAR(50)  NOT NULL,
    password_hash        VARCHAR(100) NOT NULL,   -- BCrypt = 60 chars, 100 leaves room for Argon2 later
    full_name            VARCHAR(100) NULL,
    role_id              INT          NOT NULL,
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    must_change_password BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at        TIMESTAMP    NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_users_role   (role_id),
    INDEX idx_users_active (active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- permissions
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS permissions (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(60)  NOT NULL,   -- e.g. 'USER_MANAGE', 'SALE_CREATE'
    description VARCHAR(255) NULL,
    CONSTRAINT uq_permissions_code UNIQUE (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- role_permissions  (many-to-many)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id)
        REFERENCES permissions (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Least-privilege application account (run as root once, then use pos_app)
-- ---------------------------------------------------------------------
-- CREATE USER IF NOT EXISTS 'pos_app'@'localhost' IDENTIFIED BY 'strong_password_here';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON pos_db.* TO 'pos_app'@'localhost';
-- FLUSH PRIVILEGES;
