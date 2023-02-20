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
package org.apache.fineract.portfolio.validationlimit.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.validationlimit.api.ValidationLimitApiConstants;

@Entity
@Table(name = "m_validation_limits")
public class ValidationLimit extends AbstractPersistableCustom {

    public ValidationLimit() {}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_level_cv_id")
    private CodeValue clientLevel;

    @Column(name = "maximum_single_deposit_amount")
    private BigDecimal maximumSingleDepositAmount;

    @Column(name = "maximum_cumulative_balance")
    private BigDecimal maximumCumulativeBalance;

    @Column(name = "maximum_transaction_limit")
    private BigDecimal maximumSingleWithdrawLimit;

    @Column(name = "maximum_daily_transaction_amount_limit")
    private BigDecimal maximumDailyWithdrawLimit;

    @Column(name = "max_client_specific_single_withdrawal_limit")
    private BigDecimal maximumClientSpecificSingleWithdrawLimit;

    @Column(name = "max_client_specific_daily_withdrawal_limit")
    private BigDecimal maximumClientSpecificDailyWithdrawLimit;

    private ValidationLimit(CodeValue clientLevel, BigDecimal maximumSingleDepositAmount, BigDecimal maximumCumulativeBalance,
            BigDecimal maximumTransactionLimit, BigDecimal maximumDailyTransactionAmountLimit,
            BigDecimal maximumClientSpecificDailyWithdrawLimit, BigDecimal maximumClientSpecificSingleWithdrawLimit) {
        this.clientLevel = clientLevel;
        this.maximumSingleDepositAmount = maximumSingleDepositAmount;
        this.maximumCumulativeBalance = maximumCumulativeBalance;
        this.maximumSingleWithdrawLimit = maximumTransactionLimit;
        this.maximumDailyWithdrawLimit = maximumDailyTransactionAmountLimit;
        this.maximumClientSpecificDailyWithdrawLimit = maximumClientSpecificDailyWithdrawLimit;
        this.maximumClientSpecificSingleWithdrawLimit = maximumClientSpecificSingleWithdrawLimit;

    }

    public static ValidationLimit fromJson(CodeValue clientLevel, JsonCommand command) {
        BigDecimal maximumSingleDepositAmount = command
                .bigDecimalValueOfParameterNamed(ValidationLimitApiConstants.MAXIMUM_SINGLE_DEPOSIT_AMOUNT);
        BigDecimal maximumCumulativeBalance = command
                .bigDecimalValueOfParameterNamed(ValidationLimitApiConstants.MAXIMUM_CUMULATIVE_BALANCE);
        BigDecimal maximumSingleWithdrawLimit = command
                .bigDecimalValueOfParameterNamed(ValidationLimitApiConstants.MAXIMUM_SINGLE_WITHDRAW_LIMIT);
        BigDecimal maximumDailyWithdrawLimit = command
                .bigDecimalValueOfParameterNamed(ValidationLimitApiConstants.MAXIMUM_DAILY_WITHDRAW_LIMIT);
        BigDecimal maximumClientSpecificDailyWithdrawLimit = command
                .bigDecimalValueOfParameterNamed(ValidationLimitApiConstants.MAXIMUM_CLIENT_SPECIFIC_DAILY_WITHDRAW_LIMIT);
        BigDecimal maximumClientSpecificSingleWithdrawLimit = command
                .bigDecimalValueOfParameterNamed(ValidationLimitApiConstants.MAXIMUM_CLIENT_SPECIFIC_SINGLE_WITHDRAW_LIMIT);

        return new ValidationLimit(clientLevel, maximumSingleDepositAmount, maximumCumulativeBalance, maximumSingleWithdrawLimit,
                maximumDailyWithdrawLimit, maximumClientSpecificDailyWithdrawLimit, maximumClientSpecificSingleWithdrawLimit);
    }

    public CodeValue getClientLevel() {
        return this.clientLevel;
    }

    public BigDecimal getMaximumSingleDepositAmount() {
        return this.maximumSingleDepositAmount;
    }

    public BigDecimal getMaximumCumulativeBalance() {
        return this.maximumCumulativeBalance;
    }

    public BigDecimal getMaximumSingleWithdrawLimit() {
        return this.maximumSingleWithdrawLimit == null ? BigDecimal.ZERO : this.maximumSingleWithdrawLimit;
    }

    public BigDecimal getMaximumDailyWithdrawLimit() {
        return this.maximumDailyWithdrawLimit == null ? BigDecimal.ZERO : this.maximumDailyWithdrawLimit;
    }

