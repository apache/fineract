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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.rate.domain.Rate;
import org.apache.fineract.useradministration.domain.AppUser;

/**
 * Builder class for creating {@link Loan} objects for testing purposes.
 */
public class LoanBuilder {

    private Long id;
    private String accountNo;
    private Client client = mock(Client.class);
    private Group group;
    private AccountType loanType = AccountType.INDIVIDUAL;
    private LoanProduct loanProduct;
    private Fund fund;
    private Staff loanOfficer;
    private CodeValue loanPurpose;
    private LoanRepaymentScheduleTransactionProcessor transactionProcessor = new InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(
            mock(ExternalIdFactory.class), mock(LoanChargeValidator.class), mock(LoanBalanceService.class));
    private LoanProductRelatedDetail loanRepaymentScheduleDetail;
    private LoanStatus loanStatus = LoanStatus.SUBMITTED_AND_PENDING_APPROVAL;
    private LoanSubStatus loanSubStatus;
    private Set<LoanCharge> charges = new HashSet<>();
    private Set<LoanCollateralManagement> collateral = new HashSet<>();
    private Boolean syncDisbursementWithMeeting = false;
    private BigDecimal fixedEmiAmount;
    private List<LoanDisbursementDetails> disbursementDetails = new ArrayList<>();
    private BigDecimal maxOutstandingLoanBalance;
    private Boolean createStandingInstructionAtDisbursement = false;
    private Boolean isFloatingInterestRate = false;
    private BigDecimal interestRateDifferential;
    private List<Rate> rates = new ArrayList<>();
    private BigDecimal fixedPrincipalPercentagePerInstallment;
    private ExternalId externalId = ExternalId.empty();
    private LoanApplicationTerms loanApplicationTerms = mock(LoanApplicationTerms.class);
    private Boolean enableInstallmentLevelDelinquency = false;
    private LocalDate submittedOnDate = LocalDate.now(ZoneId.systemDefault());
    private LocalDate approvedOnDate;
    private LocalDate expectedDisbursementDate;
    private LocalDate actualDisbursementDate;
    private LocalDate closedOnDate;
    private LocalDate writtenOffOnDate;
    private AppUser closedBy;
    private CodeValue writeOffReason;
    private boolean chargedOff = false;
    private CodeValue chargeOffReason;
    private LocalDate chargedOffOnDate;
    private AppUser chargedOffBy;
    private BigDecimal proposedPrincipal;
    private BigDecimal approvedPrincipal;
    private BigDecimal netDisbursalAmount;
    private List<LoanTransaction> loanTransactions = new ArrayList<>();
    private LoanSummary summary;

    public LoanBuilder(LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
        this.loanRepaymentScheduleDetail = loanProduct.getLoanProductRelatedDetail();

        Money principal = mock(Money.class);
        when(principal.getAmount()).thenReturn(BigDecimal.ZERO);
        when(loanRepaymentScheduleDetail.getPrincipal()).thenReturn(principal);

    }

