-- VimSpace Superstore POS — MSSQL schema (MVP)
-- Run against the vimspace_db database.
--
-- The column types below mirror the JPA entities in
-- backend-api/src/main/java/com/vimspace/api/entity exactly. Hibernate is the
-- source of truth for the mapping, so any change to an entity must be
-- reflected here. MigrationSchemaCompatibilityTest enforces that.
--
-- Note on identifiers: `terminal_id` / `cashier_id` hold the *business* codes
-- (e.g. 'POS-01', 'cashier') rather than surrogate keys, so they are stored as
-- VARCHAR and are not foreign keys. The `terminals` and `users` tables are
-- registries of those codes, not join targets for the sales ledger.
--
-- V2 (till session reconciliation) is applied by V2__till_session_reconciliation.sql.

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'vimspace_db')
    CREATE DATABASE vimspace_db;
GO

USE vimspace_db;
GO

CREATE TABLE users (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE terminals (
    terminal_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    terminal_code VARCHAR(20) NOT NULL UNIQUE,
    printer_name VARCHAR(100),
    is_active BIT NOT NULL DEFAULT 1
);
GO

CREATE TABLE products (
    product_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    unit VARCHAR(20) DEFAULT 'pcs',
    size VARCHAR(50),
    color VARCHAR(50),
    cost_price DECIMAL(18,4) NOT NULL DEFAULT 0,
    selling_price DECIMAL(18,4) NOT NULL DEFAULT 0,
    tax_setting VARCHAR(20) DEFAULT 'inclusive',
    is_active BIT NOT NULL DEFAULT 1,
    stock_on_hand DECIMAL(18,4) NOT NULL DEFAULT 0
);
GO

CREATE TABLE barcodes (
    barcode_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(product_id),
    barcode VARCHAR(100) NOT NULL UNIQUE
);
GO

-- till_sessions.expected_cash and till_sessions.notes are added by
-- V2__till_session_reconciliation.sql.
CREATE TABLE till_sessions (
    session_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    terminal_id VARCHAR(30) NOT NULL,
    cashier_id VARCHAR(50) NOT NULL,
    opened_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    closed_at DATETIME2,
    opening_float DECIMAL(18,4) NOT NULL DEFAULT 0,
    counted_cash DECIMAL(18,4) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'open'
);
GO

CREATE TABLE sales (
    sale_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    terminal_id VARCHAR(50),
    cashier_id VARCHAR(50),
    session_id BIGINT REFERENCES till_sessions(session_id),
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    total DECIMAL(18,4) NOT NULL DEFAULT 0,
    tax DECIMAL(18,4) NOT NULL DEFAULT 0,
    discount DECIMAL(18,4) NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    status VARCHAR(20) NOT NULL DEFAULT 'completed'
);
GO

CREATE TABLE sale_lines (
    line_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sale_id BIGINT NOT NULL REFERENCES sales(sale_id),
    product_id BIGINT NOT NULL REFERENCES products(product_id),
    quantity DECIMAL(18,4) NOT NULL,
    unit_price DECIMAL(18,4) NOT NULL,
    line_total DECIMAL(18,4) NOT NULL
);
GO

CREATE TABLE payments (
    payment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sale_id BIGINT NOT NULL REFERENCES sales(sale_id),
    method VARCHAR(30) NOT NULL,
    amount DECIMAL(18,4) NOT NULL
);
GO

CREATE TABLE stock_movements (
    movement_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(product_id),
    type VARCHAR(30) NOT NULL, -- sale, return, receipt, adjustment
    reference_id VARCHAR(100),
    quantity_delta DECIMAL(18,4) NOT NULL,
    reason VARCHAR(200),
    created_by VARCHAR(50),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE suppliers (
    supplier_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(100),
    address VARCHAR(500),
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE purchase_orders (
    po_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL REFERENCES suppliers(supplier_id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, ORDERED, RECEIVED, CANCELLED
    ordered_date DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    expected_date DATETIME2,
    received_date DATETIME2,
    cancelled_date DATETIME2,
    total_amount DECIMAL(18,4)
);
GO

CREATE TABLE purchase_order_items (
    poi_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    po_id BIGINT NOT NULL REFERENCES purchase_orders(po_id),
    sku VARCHAR(50) NOT NULL,
    product_name VARCHAR(200),
    quantity_ordered INT NOT NULL,
    quantity_received INT NOT NULL DEFAULT 0,
    unit_cost DECIMAL(18,4) NOT NULL
);
GO

CREATE TABLE audit_log (
    audit_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id),
    terminal_id BIGINT REFERENCES terminals(terminal_id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(100),
    details VARCHAR(1000),
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
