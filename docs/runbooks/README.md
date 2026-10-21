# Runbooks

## Backend startup
```
cd backend-api
./mvnw spring-boot:run
```
(Default port 8080; configure MSSQL connection in `application.properties`.)

## Database
Apply `database/migrations/V1__init_schema.sql` to MSSQL `vimspace_db`.

## FrontRetail client
```
cd frontretail-client
./mvnw javafx:run   # or build and run FrontRetailApp
```
Login: admin/admin, cashier/cashier, manager/manager.
