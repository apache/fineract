/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.security.service;

import java.sql.SQLException;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.security.exception.EscapeSqlLiteralException;
import org.postgresql.core.Utils;
import org.springframework.stereotype.Service;

@Service
public class SqlInjectionPreventerServiceImpl implements SqlInjectionPreventerService {

    private final DatabaseTypeResolver databaseTypeResolver;

    public SqlInjectionPreventerServiceImpl(DatabaseTypeResolver databaseTypeResolver) {
        this.databaseTypeResolver = databaseTypeResolver;
    }

    @Override
    public String encodeSql(String literal) {
        if (literal == null) {
            return "NULL";
        }

        if (databaseTypeResolver.isMySQL()) {
            return escapeMySQLLiteral(literal);
        } else if (databaseTypeResolver.isPostgreSQL()) {
            try {
                return Utils.escapeLiteral(null, literal, true).toString();
            } catch (SQLException e) {
                throw new EscapeSqlLiteralException("Failed to escape SQL literal", e);
            }
        } else {
            // For unknown database types, return the input unchanged
            // This maintains backward compatibility while still providing basic protection for known types
            return literal;
        }
    }

    /**
     * Escapes a string literal for MySQL/MariaDB using native MySQL standard escaping rules. This method replaces the
     * vulnerable ESAPI SQL encoding to address CVE-2025-5878.
     *
     * According to MySQL documentation, the following characters need to be escaped: - Single quote (') -> \' - Double
     * quote (") -> \" - Backslash (\) -> \\ - Null byte (\0) -> \\0 - Newline (\n) -> \\n - Carriage return (\r) -> \\r
     * - Tab (\t) -> \\t - ASCII 26 (Ctrl+Z) -> \\Z
     *
     * @param literal
     *            the string literal to escape
     * @return the escaped string literal safe for MySQL/MariaDB SQL queries
     */
    private String escapeMySQLLiteral(String literal) {

        StringBuilder escaped = new StringBuilder(literal.length() * 2);

        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);

            switch (c) {
                case '\0': // Null byte
                    escaped.append("\\0");
                break;
                case '\'':
                    escaped.append("\\'");
                break;
                case '\"':
                    escaped.append("\\\"");
                break;
                case '\\':
                    escaped.append("\\\\");
                break;
                case '\n':
                    escaped.append("\\n");
                break;
                case '\r':
                    escaped.append("\\r");
                break;
                case '\t':
                    escaped.append("\\t");
                break;
                case '\032':
                    escaped.append("\\Z");
                break;
                default:
                    escaped.append(c);
                break;
            }
        }

        return escaped.toString();
    }

    @Override
    public String quoteIdentifier(String identifier, Set<String> allowedValues) {
        if (StringUtils.isBlank(identifier)) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }

        // Whitelist validation if provided
        if (allowedValues != null && !allowedValues.contains(identifier.toLowerCase())) {
            throw new IllegalArgumentException("Identifier not in whitelist: " + identifier);
        }

        return quoteIdentifier(identifier);
    }

    @Override
    public String quoteIdentifier(String identifier) {
        if (StringUtils.isBlank(identifier)) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }

        // Validate identifier contains only safe characters (alphanumeric and underscore)
        if (!identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid identifier format: " + identifier);
        }

        if (databaseTypeResolver.isMySQL()) {
            // MySQL/MariaDB uses backticks for identifier quoting
            return "`" + identifier.replace("`", "``") + "`";
        } else if (databaseTypeResolver.isPostgreSQL()) {
            // PostgreSQL uses double quotes for identifier quoting
            return "\"" + identifier.replace("\"", "\"\"") + "\"";
        } else {
            // For unknown database types, return identifier as-is if it passes validation
            // This maintains backward compatibility while still providing basic protection
            return identifier;
        }
    }
}
