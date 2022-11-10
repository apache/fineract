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

package org.apache.fineract.portfolio.savings.request;

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.chartIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositAmountParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositPeriodFrequencyIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositPeriodParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.expectedFirstDepositOnDateParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.isCalendarInheritedParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.transferInterestToSavingsParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.accountNoParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.clientIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.externalIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.fieldOfficerIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.groupIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.productIdParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.submittedOnDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withHoldTaxParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;

public class FixedDepositApplicationReq {

    private Locale locale;
    private String dateFormat;
    private LocalDate submittedOnDate;
    private String accountNo;
    private String nickname;

    private String externalId;
    private Long productId;
    private Long clientId;
    private Long groupId;
    private Long fieldOfficerId;
    private BigDecimal interestRate;
    private boolean isCalendarInherited;
    private boolean interestRateSet = false;
    private Integer interestPeriodTypeValue;
    private Integer interestPostingPeriodTypeValue;
    private Integer interestCalculationTypeValue;
    private Integer interestCalculationDaysInYearTypeValue;
    private BigDecimal minRequiredOpeningBalance;
    private boolean minRequiredOpeningBalanceSet = false;
    private Integer lockinPeriodFrequency;
    private boolean lockinPeriodFrequencySet = false;
    private Integer lockinPeriodFrequencyTypeValue;
    private boolean lockinPeriodFrequencyTypeValueSet = false;
    private boolean isWithdrawalFeeApplicableForTransfer = false;
    private Long chartId;
    private boolean chartIdSet = false;
    private boolean withHoldTax = false;
    private boolean withHoldTaxSet = false;
    private BigDecimal depositAmount;
    private Integer depositPeriod;
    private SavingsPeriodFrequencyType depositPeriodFrequency;
    private LocalDate expectedFirstDepositOnDate;
    private Boolean transferInterest;
    private Long savingsAccountId;
    private BigDecimal interestCarriedForward;
    private String closedFixedDepositAccountNumber;

    private RecurringAccountDetailReq recurringAccountDetailReq;
    private FixedDepositApplicationTermsReq fixedDepositApplicationTermsReq;
    private FixedDepositApplicationPreClosureReq fixedDepositApplicationPreClosureReq;

    public static FixedDepositApplicationReq instance(JsonCommand command) {
        FixedDepositApplicationReq instance = new FixedDepositApplicationReq();

        instance.locale = command.extractLocale();
        instance.dateFormat = command.dateFormat();
        instance.accountNo = command.stringValueOfParameterNamed(accountNoParamName);
        instance.nickname = command.stringValueOfParameterNamed(SavingsApiConstants.nicknameParamName);
        instance.externalId = command.stringValueOfParameterNamedAllowingNull(externalIdParamName);
        instance.productId = command.longValueOfParameterNamed(productIdParamName);
        instance.clientId = command.longValueOfParameterNamed(clientIdParamName);
        instance.isCalendarInherited = command.booleanPrimitiveValueOfParameterNamed(isCalendarInheritedParamName);
        instance.groupId = command.longValueOfParameterNamed(groupIdParamName);
        instance.fieldOfficerId = command.longValueOfParameterNamed(fieldOfficerIdParamName);
        instance.submittedOnDate = command.localDateValueOfParameterNamed(submittedOnDateParamName);
        instance.savingsAccountId = command.longValueOfParameterNamed(DepositsApiConstants.linkedAccountParamName);
        if (command.parameterExists(nominalAnnualInterestRateParamName)) {
            instance.interestRateSet = true;
            instance.interestRate = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);
        }
        instance.interestPeriodTypeValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
        instance.interestPostingPeriodTypeValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
        instance.interestCalculationTypeValue = command.integerValueOfParameterNamed(interestCalculationTypeParamName);
        instance.interestCalculationDaysInYearTypeValue = command.integerValueOfParameterNamed(interestCalculationDaysInYearTypeParamName);
        instance.minRequiredOpeningBalance = command.bigDecimalValueOfParameterNamed(minRequiredOpeningBalanceParamName);
        if (command.parameterExists(minRequiredOpeningBalanceParamName)) {
            instance.minRequiredOpeningBalanceSet = true;
            instance.minRequiredOpeningBalance = command.bigDecimalValueOfParameterNamed(minRequiredOpeningBalanceParamName);
        }
        if (command.parameterExists(lockinPeriodFrequencyParamName)) {
            instance.lockinPeriodFrequencySet = true;
            instance.lockinPeriodFrequency = command.integerValueOfParameterNamed(lockinPeriodFrequencyParamName);
        }
        if (command.parameterExists(lockinPeriodFrequencyTypeParamName)) {
            instance.lockinPeriodFrequencyTypeValueSet = true;
            instance.lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
        }
        if (command.parameterExists(withdrawalFeeForTransfersParamName)) {
            instance.isWithdrawalFeeApplicableForTransfer = command
                    .booleanPrimitiveValueOfParameterNamed(withdrawalFeeForTransfersParamName);
        }
        if (command.parameterExists(chartIdParamName)) {
            instance.chartIdSet = true;
            instance.chartId = command.longValueOfParameterNamed(chartIdParamName);
        }
        if (command.parameterExists(withHoldTaxParamName)) {
            instance.withHoldTaxSet = true;
            instance.withHoldTax = command.booleanPrimitiveValueOfParameterNamed(withHoldTaxParamName);
        }

