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
package org.apache.fineract.portfolio.charge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.serialization.ChargeDefinitionCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Regression coverage for FINERACT-2752 (CBS-129): {@code updateCharge} used a plain {@code save()}, so an edited
 * charge (e.g. a changed "Income from Charge" GL account) wasn't flushed before the same request re-read the charge to
 * build the response, making the update look reverted/missing on an immediate re-fetch.
 */
class ChargeWritePlatformServiceJpaRepositoryImplTest {

    private static final long CHARGE_ID = 1L;

    private final PlatformSecurityContext context = mock(PlatformSecurityContext.class);
    private final ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer = mock(
            ChargeDefinitionCommandFromApiJsonDeserializer.class);
    private final ChargeRepository chargeRepository = mock(ChargeRepository.class);
    private final LoanProductRepository loanProductRepository = mock(LoanProductRepository.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final FineractEntityAccessUtil fineractEntityAccessUtil = mock(FineractEntityAccessUtil.class);
    private final GLAccountRepositoryWrapper glAccountRepository = mock(GLAccountRepositoryWrapper.class);
    private final TaxGroupRepositoryWrapper taxGroupRepository = mock(TaxGroupRepositoryWrapper.class);
    private final PaymentTypeRepository paymentTypeRepository = mock(PaymentTypeRepository.class);

    private final ChargeWritePlatformServiceJpaRepositoryImpl service = new ChargeWritePlatformServiceJpaRepositoryImpl(context,
            fromApiJsonDeserializer, chargeRepository, loanProductRepository, jdbcTemplate, fineractEntityAccessUtil, glAccountRepository,
            taxGroupRepository, paymentTypeRepository);

    @Test
    void updateCharge_flushesImmediately_soTheChangeIsVisibleOnAnImmediateReread() {
        final Charge chargeForUpdate = stubChargeWithChanges(Map.of("name", "Updated name"));

        final CommandProcessingResult result = service.updateCharge(CHARGE_ID, command("""
                { "locale": "en", "name": "Updated name" }
                """));

        verify(chargeRepository, times(1)).saveAndFlush(chargeForUpdate);
        verify(chargeRepository, never()).save(chargeForUpdate);
        assertEquals(CHARGE_ID, result.getResourceId());
    }

    @Test
    void updateCharge_withNoChanges_doesNotPersistAtAll() {
        stubChargeWithChanges(Map.of());

        service.updateCharge(CHARGE_ID, command("""
                { "locale": "en" }
                """));

        verify(chargeRepository, never()).saveAndFlush(any());
        verify(chargeRepository, never()).save(any());
    }

    private Charge stubChargeWithChanges(final Map<String, Object> changes) {
        final Charge chargeForUpdate = mock(Charge.class);
        when(chargeForUpdate.update(any())).thenReturn(changes);
        when(chargeForUpdate.getChargeTimeType()).thenReturn(ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        when(chargeForUpdate.getChargeCalculation()).thenReturn(ChargeCalculationType.FLAT.getValue());
        when(chargeRepository.findById(CHARGE_ID)).thenReturn(Optional.of(chargeForUpdate));
        return chargeForUpdate;
    }

    private JsonCommand command(final String json) {
        final FromJsonHelper fromJsonHelper = new FromJsonHelper();
        return new JsonCommand(CHARGE_ID, fromJsonHelper.parse(json), fromJsonHelper);
    }
}
