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
package org.apache.fineract.accounting.producttoaccountmapping.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.exception.ResourceNotFoundException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductToGLAccountMappingHelperTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long CHARGE_ID = 999999L;
    private static final Long INCOME_ACCOUNT_ID = 20L;

    @Mock
    private GLAccountRepository accountRepository;

    @Mock
    private ProductToGLAccountMappingRepository accountMappingRepository;

    @Mock
    private AccountingChargeReadService accountingChargeReadService;

    @Mock
    private GLAccountRepositoryWrapper accountRepositoryWrapper;

    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @Mock
    private CodeValueRepository codeValueRepository;

    private ProductToGLAccountMappingHelper helper;

    @BeforeEach
    void setUp() {
        helper = new ProductToGLAccountMappingHelper(accountRepository, accountMappingRepository, new FromJsonHelper(),
                accountingChargeReadService, accountRepositoryWrapper, paymentTypeRepository, codeValueRepository);
    }

    /**
     * Every caller of saveChargesToGLAccountMappings, including the working capital loan product path, must reject an
     * unknown charge before anything is written.
     */
    @Test
    void saveChargesToGLAccountMappingsRejectsUnknownChargeBeforeSaving() {
        final ResourceNotFoundException chargeNotFound = new ResourceNotFoundException("error.msg.charge.id.invalid",
                "Charge with identifier " + CHARGE_ID + " does not exist", new Object[] { CHARGE_ID });
        doThrow(chargeNotFound).when(accountingChargeReadService).validateChargeForMapping(CHARGE_ID);

        assertThatThrownBy(() -> helper.saveChargesToGLAccountMappings(null, feeToIncomeAccountMappings(), PRODUCT_ID, null,
                PortfolioProductType.WORKING_CAPITAL_LOAN, false)).isSameAs(chargeNotFound);

        verifyNoInteractions(accountRepositoryWrapper, accountMappingRepository);
    }

    @Test
    void saveChargesToGLAccountMappingsStoresChargeIdOfValidatedCharge() {
        when(accountRepositoryWrapper.findOneWithNotFoundDetection(INCOME_ACCOUNT_ID))
                .thenReturn(new GLAccount().setType(GLAccountType.INCOME.getValue()));

        helper.saveChargesToGLAccountMappings(null, feeToIncomeAccountMappings(), PRODUCT_ID, null,
                PortfolioProductType.WORKING_CAPITAL_LOAN, false);

        verify(accountingChargeReadService).validateChargeForMapping(CHARGE_ID);
        final ArgumentCaptor<ProductToGLAccountMapping> saved = ArgumentCaptor.forClass(ProductToGLAccountMapping.class);
        verify(accountMappingRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getChargeId()).isEqualTo(CHARGE_ID);
        assertThat(saved.getValue().getFinancialAccountType()).isEqualTo(CashAccountsForLoan.INCOME_FROM_FEES.getValue());
    }

    private static JsonElement feeToIncomeAccountMappings() {
        return JsonParser.parseString(
                "{\"feeToIncomeAccountMappings\": [{\"chargeId\": " + CHARGE_ID + ", \"incomeAccountId\": " + INCOME_ACCOUNT_ID + "}]}");
    }
}
