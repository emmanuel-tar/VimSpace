# VimSpace Superstore POS — Architecture

## Overview
Modular monolith backend (Spring Boot) with two Java desktop clients (FrontRetail FOH, Vim Manager BOH) and MSSQL as system of record.

## Modules
- `shared-contracts` — DTOs/requests shared between backend and clients.
- `backend-api` — Spring Boot REST API, auth/RBAC, sales, inventory, reports, audit.
- `frontretail-client` — JavaFX/Swing desktop checkout client.
- `vim-manager-client` — JavaFX/Swing desktop management client.

## Data flow
Clients → HTTPS REST/JSON → Backend API → MSSQL (atomic transactions). Clients never connect directly to MSSQL.

## Key decisions
- Modular monolith for MVP (single deployable backend + DB).
- Idempotency keys prevent duplicate sales.
- Append-only stock movement ledger; no hard deletes of posted transactions.