    public BigDecimal getMaximumClientSpecificSingleWithdrawLimit() {
        return maximumClientSpecificSingleWithdrawLimit;
    }

    public BigDecimal getMaximumClientSpecificDailyWithdrawLimit() {
        return maximumClientSpecificDailyWithdrawLimit;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);
        final String localParamName = "locale";
        final String localeAsInput = command.locale();

        if (command.isChangeInLongParameterNamed(ValidationLimitApiConstants.CLIENT_LEVEL_ID, clientLevelId())) {
            Long newValue = command.longValueOfParameterNamed(ValidationLimitApiConstants.CLIENT_LEVEL_ID);
            actualChanges.put(ValidationLimitApiConstants.CLIENT_LEVEL_ID, newValue);
        }

        String maximumSingleDepositAmountParamName = ValidationLimitApiConstants.MAXIMUM_SINGLE_DEPOSIT_AMOUNT;
        if (command.isChangeInBigDecimalParameterNamed(maximumSingleDepositAmountParamName, this.maximumSingleDepositAmount)) {
            BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maximumSingleDepositAmountParamName);
            actualChanges.put(maximumSingleDepositAmountParamName, newValue);
            actualChanges.put(localParamName, localeAsInput);
            this.maximumSingleDepositAmount = newValue;
        }

        String maximumCumulativeBalanceParamName = ValidationLimitApiConstants.MAXIMUM_CUMULATIVE_BALANCE;
        if (command.isChangeInBigDecimalParameterNamed(maximumCumulativeBalanceParamName, this.maximumCumulativeBalance)) {
            BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maximumCumulativeBalanceParamName);
            actualChanges.put(maximumCumulativeBalanceParamName, newValue);
            actualChanges.put(localParamName, localeAsInput);
            this.maximumCumulativeBalance = newValue;
        }

        String maximumSingleWithdrawLimitParamName = ValidationLimitApiConstants.MAXIMUM_SINGLE_WITHDRAW_LIMIT;
        if (command.isChangeInBigDecimalParameterNamed(maximumSingleWithdrawLimitParamName, this.maximumSingleWithdrawLimit)) {
            BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maximumSingleWithdrawLimitParamName);
            actualChanges.put(maximumSingleWithdrawLimitParamName, newValue);
            actualChanges.put(localParamName, localeAsInput);
            this.maximumSingleWithdrawLimit = newValue;
        }

        String maximumDailyWithdrawLimitParamName = ValidationLimitApiConstants.MAXIMUM_DAILY_WITHDRAW_LIMIT;
        if (command.isChangeInBigDecimalParameterNamed(maximumDailyWithdrawLimitParamName, this.maximumDailyWithdrawLimit)) {
            BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maximumDailyWithdrawLimitParamName);
            actualChanges.put(maximumDailyWithdrawLimitParamName, newValue);
            actualChanges.put(localParamName, localeAsInput);
            this.maximumDailyWithdrawLimit = newValue;
        }

        String maximumClientSpecificDailyWithdrawLimitParamName = ValidationLimitApiConstants.MAXIMUM_CLIENT_SPECIFIC_DAILY_WITHDRAW_LIMIT;
        if (command.isChangeInBigDecimalParameterNamed(maximumClientSpecificDailyWithdrawLimitParamName,
                this.maximumClientSpecificDailyWithdrawLimit)) {
            BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maximumClientSpecificDailyWithdrawLimitParamName);
            actualChanges.put(maximumClientSpecificDailyWithdrawLimitParamName, newValue);
            actualChanges.put(localParamName, localeAsInput);
            this.maximumClientSpecificDailyWithdrawLimit = newValue;
        }

        String maximumClientSpecificSingleWithdrawLimitParamName = ValidationLimitApiConstants.MAXIMUM_CLIENT_SPECIFIC_SINGLE_WITHDRAW_LIMIT;
        if (command.isChangeInBigDecimalParameterNamed(maximumClientSpecificSingleWithdrawLimitParamName,
                this.maximumClientSpecificSingleWithdrawLimit)) {
            BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maximumClientSpecificSingleWithdrawLimitParamName);
            actualChanges.put(maximumClientSpecificSingleWithdrawLimitParamName, newValue);
            actualChanges.put(localParamName, localeAsInput);
            this.maximumClientSpecificSingleWithdrawLimit = newValue;
        }

        return actualChanges;
    }

    public Long clientLevelId() {
        Long clientLevelId = null;
        if (this.clientLevel != null) {
            clientLevelId = this.clientLevel.getId();
        }
        return clientLevelId;
    }

    public void updateClientLevel(CodeValue clientLevel) {
        this.clientLevel = clientLevel;
    }

}
