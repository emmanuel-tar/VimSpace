-- VimSpace Superstore POS — till session reconciliation support (MSSQL)
-- Adds the columns the till-session service needs to persist expected cash
-- and shift notes, plus indexes used by the active-session lookups.
-- Run after V1__init_schema.sql against the vimspace_db database.
--
-- The column types here must stay in step with TillSessionEntity
-- (expected_cash: DECIMAL(18,4), notes: VARCHAR(500)); the
-- MigrationSchemaCompatibilityTest enforces that.

USE vimspace_db;
GO

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('till_sessions') AND name = 'expected_cash')
    ALTER TABLE till_sessions ADD expected_cash DECIMAL(18,4) NULL DEFAULT 0;
GO

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('till_sessions') AND name = 'notes')
    ALTER TABLE till_sessions ADD notes VARCHAR(500) NULL;
GO

-- One open session per terminal: enforce at the database level too.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_till_sessions_terminal_status' AND object_id = OBJECT_ID('till_sessions'))
    CREATE INDEX ix_till_sessions_terminal_status ON till_sessions (terminal_id, status);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_sales_session_id' AND object_id = OBJECT_ID('sales'))
    CREATE INDEX ix_sales_session_id ON sales (session_id);
GO
