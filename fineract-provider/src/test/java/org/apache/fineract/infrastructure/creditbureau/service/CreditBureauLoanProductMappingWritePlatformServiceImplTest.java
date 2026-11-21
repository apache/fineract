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
package org.apache.fineract.infrastructure.creditbureau.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauLoanProductMapping;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauLoanProductMappingRepository;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureau;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureauRepository;
import org.apache.fineract.infrastructure.creditbureau.serialization.CreditBureauLoanProductCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditBureauLoanProductMappingWritePlatformServiceImplTest {

    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();
    private static final Long ORGANISATION_ID = 7L;
    private static final Long LOAN_PRODUCT_ID = 3L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private CreditBureauLoanProductMappingRepository mappingRepository;
    @Mock
    private OrganisationCreditBureauRepository organisationRepository;
    @Mock
    private LoanProductRepository loanProductRepository;
    @Mock
    private CreditBureauLoanProductCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    private CreditBureauLoanProductMappingWritePlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CreditBureauLoanProductMappingWritePlatformServiceImpl(context, mappingRepository, organisationRepository,
                loanProductRepository, fromApiJsonDeserializer);
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    private CreditBureauLoanProductMapping add(final String json) {
        service.addCreditBureauLoanProductMapping(ORGANISATION_ID, command(json));
        final ArgumentCaptor<CreditBureauLoanProductMapping> saved = ArgumentCaptor.forClass(CreditBureauLoanProductMapping.class);
        verify(mappingRepository).saveAndFlush(saved.capture());
        return saved.getValue();
    }

    @Test
    void addTakesAllValuesFromTheCommandAndLinksOrganisationAndLoanProduct() {
        final OrganisationCreditBureau organisation = new OrganisationCreditBureau();
        final LoanProduct loanProduct = mock(LoanProduct.class);
        when(organisationRepository.getReferenceById(ORGANISATION_ID)).thenReturn(organisation);
        when(loanProductRepository.getReferenceById(LOAN_PRODUCT_ID)).thenReturn(loanProduct);

        final CreditBureauLoanProductMapping saved = add("""
                {"loanProductId": 3, "isCreditcheckMandatory": true, "skipCreditcheckInFailure": true,
                 "stalePeriod": 30, "isActive": true, "locale": "en"}
                """);

        assertTrue(saved.isCreditCheckMandatory());
        assertTrue(saved.isSkipCreditCheckInFailure());
        assertEquals(30, saved.getStalePeriod());
        assertTrue(saved.isActive());
        assertSame(organisation, saved.getOrganisation_creditbureau());
        assertSame(loanProduct, saved.getLoanProduct());
    }

    @Test
    void addDefaultsToFalseAndMinusOneWhenTheParametersAreMissing() {
        final CreditBureauLoanProductMapping saved = add("""
                {"loanProductId": 3}
                """);

        assertFalse(saved.isCreditCheckMandatory());
        assertFalse(saved.isSkipCreditCheckInFailure());
        assertEquals(-1, saved.getStalePeriod());
        assertFalse(saved.isActive());
    }

    @Test
    void addKeepsAnExplicitFalseAndAStalePeriodOfZero() {
        final CreditBureauLoanProductMapping saved = add("""
                {"loanProductId": 3, "isCreditcheckMandatory": false, "skipCreditcheckInFailure": false,
                 "stalePeriod": 0, "isActive": false, "locale": "en"}
                """);

        assertFalse(saved.isCreditCheckMandatory());
        assertFalse(saved.isSkipCreditCheckInFailure());
        assertEquals(0, saved.getStalePeriod());
        assertFalse(saved.isActive());
    }
}
