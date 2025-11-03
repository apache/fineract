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

public interface SqlInjectionPreventerService {

    String encodeSql(String literal);

    /**
     * Validates and quotes a database identifier (table name, column name) using database-specific quoting rules. This
     * method ensures that identifiers are safely quoted to prevent SQL injection attacks.
     *
     * @param identifier
     *            the database identifier to quote
     * @param allowedValues
     *            optional set of allowed values for whitelist validation
     * @return the properly quoted identifier safe for use in SQL queries
     * @throws IllegalArgumentException
     *             if the identifier is invalid or not in the whitelist (if provided)
     */
    String quoteIdentifier(String identifier, java.util.Set<String> allowedValues);

    /**
     * Validates and quotes a database identifier without whitelist validation.
     *
     * @param identifier
     *            the database identifier to quote
     * @return the properly quoted identifier safe for use in SQL queries
     * @throws IllegalArgumentException
     *             if the identifier is null or empty
     */
    String quoteIdentifier(String identifier);
}
