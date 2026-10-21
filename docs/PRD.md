# VimSpace Superstore POS — Product Requirements & MVP Architecture

**Product name:** VimSpace Superstore POS  
**Applications:** FrontRetail (FOH) and Vim Manager (BOH)  
**Technology baseline:** Java, Microsoft SQL Server (MSSQL)  
**Document status:** MVP proposal  
**Version:** 1.0  
**Date:** 19 October 2026

---

## 1. Executive Summary

VimSpace Superstore POS is a retail operations system made up of two separate Java applications that work together through a shared backend:

1. **FrontRetail (FOH — Front of House):** The cashier-facing point-of-sale application used to scan items, build a basket, take payments, print receipts, manage till sessions, and process approved returns.
2. **Vim Manager (BOH — Back of House):** The management application used to maintain products, barcodes, prices, stock, suppliers, purchasing, users, permissions, reports, and store settings.

Both applications use the same business rules and data through a central Java backend API. MSSQL is the system of record. The two applications should **not connect directly to MSSQL**; they communicate with the backend over a private, authenticated network. This avoids duplicated business logic and makes access control, validation, auditing, and future integrations easier to maintain.

The MVP should prioritise reliable checkout, accurate inventory movements, secure user access, basic purchasing, and daily sales reconciliation. Advanced features should be deferred until the core workflow is stable.

## 2. Product Vision and Goals

### Vision

Provide a reliable, maintainable, and extensible POS platform for a superstore, with fast checkout at the front and controlled retail operations at the back.

### MVP goals

- Complete a normal sale quickly using barcode scanning or product search.
- Support cash, POS/card, bank transfer, and split tender where required by store policy.
- Keep sales, payments, and stock movements consistent.
- Allow managers to maintain products, prices, categories, users, suppliers, and stock.
- Record receiving, stock adjustments, and approved returns.
- Provide daily sales, cashier, payment, low-stock, and stock-on-hand reports.
- Keep an audit trail for sensitive operations.
- Support one store and multiple checkout terminals from a central server.
- Make future multi-branch support possible without requiring a rewrite.

### Success measures

- A cashier can complete a standard barcode sale without needing BOH access.
- Every completed sale has a unique receipt number, cashier, terminal, timestamp, line items, and payment records.
- Stock changes can be traced to a sale, return, goods receipt, or adjustment.
- A manager can reconcile sales by cashier and payment method for a business day.
- Users cannot perform actions outside their assigned permissions.
- Failed or interrupted transactions do not create duplicate completed sales.

## 3. Scope

### 3.1 In scope for MVP

**FrontRetail**
- Login and role-based access.
- Select/open/close a till session.
- Barcode scan and product search.
- Basket management: quantity changes, remove line, void basket.
- Price and stock display.
- Tax-inclusive/exclusive configuration as agreed with the business.
- Configurable line-level or basket-level discounts with permission controls.
- Cash, card/POS, bank transfer, and optional split payments.
- Receipt printing and receipt reprint with audit logging.
- Hold and resume a basket, if needed by the checkout workflow.
- Supervisor-authorised voids, discounts, and returns.
- Basic sales return/refund workflow linked to the original sale where possible.
- Clear error handling for unavailable backend, printer failure, and payment recording failures.

**Vim Manager**
- User accounts, roles, and permissions.
- Store and terminal configuration.
- Product, category, unit, barcode, and price management.
- Supplier management.
- Purchase order creation and basic receiving/goods receipt.
- Stock-on-hand view and stock adjustment with reason and approval.
- Sales and transaction lookup.
- Returns lookup and approval.
- Daily sales, payment summary, cashier/till, product sales, and low-stock reports.
- CSV export for core reports.
- Audit log for high-risk actions.

**Platform**
- Central Java backend API.
- MSSQL relational database.
- Shared business validation and transaction processing.
- Database backup and restore procedure.
- Application logging and basic health checks.
- Deployment to a store server or managed Windows/Linux server reachable by checkout PCs.

### 3.2 Explicitly out of scope for the first MVP

- Customer loyalty points and advanced CRM.
- Promotions engine, mix-and-match, coupons, and complex price rules.
- E-commerce and delivery integrations.
- Accounting/ERP integration.
- Automated supplier EDI.
- AI forecasting and replenishment.
- Mobile stocktaking application.
- Advanced multi-branch replication and offline synchronisation.
- Full employee payroll and HR.
- Complex approval workflows and configurable workflow designer.
- Customer-facing self-checkout.
- Native integration to every bank/POS terminal. In the MVP, card/POS and transfer payments may be recorded by the cashier after external confirmation unless a payment provider integration is separately approved.

## 4. Users and Permissions

| Role | Main responsibilities | Example permissions |
|---|---|---|
| Cashier | Process sales and payments | Sell items, hold/resume basket, print receipt, view own till totals |
| Supervisor | Support checkout and approve exceptions | Approve discounts, voids, returns, reprints, and till corrections |
| Store Manager | Run store operations | Manage stock, receive goods, view reports, approve adjustments |
| Inventory/Purchasing Officer | Maintain stock and purchasing | Maintain suppliers, create purchase orders, receive stock |
| System Administrator | Configure and maintain system | Manage users, roles, terminals, store settings, backups/configuration |
| Auditor/Read-only Manager | Review activity | View sales, stock movements, reports, and audit logs |

