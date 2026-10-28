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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanProductUpdateUtilTest {

    @Mock
    private LoanProductRelatedDetailUpdateUtil detailUpdateUtil;
    @Mock
    private LoanProductMinMaxConstraintsUpdateUtil minMaxConstraintsUpdateUtil;
    @Mock
    private LoanProductFloatingRatesUpdateUtil floatingRatesUpdateUtil;
    @Mock
    private LoanProductVariableInstallmentConfigUpdateUtil variableInstallmentConfigUpdateUtil;
    @Mock
    private LoanProductTrancheDetailsUpdateUtil trancheDetailsUpdateUtil;
    @Mock
    private LoanProductInterestRecalculationDetailsAssembler interestRecalculationDetailsAssembler;
    @Mock
    private LoanProductInterestRecalculationDetailsUpdateUtil interestRecalculationDetailsUpdateUtil;
    @Mock
    private LoanProductGuaranteeDetailsAssembler guaranteeDetailsAssembler;
    @Mock
    private LoanProductGuaranteeDetailsUpdateUtil guaranteeDetailsUpdateUtil;

    private LoanProductUpdateUtil underTest;
    private LoanProduct loanProduct;

    @BeforeEach
    void setUp() {
        underTest = new LoanProductUpdateUtil(detailUpdateUtil, minMaxConstraintsUpdateUtil, floatingRatesUpdateUtil,
                variableInstallmentConfigUpdateUtil, trancheDetailsUpdateUtil, interestRecalculationDetailsAssembler,
                interestRecalculationDetailsUpdateUtil, guaranteeDetailsAssembler, guaranteeDetailsUpdateUtil);
        when(detailUpdateUtil.updateLoanRepaymentSchedule(any(), any(), any())).thenReturn(new HashMap<>());
        when(minMaxConstraintsUpdateUtil.update(any(), any())).thenReturn(new HashMap<>());
        loanProduct = mock(LoanProduct.class, Answers.RETURNS_DEEP_STUBS);
        when(loanProduct.getExternalId()).thenReturn(new ExternalId("old-ext"));
        when(loanProduct.getAccountingRule()).thenReturn(null);
    }

    @Test
    void externalIdChangeIsReportedAsExternalIdNotAsAccountingRule() {
        final Map<String, Object> changes = underTest.update(loanProduct, command("{\"externalId\": \"new-ext\"}"), null, null);

        assertEquals(new ExternalId("new-ext"), changes.get("externalId"));
        // "accountingRule" in the changes makes the write service delete and recreate all GL account mappings.
        assertFalse(changes.containsKey("accountingRule"));
        verify(loanProduct).setExternalId(new ExternalId("new-ext"));
    }

    @Test
    void sameExternalIdIsNotReportedAsAChange() {
        final Map<String, Object> changes = underTest.update(loanProduct, command("{\"externalId\": \"old-ext\"}"), null, null);

        assertFalse(changes.containsKey("externalId"));
        assertFalse(changes.containsKey("accountingRule"));
        verify(loanProduct, never()).setExternalId(any());
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.fromJsonElement(1L, JsonParser.parseString(json), new FromJsonHelper());
    }
}
