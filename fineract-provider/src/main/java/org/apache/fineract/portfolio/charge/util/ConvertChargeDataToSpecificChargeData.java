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
package org.apache.fineract.portfolio.charge.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountChargeData;

public final class ConvertChargeDataToSpecificChargeData {

    private ConvertChargeDataToSpecificChargeData() {}

    public static LoanChargeData toLoanChargeData(final ChargeData chargeData) {

        BigDecimal percentage = null;
        if (chargeData.getChargeCalculationType().getId() == 2) {
            percentage = chargeData.getAmount();
        }

        return LoanChargeData.newLoanChargeDetails(chargeData.getId(), chargeData.getName(), chargeData.getCurrency(),
                chargeData.getAmount(), percentage, chargeData.getChargeTimeType(), chargeData.getChargeCalculationType(),
                chargeData.isPenalty(), chargeData.getChargePaymentMode(), chargeData.getMinCap(), chargeData.getMaxCap(),
                ExternalId.empty());
    }

    public static SavingsAccountChargeData toSavingsAccountChargeData(final ChargeData chargeData) {

        final Long savingsChargeId = null;
        final Long savingsAccountId = null;
        final BigDecimal amountPaid = BigDecimal.ZERO;
        final BigDecimal amountWaived = BigDecimal.ZERO;
        final BigDecimal amountWrittenOff = BigDecimal.ZERO;
        final BigDecimal amountOutstanding = BigDecimal.ZERO;
        final BigDecimal percentage = BigDecimal.ZERO;
        final BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        final Collection<ChargeData> chargeOptions = null;
        final LocalDate dueAsOfDate = null;
        final Boolean isActive = null;
        final Boolean isFreeWithdrawal = null;
        final Integer freeWithdrawalChargeFrequency = null;
        final Integer restartFrequency = null;
        final Integer restartFrequencyEnum = null;

        final LocalDate inactivationDate = null;

        return SavingsAccountChargeData.instance(savingsChargeId, chargeData.getId(), savingsAccountId, chargeData.getName(),
                chargeData.getCurrency(), chargeData.getAmount(), amountPaid, amountWaived, amountWrittenOff, amountOutstanding,
                chargeData.getChargeTimeType(), dueAsOfDate, chargeData.getChargeCalculationType(), percentage, amountPercentageAppliedTo,
                chargeOptions, chargeData.isPenalty(), chargeData.getFeeOnMonthDay(), chargeData.getFeeInterval(), isActive,
                isFreeWithdrawal, freeWithdrawalChargeFrequency, restartFrequency, restartFrequencyEnum, inactivationDate);
    }

    public static ShareAccountChargeData toShareAccountChargeData(final ChargeData chargeData) {

        final Long shareChargeId = null;
        final Long shareAccountId = null;
        final BigDecimal amountPaid = BigDecimal.ZERO;
        final BigDecimal amountWaived = BigDecimal.ZERO;
        final BigDecimal amountWrittenOff = BigDecimal.ZERO;
        final BigDecimal amountOutstanding = BigDecimal.ZERO;
        final BigDecimal percentage = BigDecimal.ZERO;
        final BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        final Collection<ChargeData> chargeOptions = null;
        final Boolean isActive = null;
        final BigDecimal chargeAmountOrPercentage = BigDecimal.ZERO;

        return new ShareAccountChargeData(shareChargeId, chargeData.getId(), shareAccountId, chargeData.getName(), chargeData.getCurrency(),
                chargeData.getAmount(), amountPaid, amountWaived, amountWrittenOff, amountOutstanding, chargeData.getChargeTimeType(),
                chargeData.getChargeCalculationType(), percentage, amountPercentageAppliedTo, chargeOptions, isActive,
                chargeAmountOrPercentage);
    }

}
