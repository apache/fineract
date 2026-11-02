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
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.SavingsProductDataValidator;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsProductAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
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
class SavingsProductWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private SavingsProductRepository savingsProductRepository;
    @Mock
    private SavingsProductDataValidator dataValidator;
    @Mock
    private SavingsProductAssembler savingsProductAssembler;
    @Mock
    private ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;
    @Mock
    private FineractEntityAccessUtil fineractEntityAccessUtil;
    @Mock
    private JsonCommand command;

    private SavingsProductWritePlatformServiceJpaRepositoryImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new SavingsProductWritePlatformServiceJpaRepositoryImpl(context, savingsProductRepository, dataValidator,
                savingsProductAssembler, accountMappingWritePlatformService, fineractEntityAccessUtil);
        when(command.json()).thenReturn("{}");
        when(command.stringValueOfParameterNamed("name")).thenReturn("Savings A");
        when(command.stringValueOfParameterNamed("shortName")).thenReturn("SA");
        when(savingsProductAssembler.assemble(command)).thenReturn(mock(SavingsProduct.class));
    }

    @Test
    void createWithDuplicateNameOnPostgreSQLReportsDuplicateName() {
        failSaveWithPostgreSQLUniqueViolation("m_savings_product_name_key", "name", "Savings A");

        PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class, () -> underTest.create(command));

        assertEquals("error.msg.savingsproduct.duplicate.name", exception.getGlobalisationMessageCode());
        assertEquals("name", exception.getParameterName());
    }

    @Test
    void createWithDuplicateShortNameOnPostgreSQLReportsDuplicateShortName() {
        failSaveWithPostgreSQLUniqueViolation("m_savings_product_short_name_key", "short_name", "SA");

        PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class, () -> underTest.create(command));

        assertEquals("error.msg.savingsproduct.duplicate.short.name", exception.getGlobalisationMessageCode());
        assertEquals("shortName", exception.getParameterName());
    }

    @Test
    void createWithDuplicateNameOnMySQLReportsDuplicateName() {
        when(savingsProductRepository.saveAndFlush(any(SavingsProduct.class))).thenThrow(new DataIntegrityViolationException("could not execute",
                new SQLException("Duplicate entry 'Savings A' for key 'sp_unq_name'", "23000")));

        PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class, () -> underTest.create(command));

        assertEquals("error.msg.savingsproduct.duplicate.name", exception.getGlobalisationMessageCode());
    }

    private void failSaveWithPostgreSQLUniqueViolation(String constraintName, String column, String value) {
        SQLException cause = new SQLException("ERROR: duplicate key value violates unique constraint \"" + constraintName
                + "\"\n  Detail: Key (" + column + ")=(" + value + ") already exists.", "23505");
        when(savingsProductRepository.saveAndFlush(any(SavingsProduct.class)))
                .thenThrow(new DataIntegrityViolationException("could not execute statement", cause));
    }
}
