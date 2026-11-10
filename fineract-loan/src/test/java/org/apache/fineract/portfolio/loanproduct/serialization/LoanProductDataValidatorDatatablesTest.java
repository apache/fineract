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
package org.apache.fineract.portfolio.loanproduct.serialization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingHelper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanProductDataValidatorDatatablesTest {

    private static final String DATATABLES_ONLY_JSON = "{\"datatables\": []}";

    @Mock
    private LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    @Mock
    private AdvancedPaymentAllocationsJsonParser advancedPaymentAllocationsJsonParser;
    @Mock
    private AdvancedPaymentAllocationsValidator advancedPaymentAllocationsValidator;
    @Mock
    private ProductToGLAccountMappingHelper productToGLAccountMappingHelper;
    @Mock
    private LoanProduct loanProduct;

    private LoanProductDataValidator underTest;

    @BeforeEach
    void setUp() {
        underTest = new LoanProductDataValidator(new FromJsonHelper(), loanRepaymentScheduleTransactionProcessorFactory,
                advancedPaymentAllocationsJsonParser, advancedPaymentAllocationsValidator, productToGLAccountMappingHelper);
    }

    @Test
    void createAcceptsDatatablesParameter() {
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> underTest.validateForCreate(JsonCommand.from(DATATABLES_ONLY_JSON)));

        assertFalse(thrown instanceof UnsupportedParameterException,
                "datatables must be a supported parameter on loan product create, got: " + thrown);
    }

    @Test
    void updateStillRejectsDatatablesParameter() {
        assertThrows(UnsupportedParameterException.class,
                () -> underTest.validateForUpdate(JsonCommand.from(DATATABLES_ONLY_JSON), loanProduct));
    }
}
