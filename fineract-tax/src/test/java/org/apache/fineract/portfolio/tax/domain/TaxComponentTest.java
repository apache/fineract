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
package org.apache.fineract.portfolio.tax.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaxComponentTest {

    private final GLAccount creditAccount = account(4L);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 1))));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void movingAScheduledRateMovesTheEndOfThePreviousRate() {
        final TaxComponent taxComponent = component(LocalDate.of(2023, 1, 1));
        taxComponent.update(command("""
                {"percentage": 7, "startDate": "01 March 2024", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """));

        final Map<String, Object> changes = taxComponent.update(command("""
                {"startDate": "01 April 2024", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """));

        assertThat(changes).containsExactlyEntriesOf(Map.of(TaxApiConstants.startDateParamName, LocalDate.of(2024, 4, 1)));
        assertThat(taxComponent.startDate()).isEqualTo(LocalDate.of(2024, 4, 1));
        assertThat(taxComponent.getTaxComponentHistories()).singleElement()
                .satisfies(previousRate -> assertThat(previousRate.endDate()).isEqualTo(LocalDate.of(2024, 4, 1)));
        assertThat(taxComponent.getApplicablePercentage(LocalDate.of(2024, 3, 15))).isEqualByComparingTo("5");
        assertThat(taxComponent.getApplicablePercentage(LocalDate.of(2024, 4, 2))).isEqualByComparingTo("7");
    }

    @Test
    void movingAStartDateWithoutAPreviousRateOnlyMovesTheStartDate() {
        final TaxComponent taxComponent = component(LocalDate.of(2024, 3, 1));

        taxComponent.update(command("""
                {"startDate": "01 April 2024", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """));

        assertThat(taxComponent.startDate()).isEqualTo(LocalDate.of(2024, 4, 1));
        assertThat(taxComponent.getTaxComponentHistories()).isEmpty();
    }

    @Test
    void anUnchangedStartDateIsNotAChange() {
        final TaxComponent taxComponent = component(LocalDate.of(2023, 1, 1));

        final Map<String, Object> changes = taxComponent.update(command("""
                {"name": "VAT", "startDate": "01 January 2023", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """));

        assertThat(changes).isEmpty();
    }

    @Test
    void updatingAnAccountReportsOnlyTheFieldsThatChanged() {
        final TaxComponent taxComponent = component(LocalDate.of(2023, 1, 1));
        final GLAccount newCreditAccount = account(9L);

        final Map<String, Object> creditChanges = taxComponent.updateCreditAccount(GLAccountType.LIABILITY.getValue(), newCreditAccount);
        final Map<String, Object> debitChanges = taxComponent.updateDebitAccount(GLAccountType.ASSET.getValue(), account(3L));

        assertThat(creditChanges).containsExactlyEntriesOf(Map.of(TaxApiConstants.creditAccountIdParamName, 9L));
        assertThat(debitChanges).containsExactlyInAnyOrderEntriesOf(Map.of(TaxApiConstants.debitAccountTypeParamName,
                GLAccountType.ASSET.getValue(), TaxApiConstants.debitAccountIdParamName, 3L));
        assertThat(taxComponent.getCreditAccount()).isSameAs(newCreditAccount);
        assertThat(taxComponent.getDebitAccountType()).isEqualTo(GLAccountType.ASSET.getValue());
    }

    private TaxComponent component(final LocalDate startDate) {
        return TaxComponent.createTaxComponent("VAT", BigDecimal.valueOf(5), null, null, GLAccountType.LIABILITY, creditAccount, startDate);
    }

    private static GLAccount account(final Long id) {
        final GLAccount account = mock(GLAccount.class);
        when(account.getId()).thenReturn(id);
        return account;
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), "taxes", 1L, null, null, null, null, null, null,
                null, null, null, null, null, null);
    }
}
