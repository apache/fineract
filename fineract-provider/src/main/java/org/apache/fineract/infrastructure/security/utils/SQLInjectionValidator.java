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
package org.apache.fineract.infrastructure.security.utils;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public final class SQLInjectionValidator {

    private SQLInjectionValidator() {

    }

    private static final String[] DDL_COMMANDS = { "create", "drop", "alter", "truncate", "comment", "sleep" };

    private static final String[] DML_COMMANDS = { "select", "insert", "update", "delete", "merge", "upsert", "call" };

    private static final String[] COMMENTS = { "--", "({", "/*", "#" };

    private static final String SQL_PATTERN = "[a-zA-Z_=,\\-'!><.?\"`% ()0-9*\n\r]*";

    // TODO: see here https://rails-sqli.org for and
    // https://larrysteinle.com/2011/02/20/use-regular-expressions-to-detect-sql-code-injection more examples
    private static final List<String> INJECTION_PATTERNS = List.of("(?i).*[or|and]\s*[\"']?-1[\"']?\\s*(-*).*",
            "(?i).*\\s+[\"']?(\\d+)[\"']?\\s*=\\s*[\"']?(\\1)[\"']?\\s*(-*).*");

    public static void validateSQLInput(final String sqlSearch) {
        if (StringUtils.isBlank(sqlSearch)) {
            return;
        }

        // TODO: this should be replaced by INJECTION_PATTERNS
        String lowerCaseSQL = sqlSearch.toLowerCase();
        for (String ddl : DDL_COMMANDS) {
            if (lowerCaseSQL.contains(ddl)) {
                throw new SQLInjectionException();
            }
        }

        // TODO: this should be replaced by INJECTION_PATTERNS
        for (String dml : DML_COMMANDS) {
            if (lowerCaseSQL.contains(dml)) {
                throw new SQLInjectionException();
            }
        }

        // TODO: this should be replaced by INJECTION_PATTERNS
        for (String comments : COMMENTS) {
            if (lowerCaseSQL.contains(comments)) {
                throw new SQLInjectionException();
            }
        }

        // Removing the space before and after '=' operator
        // String s = " \" OR 1 = 1"; For the cases like this
        boolean injectionFound = false;

        String inputSqlString = lowerCaseSQL.replaceAll("\\s*=\\s*", "=");

        // TODO: this should be replaced by INJECTION_PATTERNS
        StringTokenizer tokenizer = new StringTokenizer(inputSqlString, " ");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (token.equals("'")) {
                if (tokenizer.hasMoreElements()) {
                    String nextToken = tokenizer.nextToken().trim();
                    if (!nextToken.equals("'")) {
                        injectionFound = true;
                        break;
                    }
                } else {
                    injectionFound = true;
                    break;
                }
            }
            if (token.equals("\"")) {
                if (tokenizer.hasMoreElements()) {
                    String nextToken = tokenizer.nextToken().trim();
                    if (!nextToken.equals("\"")) {
                        injectionFound = true;
                        break;
                    }
                } else {
                    injectionFound = true;
                    break;
                }
            } else if (token.indexOf('=') > 0) {
                StringTokenizer operatorToken = new StringTokenizer(token, "=");
                String operand = operatorToken.nextToken().trim();
                if (!operatorToken.hasMoreTokens()) {
                    injectionFound = true;
                    break;
                }
                String value = operatorToken.nextToken().trim();
                if (operand.equals(value)) {
                    injectionFound = true;
                    break;
                }
            }
        }
        if (injectionFound) {
            throw new SQLInjectionException();
        }

        for (String injectionPattern : INJECTION_PATTERNS) {
            Pattern pattern = Pattern.compile(injectionPattern);
            Matcher matcher = pattern.matcher(sqlSearch);
            if (matcher.matches()) {
                throw new SQLInjectionException();
            }
        }

        Pattern pattern = Pattern.compile(SQL_PATTERN);
        Matcher matcher = pattern.matcher(sqlSearch);
        if (!matcher.matches()) {
            throw new SQLInjectionException();
        }
    }

    public static void validateAdhocQuery(final String sqlSearch) {
        if (StringUtils.isBlank(sqlSearch)) {
            return;
        }

        // TODO: this should be replaced by INJECTION_PATTERNS
        String lowerCaseSQL = sqlSearch.toLowerCase().trim();
        for (String ddl : DDL_COMMANDS) {
            if (lowerCaseSQL.startsWith(ddl)) {
                throw new SQLInjectionException();
            }
        }

        // TODO: this should be replaced by INJECTION_PATTERNS
        for (String comments : COMMENTS) {
            if (lowerCaseSQL.contains(comments)) {
                throw new SQLInjectionException();
            }
        }

        // Removing the space before and after '=' operator
        // String s = " \" OR 1 = 1"; For the cases like this
        boolean injectionFound = false;

        String inputSqlString = lowerCaseSQL.replaceAll("\\s*=\\s*", "=");

        StringTokenizer tokenizer = new StringTokenizer(inputSqlString, " ");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (token.equals("'")) {
                if (tokenizer.hasMoreElements()) {
                    String nextToken = tokenizer.nextToken().trim();
                    if (!nextToken.equals("'")) {
                        injectionFound = true;
                        break;
                    }
                } else {
                    injectionFound = true;
                    break;
                }
            }
            if (token.equals("\"")) {
                if (tokenizer.hasMoreElements()) {
                    String nextToken = tokenizer.nextToken().trim();
                    if (!nextToken.equals("\"")) {
                        injectionFound = true;
                        break;
                    }
                } else {
                    injectionFound = true;
                    break;
                }
            } else if (token.indexOf('=') > 0) {
                StringTokenizer operatorToken = new StringTokenizer(token, "=");
                String operand = operatorToken.nextToken().trim();
                if (!operatorToken.hasMoreTokens()) {
                    injectionFound = true;
                    break;
                }
                String value = operatorToken.nextToken().trim();
                if (operand.equals(value)) {
                    injectionFound = true;
                    break;
                }
            }
        }
        if (injectionFound) {
            throw new SQLInjectionException();
        }

        for (String injectionPattern : INJECTION_PATTERNS) {
            Pattern pattern = Pattern.compile(injectionPattern);
            Matcher matcher = pattern.matcher(sqlSearch);
            if (matcher.matches()) {
                throw new SQLInjectionException();
            }
        }

        Pattern pattern = Pattern.compile(SQL_PATTERN);
        Matcher matcher = pattern.matcher(sqlSearch);
        if (!matcher.matches()) {
            throw new SQLInjectionException();
        }
    }
}
