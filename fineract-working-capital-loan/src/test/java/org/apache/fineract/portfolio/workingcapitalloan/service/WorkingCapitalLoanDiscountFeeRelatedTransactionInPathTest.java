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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanDiscountFeeRelatedTransactionInPathTest {

    private static final Long LOAN_ID = 1L;
    private static final Long RELATED_TRANSACTION_ID = 2L;

    private final JsonCommand commandWithRelatedResourceId = JsonCommand.fromJsonElement(LOAN_ID,
            new FromJsonHelper().parse("{\"relatedResourceId\":2}"));

    @Mock
    private WorkingCapitalLoanRepository loanRepository;

    @Spy
    private WorkingCapitalLoanDataValidator validator = new WorkingCapitalLoanDataValidator(new FromJsonHelper(), null, null, null, null,
            null, null);

    @InjectMocks
    private WorkingCapitalLoanWritePlatformServiceImpl service;

    @BeforeEach
    void loanExists() {
        when(loanRepository.findById(LOAN_ID)).thenReturn(Optional.of(new WorkingCapitalLoan()));
    }

    @Test
    void discountFeeRejectsRelatedResourceIdInBodyWhenThePathNamesTheDisbursement() {
        final UnsupportedParameterException exception = assertThrows(UnsupportedParameterException.class,
                () -> service.makeDiscountFee(LOAN_ID, RELATED_TRANSACTION_ID, commandWithRelatedResourceId));

        assertThat(exception.getUnsupportedParameters()).containsExactly(WorkingCapitalLoanConstants.relatedResourceIdParamName);
    }

    @Test
    void discountFeeAdjustmentRejectsRelatedResourceIdInBodyWhenThePathNamesTheDiscountFee() {
        final UnsupportedParameterException exception = assertThrows(UnsupportedParameterException.class,
                () -> service.makeDiscountFeeAdjustment(LOAN_ID, RELATED_TRANSACTION_ID, commandWithRelatedResourceId));

        assertThat(exception.getUnsupportedParameters()).containsExactly(WorkingCapitalLoanConstants.relatedResourceIdParamName);
    }
}
