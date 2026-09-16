# 🛒 Java Desktop POS & Sales Management System

A desktop-based Point of Sale (POS) and Sales Management System developed using **Java**.

The system is designed to help stores manage products, users, inventory, sales transactions, invoices, and sales reports through a simple and secure desktop application.

---

## 📌 Project Overview

The application provides two main types of users:

### 👨‍💼 Admin User

The Admin has full access to the system, including:

* Manage users
* Create Sales Users
* Edit users
* Delete users
* Manage products
* Add products
* Edit products
* Delete products
* Manage categories
* Monitor inventory
* View all sales
* View sales reports
* Manage system settings

### 💰 Sales User

The Sales User is responsible for selling products.

Permissions include:

* Login
* Search products
* Add products to cart
* Update quantities
* Remove products from cart
* Calculate total price
* Process sales
* Generate invoices
* Print receipts
* View their own sales

The Sales User **cannot**:

* Manage users
* Add or delete users
* Modify system settings
* Delete products
* Manage inventory
* Access administrative reports

---

# 🎯 Main Objectives

The main objectives of the project are:

* Simplify the sales process.
* Reduce manual errors.
* Manage products and stock efficiently.
* Provide role-based access control.
* Keep a complete record of sales.
* Generate invoices and receipts.
* Provide useful sales reports.
* Build a reliable desktop POS application.

---

# 🧩 Main Modules

## 1. Authentication

Users must log in before accessing the system.

Features:

* Username
* Password
* Role-based authentication
* Session management
* Logout

Example roles:

```text
ADMIN
SALES
```

---

## 2. User Management

Available to Admin only.

Admin can:

```text
Create User
Update User
Delete User
View Users
Change User Role
Activate / Deactivate User
```

---

## 3. Product Management

Admin can manage all products.

Each product contains information such as:

```text
Product ID
Product Name
Category
Price
Quantity
Minimum Stock
Barcode
Status
Created Date
```

Operations:

```text
Add Product
Edit Product
Delete Product
Search Product
View Product
Update Stock
```

---

## 4. Category Management

Admin can manage product categories.

Example:

```text
Electronics
Food
Drinks
Clothes
Accessories
Other
```

Operations:

```text
Add Category
Edit Category
Delete Category
View Categories
```

---

# 🛒 5. POS / Sales Module

The Sales User uses the POS screen to create a new sale.

Typical workflow:

```text
Login
   ↓
Open POS
   ↓
Search / Scan Product
   ↓
Add Product To Cart
   ↓
Set Quantity
   ↓
Calculate Total
   ↓
Select Payment Method
   ↓
Confirm Sale
   ↓
Generate Invoice
   ↓
Print Receipt
```

---

# 💵 Payment Methods

The system can support:

```text
Cash
Visa / Card
Other
```

The sale should store:

```text
Subtotal
Discount
Tax
Total
Paid Amount
Change
Payment Method
```

---

# 📦 6. Inventory Management

The system automatically updates stock after a successful sale.

Example:

```text
Before Sale:
Product Quantity = 20

Sold:
Quantity = 3

After Sale:
Product Quantity = 17
```

The system can also display low-stock products.

Example:

```text
Product: Coca Cola
Current Stock: 4
Minimum Stock: 10

Status: LOW STOCK
```

---

# 🧾 7. Invoice / Receipt

After completing a sale, the system generates an invoice containing:

```text
Invoice Number
Date
Cashier
Products
Quantity
Unit Price
Subtotal
Discount
Tax
Total
Payment Method
Paid Amount
Change
```

Example:

```text
================================
        STORE NAME
================================

Invoice: INV-000123
Date: 16/09/2026
Cashier: Ahmed

Product       Qty    Price
--------------------------------
Product A      2      100
Product B      1       50

--------------------------------
Subtotal             250
Discount              10
Tax                   12
--------------------------------
TOTAL                252

Paid                 300
Change                48

================================
        THANK YOU!
================================
```

---

# 📊 8. Sales Reports

Admin can view sales reports.

Possible reports:

```text
Daily Sales
Weekly Sales
Monthly Sales
Sales By User
Sales By Product
Top Selling Products
Total Revenue
Number Of Transactions
```

Example dashboard:

```text
Today's Sales       $4,250

Transactions             87

Products Sold           312

Low Stock Products        8
```

---

# 🗄️ Database

The application uses a relational database to store system data.

Main entities:

```text
Users
Products
Categories
Sales
Sale_Items
Payments
```

### Database Relationship

```text
Users
  │
  └────────── Sales
                │
                └────── Sale_Items
                           │
                           └────── Products
                                      │
                                      └──── Categories
```

---

# 🏗️ Architecture

The project follows a layered architecture.