Permissions should be granular and enforced by the backend, not just hidden in the user interface. Shared accounts are prohibited. Sensitive actions must record the acting user, timestamp, reason, and relevant entity.

## 5. Key User Journeys

### 5.1 Checkout sale
1. Cashier signs in to FrontRetail.
2. Cashier opens a till session and records opening float if enabled.
3. Cashier scans a barcode or searches for an item.
4. System validates the item, current selling price, and sale rules.
5. Cashier adjusts quantities or applies an authorised discount.
6. Cashier selects payment method and enters amount tendered where relevant.
7. Backend validates and commits the sale, payment records, and stock movements in one database transaction.
8. FrontRetail displays success and prints the receipt.
9. If printing fails after the sale commits, the cashier can reprint the existing receipt; the sale must not be submitted again.

### 5.2 Product setup
1. Authorised BOH user creates a product with SKU, name, category, unit, selling price, tax treatment, and one or more barcodes.
2. Backend validates uniqueness and required fields.
3. Product becomes available to FrontRetail after the next API request; no manual database refresh is required.

### 5.3 Goods receiving
1. Purchasing officer creates a purchase order or records an approved direct receipt.
2. User records quantities received and supplier document details.
3. Backend posts a goods receipt and corresponding stock movements.
4. Stock-on-hand and receiving reports update immediately.

### 5.4 Stock adjustment
1. Authorised user selects product and location.
2. User enters counted quantity or adjustment quantity and mandatory reason.
3. If approval is required, a manager approves the adjustment.
4. Backend records the movement and audit entry; users cannot directly overwrite stock-on-hand.

### 5.5 End-of-day reconciliation
1. Cashier closes the till session and records counted cash.
2. System calculates expected totals by payment type.
3. Supervisor reviews variances and records a reason.
4. Manager reviews sales totals, refunds, voids, discounts, and till variances.

## 6. Functional Requirements

Priority definitions: **P0 = required for MVP**, **P1 = important next increment**, **P2 = later enhancement**.

### 6.1 Identity and access
- **FR-001 (P0):** Authenticate users with individual accounts.
- **FR-002 (P0):** Enforce role/permission checks on every protected backend operation.
- **FR-003 (P0):** Record user, terminal, and timestamp for sales and sensitive changes.
- **FR-004 (P0):** Allow administrators to deactivate accounts and reset access.
- **FR-005 (P1):** Support password policy configuration and optional directory/SSO integration.

### 6.2 Product catalogue
- **FR-010 (P0):** Create, view, update, and deactivate products.
- **FR-011 (P0):** Support SKU/item code, name, category, unit, tax setting, cost price, selling price, and active status.
- **FR-012 (P0):** Support multiple barcodes per product and reject duplicate active barcodes.
- **FR-013 (P0):** Search by barcode, SKU, product name, and category.
- **FR-014 (P0):** Support decimal quantities only for configured weighted/measured items.
- **FR-015 (P1):** Import/export products using validated CSV templates and return row-level errors.
- **FR-016 (P1):** Maintain price-change history with user and timestamp.

### 6.3 Checkout and sales
- **FR-020 (P0):** Scan/search products and build a basket.
- **FR-021 (P0):** Validate price and item status on the server before completing the sale.
- **FR-022 (P0):** Support configured tax and discount calculations with explicit rounding rules.
- **FR-023 (P0):** Support cash, card/POS, bank transfer, and configurable payment methods.
- **FR-024 (P0):** Store multiple payment lines when split tender is enabled.
- **FR-025 (P0):** Generate unique, human-readable receipt numbers without relying on the client clock.
- **FR-026 (P0):** Commit sale header, sale lines, payments, stock movements, and audit data atomically.
- **FR-027 (P0):** Prevent accidental duplicate sale submission using a unique idempotency/request key.
- **FR-028 (P0):** Print and reprint receipts; reprints must be marked and logged.
- **FR-029 (P0):** Support supervisor-approved line void, basket void, and discount above cashier limit.
- **FR-030 (P0):** Support sale lookup and return against the original sale.
- **FR-031 (P1):** Hold and resume baskets across a cashier's session.
- **FR-032 (P1):** Configure receipt header/footer, store address, tax ID, and contact information.

### 6.4 Till and cash management
- **FR-040 (P0):** Open and close till sessions per cashier/terminal.
- **FR-041 (P0):** Record opening float and counted closing cash where enabled.
- **FR-042 (P0):** Calculate expected totals by payment method and show variances.
- **FR-043 (P0):** Restrict till adjustments and record reason plus approver.
- **FR-044 (P1):** Support paid-in/paid-out cash movements with authorisation.