    public Loan build() {
        // Build a minimal valid loan using reflection to access the protected constructor
        try {
            Loan loan = Loan.newIndividualLoanApplication(accountNo, client, loanType, loanProduct, fund, loanOfficer, loanPurpose,
                    transactionProcessor, loanRepaymentScheduleDetail, charges, collateral, fixedEmiAmount, disbursementDetails,
                    maxOutstandingLoanBalance, createStandingInstructionAtDisbursement, isFloatingInterestRate, interestRateDifferential,
                    rates, fixedPrincipalPercentagePerInstallment, externalId, loanApplicationTerms, enableInstallmentLevelDelinquency,
                    submittedOnDate);

            if (id != null) {
                loan.setId(id);
            }
            if (loanStatus != null) {
                loan.setLoanStatus(loanStatus);
            }

            if (loanSubStatus != null) {
                loan.setLoanSubStatus(loanSubStatus);
            }

            if (approvedOnDate != null) {
                loan.setApprovedOnDate(approvedOnDate);
            }

            if (expectedDisbursementDate != null) {
                loan.setExpectedDisbursementDate(expectedDisbursementDate);
            }

            if (actualDisbursementDate != null) {
                loan.setActualDisbursementDate(actualDisbursementDate);
            }

            if (closedOnDate != null) {
                loan.setClosedOnDate(closedOnDate);
                loan.setClosedBy(closedBy);
            }

            if (writtenOffOnDate != null) {
                loan.setWrittenOffOnDate(writtenOffOnDate);
            }

            if (chargedOff) {
                // Use reflection to set chargedOff flag
                java.lang.reflect.Field chargedOffField = Loan.class.getDeclaredField("chargedOff");
                chargedOffField.setAccessible(true);
                chargedOffField.set(loan, true);

                // Use reflection to set chargeOffReason
                java.lang.reflect.Field chargeOffReasonField = Loan.class.getDeclaredField("chargeOffReason");
                chargeOffReasonField.setAccessible(true);
                chargeOffReasonField.set(loan, chargeOffReason);

                // Use reflection to set chargedOffOnDate
                java.lang.reflect.Field chargedOffOnDateField = Loan.class.getDeclaredField("chargedOffOnDate");
                chargedOffOnDateField.setAccessible(true);
                chargedOffOnDateField.set(loan, chargedOffOnDate);

                // Use reflection to set chargedOffBy
                java.lang.reflect.Field chargedOffByField = Loan.class.getDeclaredField("chargedOffBy");
                chargedOffByField.setAccessible(true);
                chargedOffByField.set(loan, chargedOffBy);
            }

            if (proposedPrincipal != null) {
                loan.setProposedPrincipal(proposedPrincipal);
            }

            if (approvedPrincipal != null) {
                loan.setApprovedPrincipal(approvedPrincipal);
            }

            if (netDisbursalAmount != null) {
                loan.setNetDisbursalAmount(netDisbursalAmount);
            }

            // Set loan transactions using reflection
            if (!loanTransactions.isEmpty()) {
                java.lang.reflect.Field loanTransactionsField = Loan.class.getDeclaredField("loanTransactions");
                loanTransactionsField.setAccessible(true);
                loanTransactionsField.set(loan, loanTransactions);
            }

            // Set summary using reflection if provided
            if (summary != null) {
                java.lang.reflect.Field summaryField = Loan.class.getDeclaredField("summary");
                summaryField.setAccessible(true);
                summaryField.set(loan, summary);
            }

            return loan;
        } catch (Exception e) {
            throw new RuntimeException("Error building Loan object", e);
        }
    }

    public LoanBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public LoanBuilder withAccountNo(String accountNo) {
        this.accountNo = accountNo;
        return this;
    }

    public LoanBuilder withClient(Client client) {
        this.client = client;
        return this;
    }

    public LoanBuilder withGroup(Group group) {
        this.group = group;
        return this;
    }

    public LoanBuilder withLoanType(AccountType loanType) {
        this.loanType = loanType;
        return this;
    }

    public LoanBuilder withLoanProduct(LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
        return this;
    }

    public LoanBuilder withFund(Fund fund) {
        this.fund = fund;
        return this;
    }

    public LoanBuilder withLoanOfficer(Staff loanOfficer) {
        this.loanOfficer = loanOfficer;
        return this;
    }

    public LoanBuilder withLoanPurpose(CodeValue loanPurpose) {
        this.loanPurpose = loanPurpose;
        return this;
    }

    public LoanBuilder withTransactionProcessor(LoanRepaymentScheduleTransactionProcessor transactionProcessor) {
        this.transactionProcessor = transactionProcessor;
        return this;
    }

    public LoanBuilder withLoanRepaymentScheduleDetail(LoanProductRelatedDetail loanRepaymentScheduleDetail) {
        this.loanRepaymentScheduleDetail = loanRepaymentScheduleDetail;
        return this;
    }

    public LoanBuilder withLoanStatus(LoanStatus loanStatus) {
        this.loanStatus = loanStatus;
        return this;
    }

    public LoanBuilder withLoanSubStatus(LoanSubStatus loanSubStatus) {
        this.loanSubStatus = loanSubStatus;
        return this;
    }

    public LoanBuilder withCharges(Set<LoanCharge> charges) {
        this.charges = charges;
        return this;
    }

    public LoanBuilder withCollateral(Set<LoanCollateralManagement> collateral) {
        this.collateral = collateral;
        return this;
    }

    public LoanBuilder withSyncDisbursementWithMeeting(Boolean syncDisbursementWithMeeting) {
        this.syncDisbursementWithMeeting = syncDisbursementWithMeeting;
        return this;
    }

    public LoanBuilder withFixedEmiAmount(BigDecimal fixedEmiAmount) {
        this.fixedEmiAmount = fixedEmiAmount;
        return this;
    }

    public LoanBuilder withDisbursementDetails(List<LoanDisbursementDetails> disbursementDetails) {
        this.disbursementDetails = disbursementDetails;
        return this;
    }

