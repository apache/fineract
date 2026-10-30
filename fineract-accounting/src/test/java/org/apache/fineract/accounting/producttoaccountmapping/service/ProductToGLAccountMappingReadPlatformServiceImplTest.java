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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.data.ChargeSummaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductToGLAccountMappingReadPlatformServiceImplTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long CHARGE_ID = 7L;
    private static final int LOAN = PortfolioProductType.LOAN.getValue();

    @Mock
    private ProductToGLAccountMappingRepository productToGLAccountMappingRepository;

    @Mock
    private CodeValueMapper codeValueMapper;

    @Mock
    private AccountingChargeReadService accountingChargeReadService;

    @InjectMocks
    private ProductToGLAccountMappingReadPlatformServiceImpl readService;

    @Test
    void chargeMappingsAreNullWhenProductHasNone() {
        when(productToGLAccountMappingRepository.findAllFeeToIncomeAccountMappings(PRODUCT_ID, LOAN)).thenReturn(List.of());

        assertThat(readService.fetchFeeToGLAccountMappingsForLoanProduct(PRODUCT_ID)).isNull();
        verifyNoInteractions(accountingChargeReadService);
    }

    @Test
    void chargeMappingsExposeOnlyChargeIdNameAndPenalty() {
        final GLAccount incomeAccount = new GLAccount().setName("Penalty income").setGlCode("4002");
        incomeAccount.setId(20L);
        final ProductToGLAccountMapping mapping = new ProductToGLAccountMapping().setChargeId(CHARGE_ID).setGlAccount(incomeAccount);
        when(productToGLAccountMappingRepository.findAllPenaltyToIncomeAccountMappings(PRODUCT_ID, LOAN)).thenReturn(List.of(mapping));
        when(accountingChargeReadService.findChargesByIds(Set.of(CHARGE_ID)))
                .thenReturn(List.of(new ChargeSummaryData(CHARGE_ID, "Late payment", true)));

        final List<ChargeToGLAccountMapper> result = readService.fetchPenaltyToIncomeAccountMappingsForLoanProduct(PRODUCT_ID);

        assertThat(result).hasSize(1);
        final ChargeData charge = result.get(0).getCharge();
        assertThat(charge).usingRecursiveComparison()
                .isEqualTo(ChargeData.builder().id(CHARGE_ID).name("Late payment").penalty(true).build());
        assertThat(result.get(0).getIncomeAccount().getId()).isEqualTo(20L);
        assertThat(result.get(0).getIncomeAccount().getGlCode()).isEqualTo("4002");
    }
}
