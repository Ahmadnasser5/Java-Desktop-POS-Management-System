# POS & Sales Management System

Java desktop point of sale, built by a team of five.
This repository currently contains the shared application shell and the
**Authentication & Users** module (Engineer 1).

## Stack

Java 17 · JavaFX 21 · FXML · Maven · MySQL 8 · JDBC · BCrypt

## Getting started

### 1. Database

```bash
mysql -u root -p < src/main/resources/sql/01_auth_schema.sql
mysql -u root -p < src/main/resources/sql/02_seed_data.sql
```

### 2. Configuration

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
# then edit db.properties
```

`db.properties` is git-ignored. Environment variables `POS_DB_URL`,
`POS_DB_USER`, `POS_DB_PASSWORD` override it.

### 3. Run

```bash
mvn clean javafx:run
```

### 4. Development accounts

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@123` | ADMIN |
| `cashier` | `Sales@123` | SALES |

Both are flagged `must_change_password`, so the application asks for a new
password at first sign in. **Delete them before any real deployment.**

## Tests

```bash
mvn test
```

Covers password hashing, login success and failure, unknown user, inactive
user, session creation and logout, role detection, authorization, and the
user management business rules.

## Project layout

```text
src/main/java/
├── com/pos/
│   ├── Main.java                      application entry point
│   ├── core/db/                       ConnectionFactory, DatabaseConfig   [shared]
│   ├── core/util/                     Navigator, AlertUtil, ValidationUtil [shared]
│   ├── core/exception/                DataAccessException                 [shared]
│   └── auth/                          model · dao · service · security · controller
└── ui/
    ├── components/                    AppButton, AppTextField, ...        [shared]
    ├── layout/                        TopBar, Sidebar, StatusBar, MainLayout [shared]
    └── theme/                         ThemeManager                        [shared]

src/main/resources/
├── css/                               variables.css, style.css            [shared]
├── fxml/layout/                       the standard application frame      [shared]
├── fxml/auth/                         login
├── fxml/users/                        user management
└── sql/                               schema and seed data
```

Anything marked `[shared]` follows the Git governance rule in `uiContract.md`:
changes need team approval before merging.

## For the other four engineers

Read `INTEGRATION.md`. It has the exact calls for the current user, permission
checks, the database connection, foreign keys to `users`, and the one-line
change that puts your screen in the sidebar.

## Architecture

```text
FXML / View  →  Controller  →  Service  →  DAO  →  JDBC  →  MySQL
```

Controllers hold no SQL. Services hold no JavaFX. DAOs hold no business rules.
