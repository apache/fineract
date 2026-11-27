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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.exception.InputValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SqlIdentifierValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = { "id", "displayName", "journalEntry.id", "c.display_name", "id, name", "m.id,m.name", " id " })
    void acceptsColumnIdentifiers(String orderBy) {
        assertThat(SqlIdentifierValidator.validateOrderBy(orderBy)).isEqualTo(orderBy.trim());
    }

    @ParameterizedTest
    @ValueSource(strings = { "(select 1 from m_office)", "id; drop table m_appuser", "id desc", "1", "id--",
            "case when 1=1 then id else name end", "strpos(password,'a')", "id/*x*/", "a.b.c", "id,", "`id`" })
    void rejectsExpressions(String orderBy) {
        assertThatThrownBy(() -> SqlIdentifierValidator.validateOrderBy(orderBy)).isInstanceOf(InputValidationException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void blankOrderByAndSortOrderAreLeftAlone(String value) {
        assertThat(SqlIdentifierValidator.validateOrderBy(value)).isEqualTo(value);
        assertThat(SqlIdentifierValidator.validateSortOrder(value)).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = { "ASC", "desc", "Asc" })
    void acceptsSortDirections(String sortOrder) {
        assertThat(SqlIdentifierValidator.validateSortOrder(sortOrder)).isEqualTo(sortOrder);
    }

    @ParameterizedTest
    @ValueSource(strings = { "ASC, (select 1)", "desc nulls first", "1", "ASC;" })
    void rejectsAnythingButASortDirection(String sortOrder) {
        assertThatThrownBy(() -> SqlIdentifierValidator.validateSortOrder(sortOrder)).isInstanceOf(InputValidationException.class);
    }

    @Test
    void searchAndPaginationParametersOnlyReturnColumnIdentifiers() {
        final String orderBy = "(select 1 from m_office)";
        SearchParameters search = SearchParameters.builder().orderBy(orderBy).sortOrder("ASC").build();
        PaginationParameters pagination = PaginationParameters.builder().orderBy(orderBy).sortOrder("ASC").build();

        assertThatThrownBy(search::getOrderBy).isInstanceOf(InputValidationException.class);
        assertThatThrownBy(pagination::getOrderBy).isInstanceOf(InputValidationException.class);
        assertThatThrownBy(pagination::orderBySql).isInstanceOf(InputValidationException.class);
    }
}
