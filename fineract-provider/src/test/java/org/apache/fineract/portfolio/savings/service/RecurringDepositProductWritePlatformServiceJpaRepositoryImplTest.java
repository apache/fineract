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
package org.apache.fineract.portfolio.savings.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartAssembler;
import org.apache.fineract.portfolio.savings.data.DepositProductDataValidator;
import org.apache.fineract.portfolio.savings.domain.DepositProductAssembler;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositProduct;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecurringDepositProductWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private RecurringDepositProductRepository recurringDepositProductRepository;
    @Mock
    private DepositProductDataValidator dataValidator;
    @Mock
    private DepositProductAssembler depositProductAssembler;
    @Mock
    private ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;
    @Mock
    private InterestRateChartAssembler chartAssembler;
    @Mock
    private JsonCommand command;

    private RecurringDepositProductWritePlatformServiceJpaRepositoryImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new RecurringDepositProductWritePlatformServiceJpaRepositoryImpl(context, recurringDepositProductRepository,
                dataValidator, depositProductAssembler, accountMappingWritePlatformService, chartAssembler);
        when(command.json()).thenReturn("{}");
        when(command.stringValueOfParameterNamed("name")).thenReturn("Recurring A");
        when(command.stringValueOfParameterNamed("shortName")).thenReturn("RA");
        when(depositProductAssembler.assembleRecurringDepositProduct(command)).thenReturn(mock(RecurringDepositProduct.class));
    }

    @Test
    void createWithDuplicateNameOnPostgreSQLReportsDuplicateName() {
        failSaveWithPostgreSQLUniqueViolation("m_savings_product_name_key", "name", "Recurring A");

        PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class, () -> underTest.create(command));

        assertEquals("error.msg.savingsproduct.duplicate.name", exception.getGlobalisationMessageCode());
        assertEquals("name", exception.getParameterName());
    }

    @Test
    void createWithDuplicateShortNameOnPostgreSQLReportsDuplicateShortName() {
        failSaveWithPostgreSQLUniqueViolation("m_savings_product_short_name_key", "short_name", "RA");

        PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class, () -> underTest.create(command));

        assertEquals("error.msg.savingsproduct.duplicate.short.name", exception.getGlobalisationMessageCode());
        assertEquals("shortName", exception.getParameterName());
    }

    @Test
    void createWithDuplicateNameOnMySQLReportsDuplicateName() {
        when(recurringDepositProductRepository.saveAndFlush(any(RecurringDepositProduct.class))).thenThrow(new DataIntegrityViolationException(
                "could not execute", new SQLException("Duplicate entry 'Recurring A' for key 'sp_unq_name'", "23000")));

        PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class, () -> underTest.create(command));

        assertEquals("error.msg.savingsproduct.duplicate.name", exception.getGlobalisationMessageCode());
    }

    private void failSaveWithPostgreSQLUniqueViolation(String constraintName, String column, String value) {
        SQLException cause = new SQLException("ERROR: duplicate key value violates unique constraint \"" + constraintName
                + "\"\n  Detail: Key (" + column + ")=(" + value + ") already exists.", "23505");
        when(recurringDepositProductRepository.saveAndFlush(any(RecurringDepositProduct.class)))
                .thenThrow(new DataIntegrityViolationException("could not execute statement", cause));
    }
}
