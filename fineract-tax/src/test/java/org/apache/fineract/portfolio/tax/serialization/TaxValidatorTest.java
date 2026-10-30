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
package org.apache.fineract.portfolio.tax.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaxValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2024, 1, 1);
    private static final String LOCKED = ".cannot.be.modified.for.linked.tax.component";

    private final TaxValidator validator = new TaxValidator(new FromJsonHelper());
    private final GLAccount creditAccount = account(4L);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, TODAY)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void linkedComponentAcceptsTheWholeFormWithOnlyTheNameChanged() {
        final TaxComponent taxComponent = component(LocalDate.of(2023, 1, 1));

        assertDoesNotThrow(() -> validator.validateTaxComponentChanges(taxComponent, command("""
                {"name": "renamed", "percentage": 5, "startDate": "01 January 2023", "creditAccountType": 2, "creditAccountId": 4,
                 "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """), true));
    }

    @Test
    void linkedComponentRejectsAPercentageChange() {
        final TaxComponent taxComponent = component(LocalDate.of(2023, 1, 1));

        assertThat(rejectionCodes(taxComponent, """
                {"name": "renamed", "percentage": 7, "locale": "en"}
                """, true)).containsExactly("validation.msg.tax.component.percentage" + LOCKED);
    }

    @Test
    void linkedComponentRejectsOnlyTheChangedAccountFields() {
        final TaxComponent taxComponent = component(LocalDate.of(2023, 1, 1));

        assertThat(rejectionCodes(taxComponent, """
                {"creditAccountType": 2, "creditAccountId": 9, "debitAccountType": 1, "debitAccountId": 3}
                """, true)).containsExactly("validation.msg.tax.component.debitAccountType" + LOCKED,
                "validation.msg.tax.component.debitAccountId" + LOCKED, "validation.msg.tax.component.creditAccountId" + LOCKED);
    }

    @Test
    void linkedComponentRejectsAStartDateChangeEvenBeforeItTakesEffect() {
        final TaxComponent taxComponent = component(LocalDate.of(2024, 3, 1));

        assertThat(rejectionCodes(taxComponent, """
                {"startDate": "01 April 2024", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """, true)).containsExactly("validation.msg.tax.component.startDate" + LOCKED);
    }

    @Test
    void unlinkedComponentAcceptsAPercentageChangeFromToday() {
        final TaxComponent taxComponent = component(LocalDate.of(2023, 1, 1));

        assertDoesNotThrow(() -> validator.validateTaxComponentChanges(taxComponent, command("""
                {"percentage": 7, "startDate": "01 January 2024", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """), false));
    }

    @Test
    void unlinkedComponentRejectsAPercentageChangeStartingBeforeToday() {
        final TaxComponent taxComponent = component(LocalDate.of(2023, 1, 1));

        assertThat(rejectionCodes(taxComponent, """
                {"percentage": 7, "startDate": "31 December 2023", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """, false)).containsExactly("validation.msg.tax.component.startDate.is.less.than.date");
    }

    @Test
    void unlinkedComponentIgnoresAnUnchangedPastStartDate() {
        final TaxComponent taxComponent = component(LocalDate.of(2023, 1, 1));

        assertDoesNotThrow(() -> validator.validateTaxComponentChanges(taxComponent, command("""
                {"name": "renamed", "startDate": "01 January 2023", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """), false));
    }

    @Test
    void unlinkedComponentMovesAStartDateThatHasNotTakenEffect() {
        final TaxComponent taxComponent = component(LocalDate.of(2024, 3, 1));

        assertDoesNotThrow(() -> validator.validateTaxComponentChanges(taxComponent, command("""
                {"startDate": "01 April 2024", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """), false));
    }

    @Test
    void unlinkedComponentRejectsMovingAStartDateThatHasTakenEffect() {
        final TaxComponent taxComponent = component(LocalDate.of(2023, 1, 1));

        assertThat(rejectionCodes(taxComponent, """
                {"startDate": "01 April 2024", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """, false)).containsExactly("validation.msg.tax.component.startDate.cannot.be.modified.after.activation");
    }

    @Test
    void unlinkedComponentRejectsMovingAStartDateBeforeToday() {
        final TaxComponent taxComponent = component(LocalDate.of(2024, 3, 1));

        assertThat(rejectionCodes(taxComponent, """
                {"startDate": "31 December 2023", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """, false)).containsExactly("validation.msg.tax.component.startDate.is.less.than.date");
    }

    @Test
    void unlinkedComponentRejectsMovingAScheduledRateOntoThePreviousRate() {
        final TaxComponent taxComponent = component(LocalDate.of(2024, 2, 1));
        taxComponent.update(command("""
                {"percentage": 7, "startDate": "01 March 2024", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """));

        assertThat(rejectionCodes(taxComponent, """
                {"startDate": "01 February 2024", "dateFormat": "dd MMMM yyyy", "locale": "en"}
                """, false)).containsExactly("validation.msg.tax.component.startDate.must.be.after.previous.rate.start.date");
    }

    @Test
    void updateRejectsAnUnknownAccountType() {
        final PlatformApiDataValidationException rejection = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForTaxComponentUpdate("""
                        {"debitAccountType": 9, "debitAccountId": 3}
                        """));

        assertThat(codesOf(rejection)).containsExactly("validation.msg.tax.component.debitAccountType.is.not.one.of.expected.enumerations");
    }

    @Test
    void updateRejectsAnAccountWithoutItsType() {
        final PlatformApiDataValidationException rejection = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForTaxComponentUpdate("""
                        {"creditAccountId": 4}
                        """));

        assertThat(codesOf(rejection)).containsExactly("validation.msg.tax.component.creditAccountType.cannot.be.blank");
    }

    @Test
    void updateAcceptsAccountFields() {
        assertDoesNotThrow(() -> validator.validateForTaxComponentUpdate("""
                {"debitAccountType": 1, "debitAccountId": 3, "creditAccountType": 2, "creditAccountId": 4}
                """));
    }

    private TaxComponent component(final LocalDate startDate) {
        return TaxComponent.createTaxComponent("VAT", BigDecimal.valueOf(5), null, null, GLAccountType.LIABILITY, creditAccount, startDate);
    }

    private List<String> rejectionCodes(final TaxComponent taxComponent, final String json, final boolean linked) {
        return codesOf(assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateTaxComponentChanges(taxComponent, command(json), linked)));
    }

    private static List<String> codesOf(final PlatformApiDataValidationException rejection) {
        return rejection.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList();
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
