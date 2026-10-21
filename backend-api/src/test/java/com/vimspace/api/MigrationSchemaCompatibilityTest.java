package com.vimspace.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManagerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class MigrationSchemaCompatibilityTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private static final Map<String, Map<String, String>> SQL_COLUMNS = new LinkedHashMap<>();
    private static final Map<String, Map<String, Boolean>> SQL_NULLABLE = new LinkedHashMap<>();
    private static final Map<String, Map<String, Boolean>> SQL_UNIQUE = new LinkedHashMap<>();

    @BeforeAll
    static void parseMigrationsAndEntities() {
        parseScript(findMigrationsDir());
    }

    @Test
    @DisplayName("Every mapped column exists in the MSSQL migrations with a compatible type")
    void migrationsMatchEntityMapping() {
        Metamodel metamodel = entityManagerFactory.getMetamodel();
        Set<EntityType<?>> entities = metamodel.getEntities();

        List<String> problems = new ArrayList<>();

        for (EntityType<?> entityType : entities) {
            Class<?> entityClass = entityType.getJavaType();
            Table tableAnn = entityClass.getAnnotation(Table.class);
            String tableName = (tableAnn != null && !tableAnn.name().isEmpty())
                    ? tableAnn.name()
                    : entityClass.getSimpleName();
            tableName = tableName.toLowerCase(Locale.ROOT);

            Map<String, String> sqlColumns = SQL_COLUMNS.get(tableName);
            if (sqlColumns == null) {
                problems.add("Table '" + tableName + "' (entity "
                        + entityClass.getName() + ") is missing from the migrations");
                continue;
            }

            for (Attribute<?, ?> attribute : entityType.getAttributes()) {
                // Plural attributes (collections / element collections) map to
                // separate tables, not columns on this table.
                if (attribute instanceof jakarta.persistence.metamodel.PluralAttribute) {
                    continue;
                }

                String columnName = toColumnName(attribute);

                String declaredType = sqlColumns.get(columnName);
                if (declaredType == null) {
                    problems.add("Column '" + tableName + "." + columnName + "' (entity "
                            + entityClass.getName() + ") is missing from the migrations");
                    continue;
                }

                String expectedType = expectedSqlType(attribute.getJavaType(), attribute.isAssociation());
                String declaredBase = stripLength(declaredType);
                String expectedBase = stripLength(expectedType);
                if (!expectedBase.equalsIgnoreCase(declaredBase)) {
                    problems.add("Column '" + tableName + "." + columnName + "' is declared as "
                            + declaredType + " in the migrations but maps to "
                            + expectedType + " in " + entityClass.getName());
                }

                boolean sqlNullable = SQL_NULLABLE.get(tableName).get(columnName);
                boolean jpaNullable = false;
                if (attribute instanceof jakarta.persistence.metamodel.SingularAttribute singular) {
                    jpaNullable = singular.isOptional();
                }
                if (sqlNullable != jpaNullable) {
                    problems.add("Column '" + tableName + "." + columnName + "' nullability differs: "
                            + (sqlNullable ? "NULL" : "NOT NULL") + " in the migrations vs "
                            + (jpaNullable ? "nullable" : "not null") + " in "
                            + entityClass.getName());
                }
            }
        }

        if (!problems.isEmpty()) {
            fail("The MSSQL migrations have drifted from the JPA entity mapping:\n  - "
                    + String.join("\n  - ", problems)
                    + "\n\nUpdate database/migrations/*.sql to match the entities.");
        }
    }

    @Test
    @DisplayName("The till-session reconciliation columns are present in the migrations")
    void tillSessionReconciliationColumnsExist() {
        Map<String, String> columns = SQL_COLUMNS.get("till_sessions");
        assertTrue(columns != null, "till_sessions table is missing from the migrations");
        assertTrue(columns.containsKey("expected_cash"),
                "till_sessions.expected_cash is missing from the migrations");
        assertTrue(columns.containsKey("notes"),
                "till_sessions.notes is missing from the migrations");
        assertTrue("DECIMAL(18,4)".equalsIgnoreCase(columns.get("expected_cash")),
                "till_sessions.expected_cash should be DECIMAL(18,4) but is "
                        + columns.get("expected_cash"));
        assertTrue("VARCHAR(500)".equalsIgnoreCase(columns.get("notes")),
                "till_sessions.notes should be VARCHAR(500) but is " + columns.get("notes"));

        assertTrue(SQL_COLUMNS.get("sales").containsKey("session_id"),
                "sales.session_id is missing from the migrations");
    }
    private static String expectedSqlType(Class<?> javaType, boolean isAssociation) {
        if (isAssociation) {
            // All surrogate keys in this schema are Long / BIGINT.
            return "BIGINT";
        }
        return switch (javaType.getName()) {
            case "java.lang.String" -> "VARCHAR(255)";
            case "java.lang.Long", "long" -> "BIGINT";
            case "java.lang.Integer", "int" -> "INT";
            case "java.lang.Boolean", "boolean" -> "BIT";
            case "java.math.BigDecimal" -> "DECIMAL(19,4)";
            case "java.time.LocalDateTime" -> "DATETIME2";
            default -> throw new IllegalStateException(
                    "Unmapped Java type for the migration check: " + javaType.getName());
        };
    }

    private static String stripLength(String sqlType) {
        int paren = sqlType.indexOf('(');
        return paren >= 0 ? sqlType.substring(0, paren) : sqlType;
    }

    /** Hibernate's implicit naming strategy: camelCase attribute -> snake_case column. */
    private static String toColumnName(Attribute<?, ?> attribute) {
        String name = attribute.getName();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (sb.length() > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        String base = sb.toString();
        if (attribute.isAssociation()) {
            // FK column: {attribute}_{referenced_pk_column}; all PKs are named "id".
            base = base + "_id";
        }
        return base;
    }

    private static Path findMigrationsDir() {
        Path dir = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 6 && dir != null; i++) {
            Path candidate = dir.resolve("database").resolve("migrations");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("Could not locate database/migrations");
    }

    private static void parseScript(Path migrationsDir) {
        try (var files = Files.list(migrationsDir)) {
            List<Path> scripts = files
                    .filter(p -> p.getFileName().toString().endsWith(".sql"))
                    .sorted()
                    .toList();
            if (scripts.isEmpty()) {
                throw new IllegalStateException("No .sql migration scripts found in " + migrationsDir);
            }
            for (Path script : scripts) {
                parseSql(Files.readString(script, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the migration scripts", e);
        }
    }

    private static final Pattern CREATE_TABLE =
            Pattern.compile("(?i)CREATE\\s+TABLE\\s+(\\w+)\\s*\\(");
    private static final Pattern ALTER_TABLE_ADD =
            Pattern.compile("(?i)ALTER\\s+TABLE\\s+(\\w+)\\s+ADD\\s+(\\w+)\\s+(.+)");
    private static final Pattern COLUMN_DEFINITION =
            Pattern.compile("(?i)^(\\w+)\\s+([A-Za-z]+)(\\s*\\([^)]*\\))?\\s*(.*)$");

    private static final List<String> CONSTRAINT_KEYWORDS = List.of(
            "PRIMARY", "UNIQUE", "CONSTRAINT", "FOREIGN", "CHECK", "INDEX", "KEY");

    private static void parseSql(String sql) {
        StringBuilder cleaned = new StringBuilder();
        for (String line : sql.split("\\R")) {
            int comment = line.indexOf("--");
            cleaned.append(comment >= 0 ? line.substring(0, comment) : line).append('\n');
        }
        String body = cleaned.toString();

        Matcher createTable = CREATE_TABLE.matcher(body);
        while (createTable.find()) {
            String table = createTable.group(1).toLowerCase(Locale.ROOT);
            int end = body.indexOf("\n);", createTable.end());
            String block = body.substring(createTable.end(), end < 0 ? body.length() : end);
            for (String line : block.split("\\R")) {
                parseColumnLine(table, line);
            }
        }

        Matcher alter = ALTER_TABLE_ADD.matcher(body);
        while (alter.find()) {
            String table = alter.group(1).toLowerCase(Locale.ROOT);
            parseColumnLine(table, alter.group(2) + " " + alter.group(3));
        }
    }

    private static void parseColumnLine(String table, String rawLine) {
        String line = rawLine.trim();
        if (line.isEmpty()) {
            return;
        }
        line = line.replaceAll(",$", "").trim();
        int comment = line.indexOf("--");
        if (comment >= 0) {
            line = line.substring(0, comment).trim();
        }
        if (line.isEmpty()) {
            return;
        }

        Matcher matcher = COLUMN_DEFINITION.matcher(line);
        if (!matcher.matches()) {
            return;
        }
        String name = matcher.group(1).toLowerCase(Locale.ROOT);
        if (CONSTRAINT_KEYWORDS.contains(name.toUpperCase(Locale.ROOT))) {
            return;
        }

        String type = matcher.group(2).toUpperCase(Locale.ROOT)
                + (matcher.group(3) == null ? "" : matcher.group(3).replace(" ", ""));
        if ("NUMERIC".equals(type)) {
            type = "DECIMAL";
        }

        String rest = matcher.group(4) == null ? "" : matcher.group(4).toUpperCase(Locale.ROOT);
        boolean nullable = !rest.contains("NOT NULL");
        boolean unique = rest.contains("UNIQUE") || rest.contains("PRIMARY KEY");

        SQL_COLUMNS.computeIfAbsent(table, k -> new LinkedHashMap<>()).put(name, type);
        SQL_NULLABLE.computeIfAbsent(table, k -> new LinkedHashMap<>()).put(name, nullable);
        SQL_UNIQUE.computeIfAbsent(table, k -> new LinkedHashMap<>()).put(name, unique);
    }
}

