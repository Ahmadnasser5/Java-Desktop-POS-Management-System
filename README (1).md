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

```text
JavaFX
```

JavaFX is the selected desktop UI framework for this project.

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

The project is divided between **five engineers**, with each engineer owning a clearly defined module.

The team should agree on the database schema, shared models, naming conventions, Git workflow, and module interfaces before implementation to minimize integration conflicts.

---

## Engineer 1 — Authentication & Users

Responsible for the complete authentication and user-management module.

### Responsibilities

```text
Login
Logout
Authentication
Authorization
User Management
Role Management
Role Permissions
Session Management
Password Hashing
Activate / Deactivate Users
```

### Main Components

```text
User.java
Role.java
UserDAO.java
AuthService.java
UserService.java
LoginController.java
UserController.java
SessionManager.java
```

### Roles

```text
ADMIN
SALES
```

### Access Rules

```text
ADMIN → Full System Access

SALES → POS / Sales Operations Only
```

This module provides the authentication and authorization layer used by the rest of the application.

---

## Engineer 2 — Products & Inventory

Responsible for products, categories, and stock management.

### Responsibilities

```text
Product Management
Category Management
Product Search
Barcode Management
Stock Management
Stock Updates
Low Stock Alerts
```

### Main Components

```text
Product.java
Category.java
ProductDAO.java
CategoryDAO.java
ProductService.java
InventoryService.java
ProductController.java
CategoryController.java
```

### Product Operations

```text
Add Product
Edit Product
Delete Product
Search Product
View Product
Update Stock
```

### Integration

This module communicates with the POS/Sales module to provide product information and update stock after completed sales.

---

## Engineer 3 — POS & Sales

Responsible for the complete point-of-sale workflow.

### Responsibilities

```text
POS Screen
Product Search
Cart Management
Add Product
Remove Product
Update Quantity
Calculate Subtotal
Create Sale
Sale Items
Complete Sale
Cancel Sale
```

### Main Components

```text
Sale.java
SaleItem.java
SaleDAO.java
SaleItemDAO.java
SaleService.java
POSController.java
Cart.java
```

### Sales Workflow

```text
Search / Scan Product
        ↓
Add Product To Cart
        ↓
Set Quantity
        ↓
Calculate Total
        ↓
Confirm Sale
        ↓
Send Payment Information
        ↓
Complete Transaction
```

### Integration

This module depends on:

```text
Authentication → Current Sales User
Products → Product Information & Stock
Payments → Payment Processing
Invoices → Receipt / Invoice Generation
```

---

## Engineer 4 — Payments & Invoices

Responsible for payment processing and invoice/receipt generation.

### Responsibilities

```text
Payment Processing
Cash Payment
Card Payment
Discount
Tax
Paid Amount
Change Calculation
Invoice Generation
Receipt Generation
Receipt Printing
```

### Main Components

```text
Payment.java
PaymentDAO.java
PaymentService.java
InvoiceService.java
InvoiceUtil.java
ReceiptService.java
PaymentController.java
```

### Payment Information

```text
Subtotal
Discount
Tax
Total
Paid Amount
Change
Payment Method
```

### Supported Payment Methods

```text
Cash
Card / Visa
Other Agreed Methods
```

### Integration

This module receives the completed cart/sale information from the POS module and produces the final payment record and invoice/receipt.

---

## Engineer 5 — Dashboard & Reports

Responsible for the administrative dashboard, reports, and statistics.

### Responsibilities

```text
Admin Dashboard
Sales Reports
Daily Sales
Weekly Sales
Monthly Sales
Sales By User
Sales By Product
Top Selling Products
Total Revenue
Transaction Statistics
Low Stock Statistics
Search & Filtering
Date Range Filtering
Export Reports
```

### Main Components

```text
DashboardController.java
ReportController.java
ReportService.java
DashboardService.java
ReportDAO.java
```

### Dashboard Information

```text
Today's Sales
Total Revenue
Number Of Transactions
Products Sold
Low Stock Products
Top Selling Products
```

### Integration

This module mainly reads data from:

```text
Sales
Sale_Items
Products
Users
Payments
Inventory
```

The reporting module should not modify transactional data unless explicitly required by the agreed business requirements.

---

## 🔗 Module Integration

The five modules should communicate through clearly defined responsibilities.

```text
                    ┌─────────────────────┐
                    │ Authentication/User │
                    │     Engineer 1      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   Products/Stock    │
                    │     Engineer 2      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │      POS/Sales      │
                    │     Engineer 3      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Payments/Invoices   │
                    │     Engineer 4      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Dashboard/Reports   │
                    │     Engineer 5      │
                    └─────────────────────┘
```

### Important Integration Rules

1. Each engineer owns their assigned module.
2. Shared model classes must be agreed upon before implementation.
3. Database table names and column names must remain consistent.
4. Engineers should avoid directly modifying another engineer's module.
5. Cross-module changes should be discussed before implementation.
6. All database operations should use the DAO layer.
7. Business rules should remain inside the Service layer.
8. Controllers should mainly handle UI interaction and user input.

---

## 🌿 Git Branch Strategy

Each engineer should work on a dedicated feature branch.

```text
main
│
├── feature/auth-users
├── feature/products-inventory
├── feature/pos-sales
├── feature/payments-invoices
└── feature/dashboard-reports
```

### Development Workflow

```text
Create Branch
     ↓
Implement Feature
     ↓
Test Locally
     ↓
Commit Changes
     ↓
Push Branch
     ↓
Pull Request
     ↓
Code Review
     ↓
Merge Into Main
```

Do not develop directly on the `main` branch.

### Commit Example

```bash
git add .
git commit -m "Add user authentication and role permissions"
git push origin feature/auth-users
```

---

## 🧩 Ownership Summary

| Engineer | Module | Main Responsibility |
|---|---|---|
| Engineer 1 | Authentication & Users | Login, users, roles, permissions |
| Engineer 2 | Products & Inventory | Products, categories, stock |
| Engineer 3 | POS & Sales | Cart, sales transactions, POS |
| Engineer 4 | Payments & Invoices | Payments, invoices, receipts |
| Engineer 5 | Dashboard & Reports | Dashboard, reports, statistics |

The five engineers should coordinate regularly during integration, while maintaining clear ownership of their individual modules.

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

---

