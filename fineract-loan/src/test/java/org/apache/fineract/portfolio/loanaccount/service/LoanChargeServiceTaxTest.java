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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeTaxDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.service.ChargeTaxApplicationService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class LoanChargeServiceTaxTest {

    private static MockedStatic<MoneyHelper> moneyHelperMock;
    private static MockedStatic<DateUtils> dateUtilsMock;

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 4, 10);

    @BeforeAll
    static void setUpStatics() {
        moneyHelperMock = mockStatic(MoneyHelper.class);
        moneyHelperMock.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelperMock.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));

        dateUtilsMock = mockStatic(DateUtils.class);
        dateUtilsMock.when(DateUtils::getBusinessLocalDate).thenReturn(BUSINESS_DATE);
    }

    @AfterAll
    static void tearDownStatics() {
        moneyHelperMock.close();
        dateUtilsMock.close();
    }

    private LoanChargeService buildService(ChargeTaxApplicationService taxService) {
        return new LoanChargeService(mock(LoanChargeValidator.class), mock(LoanTransactionProcessingService.class),
                mock(LoanLifecycleStateMachine.class), mock(LoanBalanceService.class), mock(LoanScheduleGeneratorService.class),
                taxService);
    }

    @Test
    void populateDerivedFields_doesNotApplyTax_whenChargeHasNoTaxGroup() {
        ChargeTaxApplicationService taxService = mock(ChargeTaxApplicationService.class);
        LoanChargeService service = buildService(taxService);

        Charge charge = flatCharge(null /* no tax group */);
        LoanCharge loanCharge = loanCharge(charge, new BigDecimal("100.00"), null);

        service.populateDerivedFields(loanCharge, BigDecimal.ZERO, new BigDecimal("100.00"), null, BigDecimal.ZERO);

        assertThat(loanCharge.getTaxAmount()).isZero();
        assertThat(loanCharge.getTaxDetails()).isEmpty();
    }

    @Test
    void populateDerivedFields_doesNotInflateAmount_whenTaxGroupIsConfigured() {
        // base = 1000, tax = 160 (16 %) → amount stays 1000, taxAmount = 160
        TaxComponent component = mock(TaxComponent.class);
        TaxGroup taxGroup = mock(TaxGroup.class);

        ChargeTaxApplicationService taxService = mock(ChargeTaxApplicationService.class);
        when(taxService.computeTax(any(TaxGroup.class), any(BigDecimal.class), any(LocalDate.class), anyInt()))
                .thenReturn(Map.of(component, new BigDecimal("160.000000")));

        LoanChargeService service = buildService(taxService);

        Charge charge = flatCharge(taxGroup);
        LoanCharge loanCharge = loanCharge(charge, new BigDecimal("1000.00"), null);

        service.populateDerivedFields(loanCharge, BigDecimal.ZERO, new BigDecimal("1000.00"), null, BigDecimal.ZERO);

        assertThat(loanCharge.getTaxAmount()).isEqualByComparingTo(new BigDecimal("160.000000"));
        assertThat(loanCharge.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void populateDerivedFields_populatesTaxDetails_forEachTaxComponent() {
        TaxComponent comp1 = mock(TaxComponent.class);
        TaxComponent comp2 = mock(TaxComponent.class);
        TaxGroup taxGroup = mock(TaxGroup.class);

        ChargeTaxApplicationService taxService = mock(ChargeTaxApplicationService.class);
        when(taxService.computeTax(any(), any(), any(), anyInt()))
                .thenReturn(Map.of(comp1, new BigDecimal("10.000000"), comp2, new BigDecimal("5.000000")));

        LoanChargeService service = buildService(taxService);
        Charge charge = flatCharge(taxGroup);
        LoanCharge loanCharge = loanCharge(charge, new BigDecimal("100.00"), null);

        service.populateDerivedFields(loanCharge, BigDecimal.ZERO, new BigDecimal("100.00"), null, BigDecimal.ZERO);

        assertThat(loanCharge.getTaxDetails()).hasSize(2);
        assertThat(loanCharge.getTaxDetails()).extracting(LoanChargeTaxDetails::getTaxComponent).containsExactlyInAnyOrder(comp1, comp2);
    }

    @Test
    void populateDerivedFields_setsAmountOutstanding_fromOriginalAmount() {
        // base = 500, tax = 75 → amount stays 500, outstanding = 500 (tax is not added to amount)
        TaxComponent component = mock(TaxComponent.class);
        TaxGroup taxGroup = mock(TaxGroup.class);

        ChargeTaxApplicationService taxService = mock(ChargeTaxApplicationService.class);
        when(taxService.computeTax(any(), any(), any(), anyInt())).thenReturn(Map.of(component, new BigDecimal("75.000000")));

        LoanChargeService service = buildService(taxService);
        Charge charge = flatCharge(taxGroup);
        LoanCharge loanCharge = loanCharge(charge, new BigDecimal("500.00"), null);

        service.populateDerivedFields(loanCharge, BigDecimal.ZERO, new BigDecimal("500.00"), null, BigDecimal.ZERO);

        assertThat(loanCharge.getAmountOutstanding()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void populateDerivedFields_doesNotMutateCharge_whenComputedTaxIsZero() {
        TaxGroup taxGroup = mock(TaxGroup.class);

        ChargeTaxApplicationService taxService = mock(ChargeTaxApplicationService.class);
        when(taxService.computeTax(any(), any(), any(), anyInt())).thenReturn(Collections.emptyMap());

        LoanChargeService service = buildService(taxService);
        Charge charge = flatCharge(taxGroup);
        LoanCharge loanCharge = loanCharge(charge, new BigDecimal("200.00"), null);

        service.populateDerivedFields(loanCharge, BigDecimal.ZERO, new BigDecimal("200.00"), null, BigDecimal.ZERO);

        assertThat(loanCharge.getTaxAmount()).isZero();
        assertThat(loanCharge.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void populateDerivedFields_usesSubmittedOnDate_asEffectiveDateForTax() {
        LocalDate submittedOn = LocalDate.of(2026, 1, 15);
        TaxComponent component = mock(TaxComponent.class);
        TaxGroup taxGroup = mock(TaxGroup.class);

        ChargeTaxApplicationService taxService = mock(ChargeTaxApplicationService.class);
        when(taxService.computeTax(any(), any(), any(), anyInt())).thenReturn(Map.of(component, new BigDecimal("20.000000")));

        LoanChargeService service = buildService(taxService);
        Charge charge = flatCharge(taxGroup);
        LoanCharge loanCharge = loanCharge(charge, new BigDecimal("200.00"), submittedOn);

        service.populateDerivedFields(loanCharge, BigDecimal.ZERO, new BigDecimal("200.00"), null, BigDecimal.ZERO);

        org.mockito.Mockito.verify(taxService).computeTax(taxGroup, new BigDecimal("200.00"), submittedOn, 6);
    }

    @Test
    void populateDerivedFields_usesBusinessDate_whenSubmittedOnDateIsNull() {
        TaxComponent component = mock(TaxComponent.class);
        TaxGroup taxGroup = mock(TaxGroup.class);

        ChargeTaxApplicationService taxService = mock(ChargeTaxApplicationService.class);
        when(taxService.computeTax(any(), any(), any(), anyInt())).thenReturn(Map.of(component, new BigDecimal("10.000000")));

        LoanChargeService service = buildService(taxService);
        Charge charge = flatCharge(taxGroup);
        LoanCharge loanCharge = loanCharge(charge, new BigDecimal("100.00"), null /* no submittedOnDate */);

        service.populateDerivedFields(loanCharge, BigDecimal.ZERO, new BigDecimal("100.00"), null, BigDecimal.ZERO);

        org.mockito.Mockito.verify(taxService).computeTax(taxGroup, new BigDecimal("100.00"), BUSINESS_DATE, 6);
    }

    @Test
    void populateDerivedFields_clearsPreviousTaxDetails_onReapplication() {
        TaxComponent comp1 = mock(TaxComponent.class);
        TaxComponent comp2 = mock(TaxComponent.class);
        TaxGroup taxGroup = mock(TaxGroup.class);

        ChargeTaxApplicationService taxService = mock(ChargeTaxApplicationService.class);
        when(taxService.computeTax(any(), any(), any(), anyInt()))
                .thenReturn(Map.of(comp1, new BigDecimal("10.000000"), comp2, new BigDecimal("5.000000")))
                .thenReturn(Map.of(comp1, new BigDecimal("20.000000")));

        LoanChargeService service = buildService(taxService);
        Charge charge = flatCharge(taxGroup);
        LoanCharge loanCharge = loanCharge(charge, new BigDecimal("100.00"), null);

        service.populateDerivedFields(loanCharge, BigDecimal.ZERO, new BigDecimal("100.00"), null, BigDecimal.ZERO);
        assertThat(loanCharge.getTaxDetails()).hasSize(2);

        loanCharge.setAmount(new BigDecimal("100.00"));
        loanCharge.setAmountOutstanding(new BigDecimal("100.00"));

        service.populateDerivedFields(loanCharge, BigDecimal.ZERO, new BigDecimal("100.00"), null, BigDecimal.ZERO);
        assertThat(loanCharge.getTaxDetails()).hasSize(1);
        assertThat(loanCharge.getTaxDetails().get(0).getTaxComponent()).isEqualTo(comp1);
    }

    private Charge flatCharge(TaxGroup taxGroup) {
        Charge charge = mock(Charge.class);
        when(charge.getTaxGroup()).thenReturn(taxGroup);
        return charge;
    }

    private LoanCharge loanCharge(Charge charge, BigDecimal amount, LocalDate submittedOnDate) {
        LoanCharge lc = new LoanCharge();
        lc.setCharge(charge);
        lc.setAmount(amount);
        lc.setAmountOutstanding(amount);
        lc.setChargeCalculation(ChargeCalculationType.FLAT.getValue());
        lc.setChargeTime(ChargeTimeType.SPECIFIED_DUE_DATE.getValue());
        lc.setSubmittedOnDate(submittedOnDate);
        lc.setAmountOrPercentage(amount);
        return lc;
    }
}
