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
package org.apache.fineract.infrastructure.codes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.util.List;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.codes.exception.CodeValueNotFoundException;
import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.query.QMCode;
import org.apache.fineract.query.QMCodeValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class CodeValueReadPlatformServiceTest {

    // Equal to the service's own "cv" and "c" alias: QueryDSL paths compare by alias and column name.
    private static final QMCode code = new QMCode("c");
    private static final QMCodeValue cv = new QMCodeValue("cv");

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SQLQueryFactory queryFactory;

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private CodeValueRepository codeValueRepository;

    @Mock
    private CodeValueMapper codeValueMapper;

    private CodeValueReadPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CodeValueReadPlatformServiceImpl(jdbcTemplate, queryFactory, context, codeValueRepository, codeValueMapper);
    }

    @Test
    void retrieveCodeValueMapsEveryProperty() {
        givenRows(row(1, "Passport", null, 1, true, false));

        CodeValueData result = service.retrieveCodeValue(1L);

        assertEquals(1L, result.getId());
        assertEquals("Passport", result.getName());
        assertNull(result.getDescription());
        assertEquals(1, result.getPosition());
        assertTrue(result.getActive());
        assertFalse(result.getMandatory());
    }

    @Test
    void retrieveCodeValueThrowsValueNotFound() {
        givenRows();
        assertThrows(CodeValueNotFoundException.class, () -> service.retrieveCodeValue(1L));
    }

    @Test
    void retrieveCodeValueReadsCorrectCodeValue() {
        SQLQuery<Tuple> query = givenRows(row(1, "Passport", null, 1, true, false));

        service.retrieveCodeValue(1L);
        InOrder order = inOrder(context, queryFactory);

        order.verify(context).authenticatedUser();
        order.verify(queryFactory).select(cv.id, cv.codeValue, cv.codeDescription, cv.orderPosition, cv.isActive, cv.isMandatory);
        verify(query).from(cv);
        verify(query).innerJoin(code);
        verify(query).on(cv.codeId.eq(code.id));
        verify(query).where(cv.id.eq(1));
    }

    @SuppressWarnings("unchecked")
    private SQLQuery<Tuple> givenRows(Tuple... rows) {
        // RETURNS_SELF makes from/innerJoin/on/where return the same mock, so only fetchOne() and fetch() need
        // stubbing.
        SQLQuery<Tuple> query = mock(SQLQuery.class, RETURNS_SELF);
        when(queryFactory.select(cv.id, cv.codeValue, cv.codeDescription, cv.orderPosition, cv.isActive, cv.isMandatory)).thenReturn(query);
        lenient().when(query.fetchOne()).thenReturn(rows.length == 0 ? null : rows[0]);
        lenient().when(query.fetch()).thenReturn(List.of(rows));
        return query;
    }

    private static Tuple row(Integer id, String codeValue, String codeDescription, Integer orderPosition, Boolean isActive,
            Boolean isMandatory) {
        return Projections.tuple(cv.id, cv.codeValue, cv.codeDescription, cv.orderPosition, cv.isActive, cv.isMandatory).newInstance(id,
                codeValue, codeDescription, orderPosition, isActive, isMandatory);
    }
}
