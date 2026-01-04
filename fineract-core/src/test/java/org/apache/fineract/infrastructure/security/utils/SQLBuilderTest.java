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

import org.apache.fineract.infrastructure.security.utils.SQLBuilder.WhereLogicalOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SQLBuilder}.
 * <p>
 * Tests the SQL WHERE clause builder utility that prevents SQL injection by validating column names, operators, and
 * using parameterized queries.
 * </p>
 */
class SQLBuilderTest {

    @Test
    void testAddCriteriaWithValidEquals() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when
        builder.addCriteria("name =", "John");
        // then
        Assertions.assertEquals(" WHERE  name = ?", builder.getSQLTemplate());
        Assertions.assertArrayEquals(new Object[] { "John" }, builder.getArguments());
    }

    @Test
    void testAddCriteriaWithValidOperators() {
        // given
        SQLBuilder equalsBuilder = new SQLBuilder();
        SQLBuilder lessThanBuilder = new SQLBuilder();
        SQLBuilder greaterThanBuilder = new SQLBuilder();
        SQLBuilder lessEqualBuilder = new SQLBuilder();
        SQLBuilder greaterEqualBuilder = new SQLBuilder();
        SQLBuilder notEqualBuilder = new SQLBuilder();
        SQLBuilder likeBuilder = new SQLBuilder();
        // when
        equalsBuilder.addCriteria("age =", 25);
        lessThanBuilder.addCriteria("age <", 30);
        greaterThanBuilder.addCriteria("age >", 20);
        lessEqualBuilder.addCriteria("age <=", 30);
        greaterEqualBuilder.addCriteria("age >=", 20);
        notEqualBuilder.addCriteria("status <>", "inactive");
        likeBuilder.addCriteria("name LIKE", "%John%");
        // then
        Assertions.assertTrue(equalsBuilder.getSQLTemplate().contains("age = ?"));
        Assertions.assertTrue(lessThanBuilder.getSQLTemplate().contains("age < ?"));
        Assertions.assertTrue(greaterThanBuilder.getSQLTemplate().contains("age > ?"));
        Assertions.assertTrue(lessEqualBuilder.getSQLTemplate().contains("age <= ?"));
        Assertions.assertTrue(greaterEqualBuilder.getSQLTemplate().contains("age >= ?"));
        Assertions.assertTrue(notEqualBuilder.getSQLTemplate().contains("status <> ?"));
        Assertions.assertTrue(likeBuilder.getSQLTemplate().contains("name LIKE ?"));
    }

    @Test
    void testAddCriteriaNullThrowsException() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when / then
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.addCriteria(null, "value"));
    }

    @Test
    void testAddCriteriaEmptyThrowsException() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when / then
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.addCriteria("", "value"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.addCriteria("   ", "value"));
    }

    @Test
    void testAddCriteriaWithQuestionMarkThrowsException() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when / then
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.addCriteria("name = ?", "value"));
    }

    @Test
    void testAddCriteriaMissingOperatorThrowsException() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when / then
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.addCriteria("name", "value"));
    }

    @Test
    void testAddCriteriaInvalidColumnNameThrowsException() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when / then
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.addCriteria("123name =", "value"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.addCriteria("na$me =", "value"));
    }

    @Test
    void testAddCriteriaInvalidOperatorThrowsException() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when / then
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.addCriteria("name ++", "value"));
    }

    @Test
    void testAddNonNullCriteriaSkipsNull() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when
        builder.addNonNullCriteria("name =", null);
        // then
        Assertions.assertEquals("", builder.getSQLTemplate());
        Assertions.assertArrayEquals(new Object[] {}, builder.getArguments());
    }

    @Test
    void testAddNonNullCriteriaAddsWhenNotNull() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when
        builder.addNonNullCriteria("name =", "John");
        // then
        Assertions.assertEquals(" WHERE  name = ?", builder.getSQLTemplate());
        Assertions.assertArrayEquals(new Object[] { "John" }, builder.getArguments());
    }

    @Test
    void testGetSQLTemplateEmptyWhenNoCriteria() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when / then
        Assertions.assertEquals("", builder.getSQLTemplate());
    }

    @Test
    void testGetArgumentsReturnsCorrectOrder() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when
        builder.addCriteria("name =", "John");
        builder.addCriteria("age =", 25);
        builder.addCriteria("status =", "active");
        // then
        Object[] args = builder.getArguments();
        Assertions.assertEquals(3, args.length);
        Assertions.assertEquals("John", args[0]);
        Assertions.assertEquals(25, args[1]);
        Assertions.assertEquals("active", args[2]);
    }

    @Test
    void testWhereLogicalOperatorAnd() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when
        builder.addCriteria("name =", "John", WhereLogicalOperator.AND);
        builder.addCriteria("age =", 25, WhereLogicalOperator.AND);
        // then
        String sql = builder.getSQLTemplate();
        Assertions.assertTrue(sql.contains(" AND "));
        Assertions.assertTrue(sql.contains("name = ?"));
        Assertions.assertTrue(sql.contains("age = ?"));
    }

    @Test
    void testWhereLogicalOperatorOr() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when
        builder.addCriteria("name =", "John", WhereLogicalOperator.AND);
        builder.addCriteria("name =", "Jane", WhereLogicalOperator.OR);
        // then
        String sql = builder.getSQLTemplate();
        Assertions.assertTrue(sql.contains(" OR "));
    }

    @Test
    void testToStringFormat() {
        // given
        SQLBuilder builder = new SQLBuilder();
        builder.addCriteria("name =", "John");
        builder.addCriteria("age =", 25);
        // when
        String result = builder.toString();
        // then
        Assertions.assertTrue(result.startsWith("SQLBuilder{"));
        Assertions.assertTrue(result.endsWith("}"));
        Assertions.assertTrue(result.contains("name ="));
        Assertions.assertTrue(result.contains("'John'"));
        Assertions.assertTrue(result.contains("age ="));
        Assertions.assertTrue(result.contains("25"));
    }

    @Test
    void testToStringWithNullArgument() {
        // given
        SQLBuilder builder = new SQLBuilder();
        builder.addCriteria("status is", null);
        // when
        String result = builder.toString();
        // then
        Assertions.assertTrue(result.contains("null"));
    }

    @Test
    void testColumnNameWithTablePrefix() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when
        builder.addCriteria("t.name =", "John");
        // then
        Assertions.assertEquals(" WHERE  t.name = ?", builder.getSQLTemplate());
    }

    @Test
    void testColumnNameWithUnderscoreAndDash() {
        // given
        SQLBuilder builder = new SQLBuilder();
        // when
        builder.addCriteria("first_name =", "John");
        builder.addCriteria("last-name =", "Doe");
        // then
        String sql = builder.getSQLTemplate();
        Assertions.assertTrue(sql.contains("first_name = ?"));
        Assertions.assertTrue(sql.contains("last-name = ?"));
    }
}
