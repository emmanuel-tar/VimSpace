# VimSpace Superstore POS

Retail POS platform with FrontRetail (FOH) and Vim Manager (BOH) clients sharing a Spring Boot backend and MSSQL database.

## Structure
```
vimspace-pos/
├── pom.xml
├── shared-contracts/   # DTOs
├── backend-api/        # Spring Boot REST API
├── frontretail-client/ # FOH desktop client
├── vim-manager-client/ # BOH desktop client
├── database/           # MSSQL migrations + seed data
├── docs/               # PRD, architecture, ADRs, runbooks
└── tests/
```

See `docs/` for PRD, architecture, and runbooks.
