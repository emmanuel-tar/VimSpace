# ADR-001: Modular Monolith Backend

## Status
Accepted

## Context
POS needs shared business rules between FOH and BOH clients. Microservices add operational complexity unsuitable for a single-store MVP.

## Decision
Use a modular monolith Spring Boot backend with one MSSQL database. Two desktop clients share the API.

## Consequences
- Simpler deployment and transactions.
- Future multi-branch possible via store/location identifier without rewrite.
