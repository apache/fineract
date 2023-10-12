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
package org.apache.fineract.portfolio.savings.domain;

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.dateFormatParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositAmountParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositPeriodFrequencyIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositPeriodParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.expectedFirstDepositOnDateParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.localeParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.maturityInstructionIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.transferInterestToSavingsParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.transferToSavingsIdParamName;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.DepositAccountOnClosureType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;

@Entity
@Table(name = "m_deposit_account_term_and_preclosure")
public class DepositAccountTermAndPreClosure extends AbstractPersistableCustom {

    @Column(name = "deposit_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal depositAmount;

    @Column(name = "maturity_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal maturityAmount;

    @Column(name = "maturity_date", nullable = true)
    private LocalDate maturityDate;

    @Column(name = "expected_firstdepositon_date")
    private LocalDate expectedFirstDepositOnDate;

    @Column(name = "deposit_period", nullable = true)
    private Integer depositPeriod;

    @Column(name = "deposit_period_frequency_enum", nullable = true)
    private Integer depositPeriodFrequency;

    @Column(name = "on_account_closure_enum", nullable = true)
    private Integer onAccountClosureType;

    @Embedded
    private DepositPreClosureDetail preClosureDetail;

    @Embedded
    protected DepositTermDetail depositTermDetail;

    @OneToOne
    @JoinColumn(name = "savings_account_id", nullable = false)
    private SavingsAccount account;

    @Column(name = "transfer_interest_to_linked_account", nullable = false)
    private boolean transferInterestToLinkedAccount;

    @Column(name = "transfer_to_savings_account_id")
    private Long transferToSavingsAccountId;

    protected DepositAccountTermAndPreClosure() {

    }

    public static DepositAccountTermAndPreClosure createNew(DepositPreClosureDetail preClosureDetail, DepositTermDetail depositTermDetail,
            SavingsAccount account, BigDecimal depositAmount, BigDecimal maturityAmount, final LocalDate maturityDate,
            Integer depositPeriod, final SavingsPeriodFrequencyType depositPeriodFrequency, final LocalDate expectedFirstDepositOnDate,
            final DepositAccountOnClosureType accountOnClosureType, Boolean trasferInterest, Long transferToSavingsId) {

        return new DepositAccountTermAndPreClosure(preClosureDetail, depositTermDetail, account, depositAmount, maturityAmount,
                maturityDate, depositPeriod, depositPeriodFrequency, expectedFirstDepositOnDate, accountOnClosureType, trasferInterest,
                transferToSavingsId);
    }

    private DepositAccountTermAndPreClosure(DepositPreClosureDetail preClosureDetail, DepositTermDetail depositTermDetail,
            SavingsAccount account, BigDecimal depositAmount, BigDecimal maturityAmount, final LocalDate maturityDate,
            Integer depositPeriod, final SavingsPeriodFrequencyType depositPeriodFrequency, final LocalDate expectedFirstDepositOnDate,
            final DepositAccountOnClosureType accountOnClosureType, Boolean transferInterest, Long transferToSavingsId) {
        this.depositAmount = depositAmount;
        this.maturityAmount = maturityAmount;
        this.maturityDate = maturityDate;
        this.depositPeriod = depositPeriod;
        this.depositPeriodFrequency = (depositPeriodFrequency == null) ? null : depositPeriodFrequency.getValue();
        this.preClosureDetail = preClosureDetail;
        this.depositTermDetail = depositTermDetail;
        this.account = account;
        this.expectedFirstDepositOnDate = expectedFirstDepositOnDate;
        this.onAccountClosureType = (accountOnClosureType == null) ? null : accountOnClosureType.getValue();
        this.transferInterestToLinkedAccount = transferInterest;
        this.transferToSavingsAccountId = transferToSavingsId;
    }

    public Map<String, Object> update(final JsonCommand command, final DataValidatorBuilder baseDataValidator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);

        if (command.isChangeInBigDecimalParameterNamed(depositAmountParamName, this.depositAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(depositAmountParamName);
            actualChanges.put(depositAmountParamName, newValue);
            this.depositAmount = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(depositPeriodParamName, this.depositPeriod)) {
            final Integer newValue = command.integerValueOfParameterNamed(depositPeriodParamName);
            actualChanges.put(depositPeriodParamName, newValue);
            this.depositPeriod = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(depositPeriodFrequencyIdParamName, this.depositPeriodFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(depositPeriodFrequencyIdParamName);
            actualChanges.put(depositPeriodFrequencyIdParamName, SavingsEnumerations.depositTermFrequencyType(newValue));
            this.depositPeriodFrequency = newValue;
        }

        final String localeAsInput = command.locale();
        final String dateFormat = command.dateFormat();
        if (command.isChangeInLocalDateParameterNamed(expectedFirstDepositOnDateParamName, this.getExpectedFirstDepositOnDate())) {
            final String newValueAsString = command.stringValueOfParameterNamed(expectedFirstDepositOnDateParamName);
            actualChanges.put(expectedFirstDepositOnDateParamName, newValueAsString);
            actualChanges.put(localeParamName, localeAsInput);
            actualChanges.put(dateFormatParamName, dateFormat);
            this.expectedFirstDepositOnDate = command.localDateValueOfParameterNamed(expectedFirstDepositOnDateParamName);
        }

        if (command.isChangeInBooleanParameterNamed(transferInterestToSavingsParamName, this.transferInterestToLinkedAccount)) {
            final Boolean newValue = command.booleanPrimitiveValueOfParameterNamed(transferInterestToSavingsParamName);
            actualChanges.put(transferInterestToSavingsParamName, newValue);
            this.transferInterestToLinkedAccount = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(maturityInstructionIdParamName, this.onAccountClosureType)
                || command.integerValueOfParameterNamed(maturityInstructionIdParamName) == null) {
            final Integer newValue = command.integerValueOfParameterNamed(maturityInstructionIdParamName);
            actualChanges.put(maturityInstructionIdParamName, newValue);
            this.onAccountClosureType = newValue != null ? DepositAccountOnClosureType.fromInt(newValue).getValue() : null;
        }

        if (command.isChangeInLongParameterNamed(transferToSavingsIdParamName, this.transferToSavingsAccountId)
                || command.integerValueOfParameterNamed(transferToSavingsIdParamName) == null) {
            final Long newValue = command.longValueOfParameterNamed(transferToSavingsIdParamName);
            actualChanges.put(transferToSavingsIdParamName, newValue);
            this.transferToSavingsAccountId = newValue;
        }

        if (this.preClosureDetail != null) {
            actualChanges.putAll(this.preClosureDetail.update(command, baseDataValidator));
        }

        if (this.depositTermDetail != null) {
            actualChanges.putAll(this.depositTermDetail.update(command, baseDataValidator));
        }
        return actualChanges;
    }

    public DepositPreClosureDetail depositPreClosureDetail() {
        return this.preClosureDetail;
    }

    public DepositTermDetail depositTermDetail() {
        return this.depositTermDetail;
    }

    public BigDecimal depositAmount() {
        return this.depositAmount;
    }

    public Integer depositPeriod() {
        return this.depositPeriod;
    }

    public Integer depositPeriodFrequency() {
        return this.depositPeriodFrequency;
    }

    public SavingsPeriodFrequencyType depositPeriodFrequencyType() {
        return SavingsPeriodFrequencyType.fromInt(depositPeriodFrequency);
    }

    public void updateAccountReference(final SavingsAccount account) {
        this.account = account;
    }

    public void updateMaturityDetails(final BigDecimal maturityAmount, final LocalDate maturityDate) {
        this.maturityAmount = maturityAmount;
        this.maturityDate = maturityDate;
    }

    public void updateMaturityDetails(final BigDecimal depositAmount, final BigDecimal interestPayable, final LocalDate maturityDate) {
        this.depositAmount = depositAmount;
        this.maturityAmount = this.depositAmount.add(interestPayable);
        this.maturityDate = maturityDate;
    }

    public void updateDepositAmount(final BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public LocalDate getMaturityDate() {
        return this.maturityDate;
    }

    public LocalDate getExpectedFirstDepositOnDate() {
        return this.expectedFirstDepositOnDate;
    }

    public boolean isPreClosurePenalApplicable() {
        if (this.preClosureDetail != null) {
            return this.preClosureDetail.preClosurePenalApplicable();
        }
        return false;
    }

    public Integer getActualDepositPeriod(final LocalDate interestPostingUpToDate, final SavingsPeriodFrequencyType periodFrequencyType) {
        LocalDate depositFromDate = getExpectedFirstDepositOnDate();

        if (depositFromDate == null) {
            depositFromDate = this.account.accountSubmittedOrActivationDate();
        }

        Integer actualDepositPeriod = this.depositPeriod;
        if (depositFromDate == null || getMaturityDate() == null || DateUtils.isEqual(interestPostingUpToDate, getMaturityDate())) {
            return actualDepositPeriod;
        }

        final SavingsPeriodFrequencyType depositPeriodFrequencyType = periodFrequencyType;
        switch (depositPeriodFrequencyType) {
            case DAYS:
                actualDepositPeriod = Math.toIntExact(ChronoUnit.DAYS.between(depositFromDate, interestPostingUpToDate));
            break;
            case WEEKS:
                actualDepositPeriod = Math.toIntExact(ChronoUnit.WEEKS.between(depositFromDate, interestPostingUpToDate));
            break;
            case MONTHS:
                actualDepositPeriod = Math.toIntExact(ChronoUnit.MONTHS.between(depositFromDate, interestPostingUpToDate));
            break;
            case YEARS:
                actualDepositPeriod = Math.toIntExact(ChronoUnit.YEARS.between(depositFromDate, interestPostingUpToDate));
            break;
            case INVALID:
                actualDepositPeriod = this.depositPeriod;// default value
            break;
        }
        return actualDepositPeriod;
    }

    public BigDecimal maturityAmount() {
        return this.maturityAmount;
    }

    public void updateOnAccountClosureStatus(final DepositAccountOnClosureType onClosureType) {
        this.onAccountClosureType = onClosureType.getValue();
    }

    public boolean isReinvestOnClosure() {
        return DepositAccountOnClosureType.fromInt(this.onAccountClosureType).isReinvest();
    }

    public boolean isTransferToSavingsOnClosure() {
        return DepositAccountOnClosureType.fromInt(this.onAccountClosureType).isTransferToSavings();
    }

    public DepositAccountTermAndPreClosure copy(BigDecimal depositAmount) {
        final SavingsAccount account = null;
        final BigDecimal maturityAmount = null;
        final BigDecimal actualDepositAmount = depositAmount;
        final LocalDate maturityDate = null;
        final Integer depositPeriod = this.depositPeriod;
        final SavingsPeriodFrequencyType depositPeriodFrequency = SavingsPeriodFrequencyType.fromInt(this.depositPeriodFrequency);
        final DepositPreClosureDetail preClosureDetail = this.preClosureDetail.copy();
        final DepositTermDetail depositTermDetail = this.depositTermDetail.copy();
        final LocalDate expectedFirstDepositOnDate = null;
        final Boolean transferInterestToLinkedAccount = false;

        final DepositAccountOnClosureType accountOnClosureType = null;
        final Long transferToSavingsId = null;
        return DepositAccountTermAndPreClosure.createNew(preClosureDetail, depositTermDetail, account, actualDepositAmount, maturityAmount,
                maturityDate, depositPeriod, depositPeriodFrequency, expectedFirstDepositOnDate, accountOnClosureType,
                transferInterestToLinkedAccount, transferToSavingsId);
    }

    public void updateExpectedFirstDepositDate(final LocalDate expectedFirstDepositOnDate) {
        this.expectedFirstDepositOnDate = expectedFirstDepositOnDate;
    }

    public boolean isTransferInterestToLinkedAccount() {
        return this.transferInterestToLinkedAccount;
    }

    public boolean isAfterExpectedFirstDepositDate(final LocalDate compareDate) {
        return this.expectedFirstDepositOnDate != null && DateUtils.isAfter(compareDate, getExpectedFirstDepositOnDate());
    }

    public Integer getOnAccountClosureType() {
        return onAccountClosureType;
    }

    public Long getTransferToSavingsAccountId() {
        return transferToSavingsAccountId;
    }
}
