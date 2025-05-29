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
import java.time.MonthDay;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.charge.data.ChargeChangeDto;
import org.apache.fineract.portfolio.charge.data.UpdateChargeRequest;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.ChargeDueAtDisbursementCannotBePenaltyException;
import org.apache.fineract.portfolio.charge.exception.ChargeMustBePenaltyException;

public final class ChargeChangeEntityUtil {

    private ChargeChangeEntityUtil() {}

    public static ChargeChangeDto prepareToUpdateEntity(Charge entityCharge, UpdateChargeRequest request) {
        final ChargeChangeDto changes = new ChargeChangeDto();

        final String newName = request.getName();
        if (newName != null && !newName.equals(entityCharge.getName())) {
            entityCharge.setName(newName);
            changes.setName(newName);
        }

        final String newCurrencyCode = request.getCurrencyCode();
        if (newCurrencyCode != null && !newCurrencyCode.equals(entityCharge.getCurrencyCode())) {
            entityCharge.setCurrencyCode(newCurrencyCode);
            changes.setCurrencyCode(newCurrencyCode);
        }

        final BigDecimal newAmount = request.getAmount();
        if (newAmount != null && !MathUtil.isEqualTo(newAmount, entityCharge.getAmount())) {
            entityCharge.setAmount(newAmount);
            changes.setAmount(newAmount);
        }

        final Integer newChargeTimeType = request.getChargeTimeType();
        if (newChargeTimeType != null && !newChargeTimeType.equals(entityCharge.getChargeTimeType())) {
            entityCharge.setChargeTimeType(newChargeTimeType);
            changes.setChargeTimeType(newChargeTimeType);
        }

        final Integer newChargeCalculationType = request.getChargeCalculationType();
        if (newChargeCalculationType != null && !newChargeCalculationType.equals(entityCharge.getChargeCalculation())) {
            entityCharge.setChargeCalculation(newChargeCalculationType);
            changes.setChargeCalculationType(newChargeCalculationType);
        }

        final Integer newChargePaymentMode = request.getChargePaymentMode();
        if (newChargePaymentMode != null && !newChargePaymentMode.equals(entityCharge.getChargePaymentMode())) {
            entityCharge.setChargePaymentMode(newChargePaymentMode);
            changes.setChargePaymentMode(newChargePaymentMode);
        }

        final Integer newFeeInterval = request.getFeeInterval();
        if (newFeeInterval != null && !newFeeInterval.equals(entityCharge.getFeeInterval())) {
            entityCharge.setFeeInterval(newFeeInterval);
            changes.setFeeInterval(newFeeInterval);
        }

        final Integer newFeeFrequency = request.getFeeFrequency();
        if (newFeeFrequency != null && !newFeeFrequency.equals(entityCharge.getFeeFrequency())) {
            entityCharge.setFeeFrequency(newFeeFrequency);
            changes.setFeeFrequency(newFeeFrequency);
        }

        final Boolean newPenalty = request.getPenalty();
        if (newPenalty != null && !newPenalty.equals(entityCharge.isPenalty())) {
            entityCharge.setPenalty(newPenalty);
            changes.setPenalty(newPenalty);
        }

        final Boolean newActive = request.getActive();
        if (newActive != null && !newActive.equals(entityCharge.isActive())) {
            entityCharge.setActive(newActive);
            changes.setActive(newActive);
        }

        final Boolean newEnableFreeWithdrawal = request.getEnableFreeWithdrawalCharge();
        if (newEnableFreeWithdrawal != null && !newEnableFreeWithdrawal.equals(entityCharge.isEnableFreeWithdrawal())) {
            changes.setEnableFreeWithdrawal(newEnableFreeWithdrawal);
            entityCharge.setEnableFreeWithdrawal(newEnableFreeWithdrawal);
        }

        final Integer newFreeWithdrawalFrequency = request.getFreeWithdrawalFrequency();
        if (newFreeWithdrawalFrequency != null && !newFreeWithdrawalFrequency.equals(entityCharge.getFreeWithdrawalFrequency())) {
            changes.setFreeWithdrawalFrequency(newFreeWithdrawalFrequency);
            entityCharge.setFreeWithdrawalFrequency(newFreeWithdrawalFrequency);
        }

        final Integer newRestartFrequency = request.getRestartCountFrequency();
        if (newRestartFrequency != null && !newRestartFrequency.equals(entityCharge.getRestartFrequency())) {
            entityCharge.setRestartFrequency(newRestartFrequency);
            changes.setRestartFrequency(newRestartFrequency);
        }

        final Integer newRestartFrequencyEnum = request.getCountFrequencyType();
        if (newRestartFrequencyEnum != null && !newRestartFrequencyEnum.equals(entityCharge.getRestartFrequencyEnum())) {
            entityCharge.setRestartFrequencyEnum(newRestartFrequencyEnum);
            changes.setRestartFrequencyEnum(newRestartFrequencyEnum);
        }

        final Boolean newEnablePaymentType = request.getEnablePaymentType();
        if (newEnablePaymentType != null && !newEnablePaymentType.equals(entityCharge.isEnablePaymentType())) {
            entityCharge.setEnablePaymentType(newEnablePaymentType);
            changes.setEnablePaymentType(newEnablePaymentType);
        }

        if (entityCharge.isPercentageOfApprovedAmount()) {
            final BigDecimal newMinCap = request.getMinCap();
            if (newMinCap != null && !MathUtil.isEqualTo(newMinCap, entityCharge.getMinCap())) {
                entityCharge.setMinCap(newMinCap);
                changes.setMinCap(newMinCap);
            }

            final BigDecimal newMaxCap = request.getMaxCap();
            if (newMaxCap != null && !MathUtil.isEqualTo(newMaxCap, entityCharge.getMaxCap())) {
                entityCharge.setMaxCap(newMaxCap);
                changes.setMaxCap(newMaxCap);
            }
        }

        final Long newPaymentTypeId = request.getPaymentTypeId();
        if (newPaymentTypeId != null && !newPaymentTypeId.equals(entityCharge.getPaymentTypeId())) {
            changes.setPaymentTypeId(newPaymentTypeId);
        }

        final Long newIncomeAccountId = request.getIncomeAccountId();
        if (newIncomeAccountId != null && !newIncomeAccountId.equals(entityCharge.getIncomeAccountId())) {
            changes.setIncomeAccountId(newIncomeAccountId);
        }

        final Long newTaxGroupId = request.getTaxGroupId();
        if (newTaxGroupId != null && !newTaxGroupId.equals(entityCharge.getTaxGroupId())) {
            changes.setTaxGroupId(newTaxGroupId);
        }

        String actualValueEntered = request.getFeeOnMonthDay();
        if (StringUtils.isNotBlank(actualValueEntered)) {
            final MonthDay feeOnMonthDay = MonthDay.parse(actualValueEntered);
            final Integer dayOfMonthValue = feeOnMonthDay.getDayOfMonth();

            if (!entityCharge.getFeeOnDay().equals(dayOfMonthValue)) {
                entityCharge.setFeeOnDay(dayOfMonthValue);
                changes.setFeeOnMonthDay(dayOfMonthValue);
            }

            final Integer monthOfYear = feeOnMonthDay.getMonthValue();
            if (!entityCharge.getFeeOnMonth().equals(monthOfYear)) {
                entityCharge.setFeeOnMonth(monthOfYear);
                changes.setFeeOnMonth(monthOfYear);
            }
        }

        if (entityCharge.isPenalty() && ChargeTimeType.fromInt(entityCharge.getChargeTimeType()).isTimeOfDisbursement()) {
            throw new ChargeDueAtDisbursementCannotBePenaltyException(entityCharge.getName());
        }
        if (!entityCharge.isPenalty() && ChargeTimeType.fromInt(entityCharge.getChargeTimeType()).isOverdueInstallment()) {
            throw new ChargeMustBePenaltyException(entityCharge.getName());
        }

        return changes;
    }
}
