-- ============================================
-- Products & Inventory Module - MySQL Schema
-- Column names match spec exactly.
-- ============================================

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    barcode VARCHAR(50) UNIQUE,
    price DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock_quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT NOT NULL DEFAULT 5,
    category_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- بيانات تجريبية (اختياري - احذفها في بيئة الإنتاج)
INSERT INTO categories (name) VALUES ('مشروبات'), ('وجبات خفيفة'), ('أدوات منزلية')
    ON DUPLICATE KEY UPDATE name = name;

INSERT INTO products (name, barcode, price, stock_quantity, min_stock_level, category_id) VALUES
    ('مياه معدنية 1.5 لتر', '6221031001', 10.00, 50, 10, 1),
    ('شيبسي بطاطس 150 جم',  '6221031002', 25.00, 3,  5,  2),
    ('سائل تنظيف الأطباق 500 مل', '6221031003', 40.00, 8,  5,  3)
    ON DUPLICATE KEY UPDATE name = VALUES(name);
