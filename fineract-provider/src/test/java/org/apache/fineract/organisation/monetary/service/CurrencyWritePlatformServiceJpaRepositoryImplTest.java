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
package org.apache.fineract.organisation.monetary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyUpdateRequest;
import org.apache.fineract.organisation.monetary.data.CurrencyUpdateResponse;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrency;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepository;
import org.apache.fineract.organisation.monetary.exception.CurrencyInUseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CurrencyWritePlatformServiceJpaRepositoryImplTest {

    @InjectMocks
    private CurrencyWritePlatformServiceJpaRepositoryImpl underTest;

    @Mock
    private ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;

    @Mock
    private OrganisationCurrencyRepository organisationCurrencyRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private static final String KEPT_CODE = "USD";
    private static final String REMOVED_CODE = "EUR";

    private OrganisationCurrency organisationCurrency(final String code) {
        return new OrganisationCurrency(code, code, 2, 100, code, code);
    }

    @Test
    public void shouldRemoveCurrencyWhenNotUsedInAnyExistingData() {
        given(applicationCurrencyRepository.findOneWithNotFoundDetection(KEPT_CODE))
                .willReturn(new ApplicationCurrency(KEPT_CODE, KEPT_CODE, 2, 100, KEPT_CODE, KEPT_CODE));
        given(organisationCurrencyRepository.findAll())
                .willReturn(List.of(organisationCurrency(KEPT_CODE), organisationCurrency(REMOVED_CODE)));
        given(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).willReturn(0);

        final CurrencyUpdateResponse result = underTest
                .updateAllowedCurrencies(CurrencyUpdateRequest.builder().currencies(List.of(KEPT_CODE)).build());

        assertEquals(List.of(KEPT_CODE), result.getCurrencies());
        verify(organisationCurrencyRepository).deleteAll();
        verify(organisationCurrencyRepository).saveAll(any());
    }

    @Test
    public void shouldThrowCurrencyInUseExceptionWhenRemovedCurrencyIsStillReferenced() {
        given(applicationCurrencyRepository.findOneWithNotFoundDetection(KEPT_CODE))
                .willReturn(new ApplicationCurrency(KEPT_CODE, KEPT_CODE, 2, 100, KEPT_CODE, KEPT_CODE));
        given(organisationCurrencyRepository.findAll())
                .willReturn(List.of(organisationCurrency(KEPT_CODE), organisationCurrency(REMOVED_CODE)));
        // simulates the removed currency still being referenced by one of the checked tables (e.g. m_loan)
        given(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).willReturn(1);

        assertThrows(CurrencyInUseException.class,
                () -> underTest.updateAllowedCurrencies(CurrencyUpdateRequest.builder().currencies(List.of(KEPT_CODE)).build()));

        verify(organisationCurrencyRepository, never()).deleteAll();
        verify(organisationCurrencyRepository, never()).saveAll(any());
    }

    @Test
    public void shouldNotCheckUsageForCurrenciesThatRemainAllowed() {
        given(applicationCurrencyRepository.findOneWithNotFoundDetection(KEPT_CODE))
                .willReturn(new ApplicationCurrency(KEPT_CODE, KEPT_CODE, 2, 100, KEPT_CODE, KEPT_CODE));
        given(organisationCurrencyRepository.findAll()).willReturn(List.of(organisationCurrency(KEPT_CODE)));

        underTest.updateAllowedCurrencies(CurrencyUpdateRequest.builder().currencies(List.of(KEPT_CODE)).build());

        verify(jdbcTemplate, never()).queryForObject(anyString(), eq(Integer.class), any(Object[].class));
        verify(organisationCurrencyRepository).deleteAll();
    }
}