        instance.depositAmount = command.bigDecimalValueOfParameterNamed(depositAmountParamName);
        instance.depositPeriod = command.integerValueOfParameterNamed(depositPeriodParamName);
        instance.depositPeriodFrequency = SavingsPeriodFrequencyType
                .fromInt(command.integerValueOfParameterNamed(depositPeriodFrequencyIdParamName));
        instance.expectedFirstDepositOnDate = command.localDateValueOfParameterNamed(expectedFirstDepositOnDateParamName);
        instance.transferInterest = command.booleanPrimitiveValueOfParameterNamed(transferInterestToSavingsParamName);

        instance.recurringAccountDetailReq = RecurringAccountDetailReq.instance(command);
        instance.fixedDepositApplicationTermsReq = FixedDepositApplicationTermsReq.instance(command);
        instance.fixedDepositApplicationPreClosureReq = FixedDepositApplicationPreClosureReq.instance(command);
        return instance;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public LocalDate getSubmittedOnDate() {
        return submittedOnDate;
    }

    public void setSubmittedOnDate(LocalDate submittedOnDate) {
        this.submittedOnDate = submittedOnDate;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getFieldOfficerId() {
        return fieldOfficerId;
    }

    public void setFieldOfficerId(Long fieldOfficerId) {
        this.fieldOfficerId = fieldOfficerId;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public boolean isCalendarInherited() {
        return isCalendarInherited;
    }

    public void setCalendarInherited(boolean calendarInherited) {
        isCalendarInherited = calendarInherited;
    }

    public boolean isInterestRateSet() {
        return interestRateSet;
    }

    public void setInterestRateSet(boolean interestRateSet) {
        this.interestRateSet = interestRateSet;
    }

    public Integer getInterestPeriodTypeValue() {
        return interestPeriodTypeValue;
    }

    public void setInterestPeriodTypeValue(Integer interestPeriodTypeValue) {
        this.interestPeriodTypeValue = interestPeriodTypeValue;
    }

    public Integer getInterestPostingPeriodTypeValue() {
        return interestPostingPeriodTypeValue;
    }

    public void setInterestPostingPeriodTypeValue(Integer interestPostingPeriodTypeValue) {
        this.interestPostingPeriodTypeValue = interestPostingPeriodTypeValue;
    }

    public Integer getInterestCalculationTypeValue() {
        return interestCalculationTypeValue;
    }

    public void setInterestCalculationTypeValue(Integer interestCalculationTypeValue) {
        this.interestCalculationTypeValue = interestCalculationTypeValue;
    }

    public Integer getInterestCalculationDaysInYearTypeValue() {
        return interestCalculationDaysInYearTypeValue;
    }

    public void setInterestCalculationDaysInYearTypeValue(Integer interestCalculationDaysInYearTypeValue) {
        this.interestCalculationDaysInYearTypeValue = interestCalculationDaysInYearTypeValue;
    }

    public BigDecimal getMinRequiredOpeningBalance() {
        return minRequiredOpeningBalance;
    }

    public void setMinRequiredOpeningBalance(BigDecimal minRequiredOpeningBalance) {
        this.minRequiredOpeningBalance = minRequiredOpeningBalance;
    }

    public boolean isMinRequiredOpeningBalanceSet() {
        return minRequiredOpeningBalanceSet;
    }

    public Integer getLockinPeriodFrequency() {
        return lockinPeriodFrequency;
    }

    public void setLockinPeriodFrequency(Integer lockinPeriodFrequency) {
        this.lockinPeriodFrequency = lockinPeriodFrequency;
    }

    public boolean isLockinPeriodFrequencySet() {
        return lockinPeriodFrequencySet;
    }

    public void setLockinPeriodFrequencySet(boolean lockinPeriodFrequencySet) {
        this.lockinPeriodFrequencySet = lockinPeriodFrequencySet;
    }

    public Integer getLockinPeriodFrequencyTypeValue() {
        return lockinPeriodFrequencyTypeValue;
    }

    public void setLockinPeriodFrequencyTypeValue(Integer lockinPeriodFrequencyTypeValue) {
        this.lockinPeriodFrequencyTypeValue = lockinPeriodFrequencyTypeValue;
    }

    public boolean isLockinPeriodFrequencyTypeValueSet() {
        return lockinPeriodFrequencyTypeValueSet;
    }

    public void setLockinPeriodFrequencyTypeValueSet(boolean lockinPeriodFrequencyTypeValueSet) {
        this.lockinPeriodFrequencyTypeValueSet = lockinPeriodFrequencyTypeValueSet;
    }

    public boolean isWithdrawalFeeApplicableForTransfer() {
        return isWithdrawalFeeApplicableForTransfer;
    }

    public void setWithdrawalFeeApplicableForTransfer(boolean withdrawalFeeApplicableForTransfer) {
        isWithdrawalFeeApplicableForTransfer = withdrawalFeeApplicableForTransfer;
    }

    public Long getChartId() {
        return chartId;
    }

    public void setChartId(Long chartId) {
        this.chartId = chartId;
    }

    public boolean isChartIdSet() {
        return chartIdSet;
    }

    public boolean isWithHoldTax() {
        return withHoldTax;
    }

    public void setWithHoldTax(boolean withHoldTax) {
        this.withHoldTax = withHoldTax;
    }

    public boolean isWithHoldTaxSet() {
        return withHoldTaxSet;
    }

    public void setWithHoldTaxSet(boolean withHoldTaxSet) {
        this.withHoldTaxSet = withHoldTaxSet;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public Integer getDepositPeriod() {
        return depositPeriod;
    }

    public void setDepositPeriod(Integer depositPeriod) {
        this.depositPeriod = depositPeriod;
    }

    public SavingsPeriodFrequencyType getDepositPeriodFrequency() {
        return depositPeriodFrequency;
    }

    public void setDepositPeriodFrequency(SavingsPeriodFrequencyType depositPeriodFrequency) {
        this.depositPeriodFrequency = depositPeriodFrequency;
    }

    public LocalDate getExpectedFirstDepositOnDate() {
        return expectedFirstDepositOnDate;
    }

    public Boolean getTransferInterest() {
        return transferInterest;
    }

    public void setTransferInterest(Boolean transferInterest) {
        this.transferInterest = transferInterest;
    }

    public Long getSavingsAccountId() {
        return savingsAccountId;
    }

    public void setSavingsAccountId(Long savingsAccountId) {
        this.savingsAccountId = savingsAccountId;
    }

    public RecurringAccountDetailReq getRecurringAccountDetailReq() {
        return recurringAccountDetailReq;
    }

    public FixedDepositApplicationTermsReq getFixedDepositApplicationTermsReq() {
        return fixedDepositApplicationTermsReq;
    }

    public void setFixedDepositApplicationTermsReq(FixedDepositApplicationTermsReq fixedDepositApplicationTermsReq) {
        this.fixedDepositApplicationTermsReq = fixedDepositApplicationTermsReq;
    }

    public FixedDepositApplicationPreClosureReq getFixedDepositApplicationPreClosureReq() {
        return fixedDepositApplicationPreClosureReq;
    }

    public void setFixedDepositApplicationPreClosureReq(FixedDepositApplicationPreClosureReq fixedDepositApplicationPreClosureReq) {
        this.fixedDepositApplicationPreClosureReq = fixedDepositApplicationPreClosureReq;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public BigDecimal getInterestCarriedForward() {
        return interestCarriedForward;
    }

    public void setInterestCarriedForward(BigDecimal interestCarriedForward) {
        this.interestCarriedForward = interestCarriedForward;
    }

    public String getClosedFixedDepositAccountNumber() {
        return closedFixedDepositAccountNumber;
    }

    public void setClosedFixedDepositAccountNumber(String closedFixedDepositAccountNumber) {
        this.closedFixedDepositAccountNumber = closedFixedDepositAccountNumber;
    }
}