    public LoanBuilder withMaxOutstandingLoanBalance(BigDecimal maxOutstandingLoanBalance) {
        this.maxOutstandingLoanBalance = maxOutstandingLoanBalance;
        return this;
    }

    public LoanBuilder withCreateStandingInstructionAtDisbursement(Boolean createStandingInstructionAtDisbursement) {
        this.createStandingInstructionAtDisbursement = createStandingInstructionAtDisbursement;
        return this;
    }

    public LoanBuilder withFloatingInterestRate(Boolean isFloatingInterestRate) {
        this.isFloatingInterestRate = isFloatingInterestRate;
        return this;
    }

    public LoanBuilder withInterestRateDifferential(BigDecimal interestRateDifferential) {
        this.interestRateDifferential = interestRateDifferential;
        return this;
    }

    public LoanBuilder withRates(List<Rate> rates) {
        this.rates = rates;
        return this;
    }

    public LoanBuilder withFixedPrincipalPercentagePerInstallment(BigDecimal fixedPrincipalPercentagePerInstallment) {
        this.fixedPrincipalPercentagePerInstallment = fixedPrincipalPercentagePerInstallment;
        return this;
    }

    public LoanBuilder withExternalId(ExternalId externalId) {
        this.externalId = externalId;
        return this;
    }

    public LoanBuilder withLoanApplicationTerms(LoanApplicationTerms loanApplicationTerms) {
        this.loanApplicationTerms = loanApplicationTerms;
        return this;
    }

    public LoanBuilder withEnableInstallmentLevelDelinquency(Boolean enableInstallmentLevelDelinquency) {
        this.enableInstallmentLevelDelinquency = enableInstallmentLevelDelinquency;
        return this;
    }

    public LoanBuilder withSubmittedOnDate(LocalDate submittedOnDate) {
        this.submittedOnDate = submittedOnDate;
        return this;
    }

    public LoanBuilder withApprovedOnDate(LocalDate approvedOnDate) {
        this.approvedOnDate = approvedOnDate;
        return this;
    }

    public LoanBuilder withExpectedDisbursementDate(LocalDate expectedDisbursementDate) {
        this.expectedDisbursementDate = expectedDisbursementDate;
        return this;
    }

    public LoanBuilder withActualDisbursementDate(LocalDate actualDisbursementDate) {
        this.actualDisbursementDate = actualDisbursementDate;
        return this;
    }

    public LoanBuilder withClosedOnDate(LocalDate closedOnDate) {
        this.closedOnDate = closedOnDate;
        return this;
    }

    public LoanBuilder withClosedBy(AppUser closedBy) {
        this.closedBy = closedBy;
        return this;
    }

    public LoanBuilder withWrittenOffOnDate(LocalDate writtenOffOnDate) {
        this.writtenOffOnDate = writtenOffOnDate;
        return this;
    }

    public LoanBuilder withWriteOffReason(CodeValue writeOffReason) {
        this.writeOffReason = writeOffReason;
        return this;
    }

    public LoanBuilder withChargedOff(boolean chargedOff) {
        this.chargedOff = chargedOff;
        return this;
    }

    public LoanBuilder withChargeOffReason(CodeValue chargeOffReason) {
        this.chargeOffReason = chargeOffReason;
        return this;
    }

    public LoanBuilder withChargedOffOnDate(LocalDate chargedOffOnDate) {
        this.chargedOffOnDate = chargedOffOnDate;
        return this;
    }

    public LoanBuilder withChargedOffBy(AppUser chargedOffBy) {
        this.chargedOffBy = chargedOffBy;
        return this;
    }

    public LoanBuilder withProposedPrincipal(BigDecimal proposedPrincipal) {
        this.proposedPrincipal = proposedPrincipal;
        return this;
    }

    public LoanBuilder withApprovedPrincipal(BigDecimal approvedPrincipal) {
        this.approvedPrincipal = approvedPrincipal;
        return this;
    }

    public LoanBuilder withNetDisbursalAmount(BigDecimal netDisbursalAmount) {
        this.netDisbursalAmount = netDisbursalAmount;
        return this;
    }

    public LoanBuilder withLoanTransactions(List<LoanTransaction> loanTransactions) {
        this.loanTransactions = loanTransactions;
        return this;
    }

    public LoanBuilder withLoanTransaction(LoanTransaction loanTransaction) {
        this.loanTransactions.add(loanTransaction);
        return this;
    }

    public LoanBuilder withSummary(LoanSummary summary) {
        this.summary = summary;
        return this;
    }
}
