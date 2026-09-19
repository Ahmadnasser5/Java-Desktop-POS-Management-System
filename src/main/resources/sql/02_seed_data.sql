-- =====================================================================
--  Seed data for the Authentication & Users module
--  Run AFTER 01_auth_schema.sql
-- =====================================================================
USE pos_db;

-- ---------------------------------------------------------------------
-- Roles
-- ---------------------------------------------------------------------
INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'Full system access'),
    ('SALES', 'Point of sale operator')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ---------------------------------------------------------------------
-- Permissions
--   Codes are shared with the other four modules. Keep them in sync with
--   com.pos.auth.security.Permissions
-- ---------------------------------------------------------------------
INSERT INTO permissions (code, description) VALUES
    ('USER_VIEW',       'View the user list'),
    ('USER_MANAGE',     'Create, edit, activate and deactivate users'),
    ('ROLE_MANAGE',     'Manage roles and their permissions'),
    ('PRODUCT_VIEW',    'View products and inventory'),
    ('PRODUCT_MANAGE',  'Create and edit products, categories and stock'),
    ('SALE_CREATE',     'Open the POS screen and complete a sale'),
    ('SALE_CANCEL',     'Cancel a completed sale'),
    ('PAYMENT_PROCESS', 'Process payments and issue invoices'),
    ('REPORT_VIEW',     'View reports and export them'),
    ('DASHBOARD_VIEW',  'View the dashboard')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ---------------------------------------------------------------------
-- ADMIN -> every permission
-- ---------------------------------------------------------------------
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

-- ---------------------------------------------------------------------
-- SALES -> operational permissions only
-- ---------------------------------------------------------------------
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('PRODUCT_VIEW', 'SALE_CREATE', 'PAYMENT_PROCESS')
WHERE r.name = 'SALES';

-- ---------------------------------------------------------------------
-- Bootstrap accounts
--   admin / Admin@123     (BCrypt, cost 12)
--   cashier / Sales@123   (BCrypt, cost 12)
--
--   Both are flagged must_change_password = TRUE, so the application
--   forces a new password at first login. These are DEVELOPMENT accounts.
--   Delete or change them before any real deployment.
-- ---------------------------------------------------------------------
INSERT INTO users (username, password_hash, full_name, role_id, active, must_change_password)
SELECT 'admin',
       '$2a$12$2w4GAtGUncQKm7t9JAjNeewPQuLwTGa/sUiJGdydnqh4v6UQbQK1a',
       'System Administrator',
       r.id, TRUE, TRUE
FROM roles r WHERE r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE username = username;

INSERT INTO users (username, password_hash, full_name, role_id, active, must_change_password)
SELECT 'cashier',
       '$2a$12$H6GqR8l52D19XKatwMcvLepUSVumKY0LbPYUzJaCkIks9qfjKF4V.',
       'Demo Cashier',
       r.id, TRUE, TRUE
FROM roles r WHERE r.name = 'SALES'
ON DUPLICATE KEY UPDATE username = username;
