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

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.security.exception.InputValidationException;

/**
 * Allow-list checks for values that are concatenated into SQL as identifiers rather than bound as parameters, such as
 * the orderBy and sortOrder of a paged read. Only plain identifiers, optionally qualified by one alias and separated by
 * commas, are accepted, so no expression, sub-select or function call can reach the ORDER BY clause.
 */
public final class SqlIdentifierValidator {

    private static final String IDENTIFIER = "[A-Za-z_][A-Za-z0-9_]*";
    private static final String QUALIFIED_IDENTIFIER = IDENTIFIER + "(\\." + IDENTIFIER + ")?";
    private static final Pattern ORDER_BY = Pattern.compile("^" + QUALIFIED_IDENTIFIER + "(\\s*,\\s*" + QUALIFIED_IDENTIFIER + ")*$");
    private static final Pattern SORT_ORDER = Pattern.compile("^(?i)(asc|desc)$");

    private SqlIdentifierValidator() {}

    /**
     * Returns the orderBy unchanged if it is blank or a comma-separated list of (optionally alias-qualified) column
     * identifiers; otherwise throws.
     */
    public static String validateOrderBy(final String orderBy) {
        if (StringUtils.isBlank(orderBy)) {
            return orderBy;
        }
        if (!ORDER_BY.matcher(orderBy.trim()).matches()) {
            throw new InputValidationException("orderBy must be a column name");
        }
        return orderBy.trim();
    }

    /**
     * Returns the sortOrder unchanged if it is blank, ASC or DESC (any case); otherwise throws.
     */
    public static String validateSortOrder(final String sortOrder) {
        if (StringUtils.isBlank(sortOrder)) {
            return sortOrder;
        }
        if (!SORT_ORDER.matcher(sortOrder.trim()).matches()) {
            throw new InputValidationException("sortOrder must be ASC or DESC");
        }
        return sortOrder.trim();
    }
}
