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
package org.apache.fineract.portfolio.workingcapitalloan.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedPayment;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationSchedulePaymentData;
import org.springframework.stereotype.Component;

@Component
public class ProjectedAmortizationScheduleMapper {

    private static final int DISPLAY_SCALE = 2;
    private static final RoundingMode DISPLAY_ROUNDING = RoundingMode.HALF_UP;

    public ProjectedAmortizationScheduleData toData(final ProjectedAmortizationScheduleModel model) {
        final List<ProjectedAmortizationSchedulePaymentData> paymentDataList = model.payments().stream().map(this::toPaymentData).toList();

        return ProjectedAmortizationScheduleData.builder() //
                .originationFeeAmount(roundMoney(model.originationFeeAmount())) //
                .netDisbursementAmount(roundMoney(model.netDisbursementAmount())) //
                .totalPaymentValue(roundMoney(model.totalPaymentValue())) //
                .periodPaymentRate(model.periodPaymentRate()) //
                .npvDayCount(model.npvDayCount()) //
                .expectedDisbursementDate(model.expectedDisbursementDate()) //
                .expectedPaymentAmount(roundMoney(model.expectedPaymentAmount())) //
                .loanTerm(model.loanTerm()) //
                .effectiveInterestRate(model.effectiveInterestRate()) //
                .payments(paymentDataList) //
                .build();
    }

    private ProjectedAmortizationSchedulePaymentData toPaymentData(final ProjectedPayment payment) {
        return ProjectedAmortizationSchedulePaymentData.builder() //
                .paymentNo(payment.paymentNo()) //
                .paymentDate(payment.date()) //
                .count(payment.count()) //
                .paymentsLeft(payment.paymentsLeft()) //
                .expectedPaymentAmount(roundMoney(payment.expectedPaymentAmount())) //
                .forecastPaymentAmount(roundMoney(payment.forecastPaymentAmount())) //
                .discountFactor(payment.discountFactor()) //
                .npvValue(roundMoney(payment.npvValue())) //
                .balance(roundMoney(payment.balance())) //
                .expectedAmortizationAmount(roundMoney(payment.expectedAmortizationAmount())) //
                .netAmortizationAmount(roundMoney(payment.netAmortizationAmount())) //
                .actualPaymentAmount(roundMoney(payment.actualPaymentAmount())) //
                .actualAmortizationAmount(roundMoney(payment.actualAmortizationAmount())) //
                .incomeModification(roundMoney(payment.incomeModification())) //
                .deferredBalance(roundMoney(payment.deferredBalance())) //
                .build();
    }

    private static BigDecimal roundMoney(final Money value) {
        return value != null ? value.getAmount().setScale(DISPLAY_SCALE, DISPLAY_ROUNDING) : null;
    }
}
