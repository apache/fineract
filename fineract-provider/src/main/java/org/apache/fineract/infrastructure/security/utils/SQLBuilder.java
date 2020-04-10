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

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility to assemble the WHERE clause of an SQL query without the risk of SQL injection.
 *
 * <p>When using this utility instead of manually assembling SQL queries, then
 * {@link SQLInjectionValidator} should not be required anymore.  (Correctly using
 * this means only ever passing completely fixed String literals to .)
 *
 * @author Michael Vorburger <mike@vorburger.ch>
 */
public class SQLBuilder {

    private final static Pattern ATOZ = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_-]*\\.)?[a-zA-Z_-][a-zA-Z0-9_-]*");

    // This holds the query string, with the '?' placeholders, but no argument values
    private final StringBuilder sb = new StringBuilder();

    // This holds the arguments, in the order of the '?' placeholders in sb
    private final List<Object> args = new ArrayList<>();

    // This holds the criterias, where nth element corresponds to nth element in args
    private final ArrayList<String> crts = new ArrayList<String>();
    /**
     * Adds a criteria for a SQL WHERE clause.
     * All criteria are appended by AND (support for OR, or nesting, can be added when needed).
     * @param criteria The name of the column to be filtered, and an operator; e.g. "name =" or "age >" (but without '?' placeholder)
     * @param argument The argument to be filtered on (e.g. "Michael" or 123).  The null value is explicitly permitted.
     */
    public void addCriteria(String criteria, Object argument) {
        if (criteria == null || criteria.trim().isEmpty()) {
            throw new IllegalArgumentException("criteria cannot be null");
        }
        String trimmedCriteria = criteria.trim();
        if (trimmedCriteria.isEmpty()) {
            throw new IllegalArgumentException("criteria cannot be null");
        }
        if (trimmedCriteria.contains("?")) {
            throw new IllegalArgumentException("criteria cannot contain a '?' (that is automatically added at the end): " + trimmedCriteria);
        }
        int columnOperatorIndex = trimmedCriteria.indexOf(' ');
        if (columnOperatorIndex == -1) {
            throw new IllegalArgumentException("criteria missing operator: " + trimmedCriteria);
        }
        String columnName = trimmedCriteria.substring(0, columnOperatorIndex).trim().toLowerCase(Locale.ROOT);
        if (!ATOZ.matcher(columnName).matches()) {
            throw new IllegalArgumentException("criteria column name must match [a-z]: " + trimmedCriteria);
        }
        String operator = trimmedCriteria.substring(columnOperatorIndex).trim();
        if (operator.indexOf(' ') > -1) {
            throw new IllegalArgumentException("criteria cannot contain more than 1 space (between column name and operator): " + trimmedCriteria);
        }
        if (!operator.equals("=") && !operator.equals("<") && !operator.equals(">")
                && !operator.equals("<=") && !operator.equals(">=") && !operator.equals("<>")
                && !operator.equals("LIKE") && !operator.equals("like")) {
            // add support for SQL's BETWEEN and IN, if/when ever needed.. (it's a little more than just adding above, as it can have multiple arguments)
            throw new IllegalArgumentException("criteria must end with valid SQL operator for WHERE: " + trimmedCriteria);
        }

        if (sb.length() > 0) {
            sb.append("  AND  ");
        }
        sb.append(trimmedCriteria);
        sb.append(" ?");
        crts.add(trimmedCriteria);
        args.add(argument);
    }

    /**
     * Delegates to {@link #addCriteria(String, Object)} if argument is not null, otherwise does nothing.
     */
    public void addNonNullCriteria(String criteria, Object argument) {
        if (argument != null) {
            addCriteria(criteria, argument);
        }
    }

    /**
     * Returns a SQL WHERE clause, created from the {@link #addCriteria(String, Object)}, with '?' placeholders.
     * @return SQL WHERE clause, almost always starting with " WHERE ..." (unless no criteria, then empty)
     */
    public String getSQLTemplate() {
        if (sb.length() > 0) {
            return " WHERE  " + sb.toString();
        }
        return "";
    }

    /**
     * Returns the arguments for the WHERE clause.
     * @return Object array suitable for use with Spring Framework JdbcTemplate (or plain JDBC {@link PreparedStatement})
     */
    public Object[] getArguments() {
        return args.toArray();
    }

    /*
     * Returns a String representation suitable for debugging and log output.
     * This is ONLY intended for debugging in logs, and NEVER for passing to a JDBC database.
     */
    @Override
    public String toString() {
       StringBuilder whereClause  = new StringBuilder("SQLBuilder{");
       for (int i=0;i<args.size();i++)
        {
            if (i!=0)
            {
                whereClause.append("  AND  ");
            }
            else
            {
                whereClause.append("WHERE  ");
            }
            Object currentArg = args.get(i);
            whereClause.append(crts.get(i));
            whereClause.append(" ");
            whereClause.append("[");
            if(currentArg instanceof String)
            {
                whereClause.append("'");
                whereClause.append(currentArg);
                whereClause.append("'");
            }else if(currentArg == null)
            {
                whereClause.append("null");
            }else{
                whereClause.append(String.valueOf(currentArg));
            }
            whereClause.append("]");
        }
         whereClause.append("}");
         return whereClause.toString();
    }
}
