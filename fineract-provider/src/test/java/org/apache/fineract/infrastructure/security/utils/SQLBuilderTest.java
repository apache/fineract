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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit Test for {@link SQLBuilder}.
 *
 * @author Michael Vorburger <mike@vorburger.ch>
 */
public class SQLBuilderTest {

    @Test
    public void testEmpty() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        assertEquals("", sqlBuilder.getSQLTemplate());
        assertArrayEquals(new Object[] {}, sqlBuilder.getArguments());
        assertEquals("SQLBuilder{}", sqlBuilder.toString());
    }

    @Test
    public void testUsage() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.addCriteria("name =", "Michael");
        sqlBuilder.addCriteria("hobby LIKE ", "Mifos/Apache Fineract");
        sqlBuilder.addCriteria("age <  ", 123);
        assertEquals(" WHERE  name = ?  AND  hobby LIKE ?  AND  age < ?", sqlBuilder.getSQLTemplate());
        assertArrayEquals(new Object[] { "Michael", "Mifos/Apache Fineract", 123 }, sqlBuilder.getArguments());
        assertEquals("SQLBuilder{WHERE  name = ['Michael']  AND  hobby LIKE ['Mifos/Apache Fineract']  AND  age < [123]}",
                sqlBuilder.toString());
    }

    @Test
    public void testNullArgument() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.addCriteria("ref =", null);
        assertEquals(" WHERE  ref = ?", sqlBuilder.getSQLTemplate());
        assertArrayEquals(new Object[] { null }, sqlBuilder.getArguments());
        assertEquals("SQLBuilder{WHERE  ref = [null]}", sqlBuilder.toString());
    }

    @Test
    public void testLowerAndUpperCaseOperators() {
        SQLBuilder sqlBuilder = new SQLBuilder();
        sqlBuilder.addCriteria("hobby LIKE ", "Mifos/Apache Fineract");
        sqlBuilder.addCriteria("hobby like ", "Mifos/Apache Fineract");
    }

    @Test
    public void testAddIllegalArguments() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("age<", 123);
        }, "space between column and operator");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria(null, "argument");
        }, "null Criteria Fragment");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("", "argument");
        }, "empty Criteria Fragment");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria(" ", "argument");
        }, "space only Criteria Fragment");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("age = ?", 123);
        }, "Criteria Fragment with ?");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("age", 123);
        }, "Criteria Fragment missing operator");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("and age = ?", 123);
        }, "Criteria starts with AND");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("age = ? and", 123);
        }, "Criteria ends with AND");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("or age =", 123);
        }, "Criteria starts with OR");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("age = ? or", 123);
        }, "Criteria ends with OR");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("(age =", 123);
        }, "Criteria contains opening parentheis");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("age = ?)", 123);
        }, "Criteria contains closing parentheis");
        assertThrows(IllegalArgumentException.class, () -> {
            new SQLBuilder().addCriteria("age< = ?)", 123);
        }, "Offset corner case");

    }
}
