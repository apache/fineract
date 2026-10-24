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
package org.apache.fineract.portfolio.tax.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxComponentRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.apache.fineract.portfolio.tax.mapper.TaxComponentMapper;
import org.apache.fineract.portfolio.tax.mapper.TaxGroupMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TaxReadPlatformServiceImplTest {

    @Mock
    private TaxComponentRepositoryWrapper taxComponentRepositoryWrapper;
    @Mock
    private TaxGroupRepositoryWrapper taxGroupRepositoryWrapper;
    @Mock
    private TaxGroupRepository taxGroupRepository;
    @Mock
    private AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    @Mock
    private TaxComponentMapper taxComponentMapper;
    @Mock
    private TaxGroupMapper taxGroupMapper;
    @Mock
    private ChargeRepository chargeRepository;

    @InjectMocks
    private TaxReadPlatformServiceImpl underTest;

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 10);
    private TaxComponent taxComponent;
    private static final Long COMPONENT_ID = 1L;

    @BeforeEach
    void setUp() {
        taxComponent = TaxComponent.createTaxComponent("TC1", BigDecimal.TEN, GLAccountType.ASSET, null, GLAccountType.LIABILITY, null,
                TODAY);
        ReflectionTestUtils.setField(taxComponent, "id", COMPONENT_ID);
    }

    @Test
    void testRetrieveTaxComponentWhenNotInUse() {
        TaxComponentData baseData = TaxComponentData.lookup(COMPONENT_ID, "TC1");
        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);
        when(taxComponentMapper.map(taxComponent)).thenReturn(baseData);

        Map<String, List<GLAccountData>> accountOptions = Collections.emptyMap();
        List<EnumOptionData> glAccountTypeOptions = Collections.emptyList();
        when(accountingDropdownReadPlatformService.retrieveAccountMappingOptions()).thenReturn(accountOptions);
        when(accountingDropdownReadPlatformService.retrieveGLAccountTypeOptions()).thenReturn(glAccountTypeOptions);

        TaxComponentData result = underTest.retrieveTaxComponentData(COMPONENT_ID);

        assertNotNull(result);
        assertTrue(result.getAccountsEditable());
        assertNotNull(result.getGlAccountOptions());
        assertNotNull(result.getGlAccountTypeOptions());
        verify(accountingDropdownReadPlatformService).retrieveAccountMappingOptions();
        verify(accountingDropdownReadPlatformService).retrieveGLAccountTypeOptions();
    }

    @Test
    void testRetrieveTaxComponentWhenInUse() {
        TaxGroupMappings mapping = TaxGroupMappings.createTaxGroupMappings(taxComponent, TODAY);
        taxComponent.getTaxGroupMappings().add(mapping);

        TaxComponentData baseData = TaxComponentData.lookup(COMPONENT_ID, "TC1");
        when(taxComponentRepositoryWrapper.findOneWithNotFoundDetection(COMPONENT_ID)).thenReturn(taxComponent);
        when(taxComponentMapper.map(taxComponent)).thenReturn(baseData);
        when(chargeRepository.existsByTaxGroupContainingTaxComponent(COMPONENT_ID)).thenReturn(true);

        TaxComponentData result = underTest.retrieveTaxComponentData(COMPONENT_ID);

        assertNotNull(result);
        assertFalse(result.getAccountsEditable());
        assertNull(result.getGlAccountOptions());
        assertNull(result.getGlAccountTypeOptions());
        verify(accountingDropdownReadPlatformService, never()).retrieveAccountMappingOptions();
        verify(accountingDropdownReadPlatformService, never()).retrieveGLAccountTypeOptions();
    }
}
