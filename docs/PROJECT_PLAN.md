# VimSpace Superstore POS — Project Plan (MVP)

## Milestones
1. Project scaffolding (Maven multi-module) — DONE
2. Login GUI + splash + home dashboard — DONE
3. Backend API skeleton + MSSQL schema — DONE
4. Wire checkout to backend (sale commit, stock, idempotency)
5. Till session & cash management
6. Inventory, purchasing, returns
7. Reports + CSV export
8. Hardening: auth, audit, TLS, backups, restore test

## Success measures
- Cashier completes barcode sale without BOH access.
- Unique receipt per sale; atomic sale+stock commit.
- Manager reconciles sales by cashier/payment method.
- Permissions enforced by backend.
