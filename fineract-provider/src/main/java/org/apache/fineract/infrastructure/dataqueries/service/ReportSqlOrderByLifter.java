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
package org.apache.fineract.infrastructure.dataqueries.service;

import java.util.regex.Pattern;

/**
 * FINERACT-2622: {@link GenericDataServiceImpl#wrapSQL(String)} wraps every report query as
 * {@code select x.* from (<sql>) x}, so that a derived table's own {@code ORDER BY} is not guaranteed to be honored by
 * the outer query. This helper detects a top-level trailing {@code ORDER BY} (together with any trailing
 * {@code LIMIT}/{@code OFFSET}) in the original SQL and lifts it onto the outer wrapper, where it resolves against the
 * columns exposed by {@code x.*}, so the requested ordering is preserved.
 *
 * <p>
 * The scan ignores {@code ORDER BY} that appears inside parentheses (subqueries or window functions), inside string
 * literals, or inside SQL comments. As a safeguard against regressions, lifting is skipped when the trailing clause
 * contains a dot-qualified reference (e.g. {@code alias.column}) that would not resolve against {@code x.*} in the
 * outer scope, leaving the original (pre-fix) behavior for that case.
 */
final class ReportSqlOrderByLifter {

    private static final Pattern DOT_QUALIFIED_REFERENCE = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*\\s*\\.\\s*[A-Za-z_*]");

    private ReportSqlOrderByLifter() {}

    /**
     * Wraps the given report SQL in a derived-table select, lifting a top-level trailing {@code ORDER BY} (and any
     * trailing {@code LIMIT}/{@code OFFSET}) onto the outer wrapper when one is present and safe to lift.
     */
    static String wrap(final String sql) {
        final int orderByIndex = topLevelTrailingOrderByIndex(sql);
        if (orderByIndex < 0) {
            return "select x.* from (" + sql + ") x";
        }
        final String inner = sql.substring(0, orderByIndex);
        final String trailingOrderBy = sql.substring(orderByIndex);
        return "select x.* from (" + inner + ") x " + trailingOrderBy;
    }

    /**
     * Returns the index of the last top-level {@code ORDER BY} keyword in {@code sql}, or {@code -1} if there is none,
     * or if the one found is not safe to lift (contains a dot-qualified column reference).
     */
    private static int topLevelTrailingOrderByIndex(final String sql) {
        int depth = 0;
        int lastTopLevelOrderBy = -1;
        final int length = sql.length();
        int i = 0;
        while (i < length) {
            final char c = sql.charAt(i);
            if (c == '\'') {
                i = skipStringLiteral(sql, i);
                continue;
            }
            if (c == '-' && i + 1 < length && sql.charAt(i + 1) == '-') {
                i = skipLineComment(sql, i);
                continue;
            }
            if (c == '/' && i + 1 < length && sql.charAt(i + 1) == '*') {
                i = skipBlockComment(sql, i);
                continue;
            }
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && isOrderByAt(sql, i)) {
                lastTopLevelOrderBy = i;
            }
            i++;
        }
        if (lastTopLevelOrderBy < 0 || DOT_QUALIFIED_REFERENCE.matcher(sql.substring(lastTopLevelOrderBy)).find()) {
            return -1;
        }
        return lastTopLevelOrderBy;
    }

    private static int skipStringLiteral(final String sql, final int start) {
        final int length = sql.length();
        int i = start + 1;
        while (i < length) {
            if (sql.charAt(i) == '\'') {
                if (i + 1 < length && sql.charAt(i + 1) == '\'') {
                    i += 2;
                    continue;
                }
                return i + 1;
            }
            i++;
        }
        return i;
    }

    private static int skipLineComment(final String sql, final int start) {
        final int length = sql.length();
        int i = start + 2;
        while (i < length && sql.charAt(i) != '\n') {
            i++;
        }
        return i;
    }

    private static int skipBlockComment(final String sql, final int start) {
        final int length = sql.length();
        int i = start + 2;
        while (i + 1 < length && !(sql.charAt(i) == '*' && sql.charAt(i + 1) == '/')) {
            i++;
        }
        return Math.min(i + 2, length);
    }

    private static boolean isOrderByAt(final String sql, final int index) {
        if (!sql.regionMatches(true, index, "order", 0, 5)) {
            return false;
        }
        if (index > 0 && isWordChar(sql.charAt(index - 1))) {
            return false;
        }
        int j = index + 5;
        final int afterOrder = j;
        while (j < sql.length() && Character.isWhitespace(sql.charAt(j))) {
            j++;
        }
        if (j == afterOrder) {
            return false;
        }
        if (!sql.regionMatches(true, j, "by", 0, 2)) {
            return false;
        }
        final int afterBy = j + 2;
        return afterBy >= sql.length() || !isWordChar(sql.charAt(afterBy));
    }

    private static boolean isWordChar(final char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