```text
┌──────────────────────────┐
│          View            │
│      JavaFX / Swing      │
└────────────┬─────────────┘
             │
┌────────────▼─────────────┐
│       Controller         │
└────────────┬─────────────┘
             │
┌────────────▼─────────────┐
│         Service          │
│      Business Logic      │
└────────────┬─────────────┘
             │
┌────────────▼─────────────┐
│           DAO            │
│      Database Access     │
└────────────┬─────────────┘
             │
┌────────────▼─────────────┐
│        Database          │
└──────────────────────────┘
```

This keeps the UI, business logic, and database operations separated.

---

# 💻 Technology Stack

## Programming Language

```text
Java
```

## Desktop UI

Recommended:

```text
JavaFX
```

Alternative:

```text
Java Swing
```

JavaFX is a good fit if you want a modern desktop interface; existing Java POS projects also use JavaFX + JDBC + MySQL for this type of system.

## Database

```text
MySQL
```

## Database Connectivity

```text
JDBC
```

## Build Tool

```text
Maven
```

## Version Control

```text
Git
GitHub
```

---

# 📁 Project Structure

Recommended structure:

```text
src/
│
├── main/
│   │
│   ├── java/
│   │   │
│   │   └── com/pos/
│   │       │
│   │       ├── Main.java
│   │       │
│   │       ├── controller/
│   │       │   ├── LoginController.java
│   │       │   ├── AdminController.java
│   │       │   ├── SalesController.java
│   │       │   ├── ProductController.java
│   │       │   └── SaleController.java
│   │       │
│   │       ├── model/
│   │       │   ├── User.java
│   │       │   ├── Product.java
│   │       │   ├── Category.java
│   │       │   ├── Sale.java
│   │       │   └── SaleItem.java
│   │       │
│   │       ├── dao/
│   │       │   ├── UserDAO.java
│   │       │   ├── ProductDAO.java
│   │       │   ├── CategoryDAO.java
│   │       │   └── SaleDAO.java
│   │       │
│   │       ├── service/
│   │       │   ├── AuthService.java
│   │       │   ├── ProductService.java
│   │       │   ├── SaleService.java
│   │       │   └── ReportService.java
│   │       │
│   │       ├── database/
│   │       │   └── DatabaseConnection.java
│   │       │
│   │       └── util/
│   │           ├── ValidationUtil.java
│   │           ├── InvoiceUtil.java
│   │           └── SessionManager.java
│   │
│   └── resources/
│       │
│       ├── fxml/
│       ├── css/
│       ├── images/
│       └── icons/
│
└── pom.xml
```

---

# 👥 Team Development

For a team of four developers, the project can be divided into:

### Developer 1 — Authentication & Users

Responsible for:

```text
Login
Logout
Authentication
Authorization
User Management
Role Permissions
```

### Developer 2 — Products & Inventory

Responsible for:

```text
Products
Categories
Stock
Inventory
Search
Low Stock Alerts
```

### Developer 3 — POS & Sales

Responsible for:

```text
POS Screen
Cart
Sales
Payments
Invoice
Receipt
```

### Developer 4 — Reports & Integration

Responsible for:

```text
Dashboard
Reports
Statistics
Database Integration
Testing
Final Integration
```

All four developers should agree first on the **database schema and Java model classes** so that their modules integrate without conflicts.

---

# 🔐 Security

The system should implement:

* Password hashing
* Prepared Statements
* Role-based authorization
* Input validation
* Session management
* Database constraints

Users should only have access to the features allowed by their role.

---

# 🧪 Testing

The project should include testing for:

### Authentication

```text
Valid Login
Invalid Password
Invalid Username
Inactive User
Wrong Role
Logout
```

### Products

```text
Add Product
Update Product
Delete Product
Invalid Price
Invalid Quantity
Duplicate Product
```

### Sales

```text
Add Product To Cart
Remove Product
Update Quantity
Insufficient Stock
Calculate Total
Complete Sale
Cancel Sale
```

### Permissions

```text
Admin → Full Access

Sales User → Sales Only
```

---

# 🚀 Installation

## Requirements

Install:

```text
JDK
IntelliJ IDEA / NetBeans / Eclipse
Maven
MySQL Server
Git
```

---

## Clone Repository

```bash
git clone <repository-url>
```

Enter the project:

```bash
cd java-pos-system
```

---

## Database Setup

Create the database:

```sql
CREATE DATABASE pos_system;
```

Then execute the provided SQL schema.

Configure the database connection:

```text
Host: localhost
Port: 3306
Database: pos_system
Username: root
Password: ********
```

---

# ▶️ Running the Application

Using Maven:

```bash
mvn clean install
```

Then:

```bash
mvn javafx:run
```

Or run the `Main.java` class directly from your IDE.

---

# 📌 Future Improvements

Possible future features:

* Barcode scanner support
* Thermal receipt printer
* Cash drawer integration
* Product image management
* Customer management
* Discounts
* Taxes
* Returns and refunds
* Backup and restore
* Multiple branches
* Multi-language support
* Dark mode
* Export reports to PDF / Excel

JavaPOS-compatible hardware can also be integrated with Java desktop POS systems for peripherals such as scanners and receipt printers.

---