### 6.5 Inventory and purchasing
- **FR-050 (P0):** Show stock-on-hand by product and stock location.
- **FR-051 (P0):** Create and maintain suppliers.
- **FR-052 (P0):** Create purchase orders and record goods received.
- **FR-053 (P0):** Record all stock changes in an append-only stock movement ledger.
- **FR-054 (P0):** Support adjustment reasons such as count correction, damage, expiry, and opening balance.
- **FR-055 (P0):** Configure whether negative stock is blocked or permitted; default to blocked.
- **FR-056 (P0):** Return eligible sale items to stock only when the return disposition indicates resellable.
- **FR-057 (P1):** Low-stock thresholds and replenishment suggestions.
- **FR-058 (P1):** Stock counts and variance posting workflow.
- **FR-059 (P2):** Inter-store transfers and central warehouse operations.

### 6.6 Reports
- **FR-060 (P0):** Sales by date/time, cashier, terminal, and payment method.
- **FR-061 (P0):** Daily sales summary with gross sales, discounts, tax, returns, and net sales clearly defined.
- **FR-062 (P0):** Product sales and quantity sold.
- **FR-063 (P0):** Current stock-on-hand and low-stock list.
- **FR-064 (P0):** Till closure and cash variance report.
- **FR-065 (P0):** Audit report for voids, discounts, returns, reprints, and stock adjustments.
- **FR-066 (P1):** CSV export for reports and date-range filters.

### 6.7 Configuration
- **FR-070 (P0):** Configure store identity, currency, tax rates, receipt settings, and payment types.
- **FR-071 (P0):** Configure terminals and printer assignment.
- **FR-072 (P0):** Configure rounding and price precision consistently across backend, database, and receipt.
- **FR-073 (P1):** Support multiple branches using a store/location identifier in transactional data.

## 7. Non-Functional Requirements

- **Performance:** Barcode lookup should normally complete within 300 ms on the store LAN under expected MVP load; sale completion should normally complete within 2 seconds excluding external payment and printer time.
- **Reliability:** A committed sale must not be lost because receipt printing fails. A retried request must not create a duplicate sale.
- **Consistency:** Sale posting, payments, and stock movements must be committed atomically in MSSQL.
- **Security:** TLS for API traffic where supported by the deployment; password hashes using a modern password hashing algorithm; secrets kept outside source control; least-privilege database account; parameterised queries/ORM; input validation; audit logging.
- **Availability:** Target 99.5% during store operating hours for a single-store MVP, subject to the server, network, and backup arrangements.
- **Recoverability:** Daily full backup plus appropriate transaction-log backups based on recovery objectives; perform a documented restore test before go-live.
- **Maintainability:** Shared domain rules in one backend; automated tests for pricing, tax, discounts, payment validation, and stock posting.
- **Usability:** Cashier workflow must be keyboard- and scanner-friendly, with large controls, clear totals, and minimal modal dialogs.
- **Compatibility:** Target supported Windows desktop versions for FOH/BOH; confirm the exact OS and printer models before implementation.
- **Auditability:** Never hard-delete completed sales or posted stock movements. Correct them through linked reversal/refund/adjustment records.

## 8. Recommended MVP System Architecture

### 8.1 Architectural decision

Use a **modular monolith backend** with two separate Java desktop clients:

- **FrontRetail:** Java desktop application (JavaFX recommended).
- **Vim Manager:** Java desktop application (JavaFX recommended).
- **Backend API:** Java with Spring Boot, organised into clear modules.
- **Database:** Microsoft SQL Server.
- **API style:** REST/JSON over HTTPS on the private store network.
- **Database access:** Spring Data JPA/Hibernate for standard CRUD, with explicit SQL/transaction handling for checkout and stock posting where needed.
- **Build and dependency management:** Maven.
- **Packaging:** jpackage or a managed installer for Windows clients.

This is the closest practical MVP: one deployable backend and one database, without the operational complexity of microservices. The two desktop applications remain separate products but share a controlled API and common business rules.

### 8.2 Logical diagram

```text
                 STORE LAN / PRIVATE NETWORK
  ┌────────────────────────┐       ┌────────────────────────┐
  │ FrontRetail (FOH)       │       │ Vim Manager (BOH)      │
  │ JavaFX desktop client  │       │ JavaFX desktop client  │
  │ - Checkout             │       │ - Catalogue            │
  │ - Till session         │       │ - Purchasing/receiving │
  │ - Receipt printing     │       │ - Inventory/reports    │
  └────────────┬───────────┘       └────────────┬───────────┘
               │ HTTPS REST/JSON                │ HTTPS REST/JSON
               └────────────────┬───────────────┘
                                ▼
                  ┌──────────────────────────┐
                  │ Java Spring Boot API     │
                  │ Modular monolith         │
                  │                          │
                  │ Auth/RBAC                │
                  │ Product & pricing        │
                  │ Sales & payments         │
                  │ Till management          │
                  │ Inventory ledger         │
                  │ Purchasing/receiving     │
                  │ Reports & audit          │
                  └─────────────┬────────────┘
                                │ JDBC / least privilege
                                ▼
                  ┌──────────────────────────┐
                  │ Microsoft SQL Server     │
                  │ Source of truth          │
                  │ Transactions, indexes,   │
                  │ constraints, backups     │
                  └──────────────────────────┘
```
