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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRescheduleRequestToTermVariationMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariations;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_loan_reschedule_request")
public class LoanRescheduleRequest extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "status_enum", nullable = false)
    private Integer statusEnum;

    @Column(name = "reschedule_from_installment")
    private Integer rescheduleFromInstallment;

    @Column(name = "reschedule_from_date")
    private LocalDate rescheduleFromDate;

    @Column(name = "recalculate_interest")
    private Boolean recalculateInterest;

    @ManyToOne
    @JoinColumn(name = "reschedule_reason_cv_id")
    private CodeValue rescheduleReasonCodeValue;

    @Column(name = "reschedule_reason_comment")
    private String rescheduleReasonComment;

    @Column(name = "submitted_on_date")
    private LocalDate submittedOnDate;

    @ManyToOne
    @JoinColumn(name = "submitted_by_user_id")
    private AppUser submittedByUser;

    @Column(name = "approved_on_date")
    private LocalDate approvedOnDate;

    @ManyToOne
    @JoinColumn(name = "approved_by_user_id")
    private AppUser approvedByUser;

    @Column(name = "rejected_on_date")
    private LocalDate rejectedOnDate;

    @ManyToOne
    @JoinColumn(name = "rejected_by_user_id")
    private AppUser rejectedByUser;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "loanRescheduleRequest")
    private Set<LoanRescheduleRequestToTermVariationMapping> loanRescheduleRequestToTermVariationMappings = new HashSet<>();

    /**
     * LoanRescheduleRequest constructor
     **/
    protected LoanRescheduleRequest() {}

    /**
     * LoanRescheduleRequest constructor
     **/
    private LoanRescheduleRequest(final Loan loan, final Integer statusEnum, final Integer rescheduleFromInstallment,
            final LocalDate rescheduleFromDate, final Boolean recalculateInterest, final CodeValue rescheduleReasonCodeValue,
            final String rescheduleReasonComment, final LocalDate submittedOnDate, final AppUser submittedByUser,
            final LocalDate approvedOnDate, final AppUser approvedByUser, final LocalDate rejectedOnDate, AppUser rejectedByUser) {
        this.loan = loan;
        this.statusEnum = statusEnum;
        this.rescheduleFromInstallment = rescheduleFromInstallment;
        this.rescheduleFromDate = rescheduleFromDate;
        this.rescheduleReasonCodeValue = rescheduleReasonCodeValue;
        this.rescheduleReasonComment = rescheduleReasonComment;
        this.submittedOnDate = submittedOnDate;
        this.submittedByUser = submittedByUser;
        this.approvedOnDate = approvedOnDate;
        this.approvedByUser = approvedByUser;
        this.rejectedOnDate = rejectedOnDate;
        this.rejectedByUser = rejectedByUser;
        this.recalculateInterest = recalculateInterest;
    }

    /**
     * @return a new instance of the LoanRescheduleRequest class
     **/
    public static LoanRescheduleRequest instance(final Loan loan, final Integer statusEnum, final Integer rescheduleFromInstallment,
            final LocalDate rescheduleFromDate, final Boolean recalculateInterest, final CodeValue rescheduleReasonCodeValue,
            final String rescheduleReasonComment, final LocalDate submittedOnDate, final AppUser submittedByUser,
            final LocalDate approvedOnDate, final AppUser approvedByUser, final LocalDate rejectedOnDate, AppUser rejectedByUser) {

        return new LoanRescheduleRequest(loan, statusEnum, rescheduleFromInstallment, rescheduleFromDate, recalculateInterest,
                rescheduleReasonCodeValue, rescheduleReasonComment, submittedOnDate, submittedByUser, approvedOnDate, approvedByUser,
                rejectedOnDate, rejectedByUser);
    }

    /**
     * @return the reschedule request loan object
     **/
    public Loan getLoan() {
        return this.loan;
    }

    /**
     * @return the status enum
     **/
    public Integer getStatusEnum() {
        return this.statusEnum;
    }

    /**
     * @return installment number of the rescheduling start point
     **/
    public Integer getRescheduleFromInstallment() {
        return this.rescheduleFromInstallment;
    }

    /**
     * @return due date of the rescheduling start point
     **/
    public LocalDate getRescheduleFromDate() {
        return this.rescheduleFromDate;
    }

    /**
     * @return the reschedule reason code value object
     **/
    public CodeValue getRescheduleReasonCodeValue() {
        return this.rescheduleReasonCodeValue;
    }

    /**
     * @return the reschedule reason comment added by the "submittedByUser"
     **/
    public String getRescheduleReasonComment() {
        return this.rescheduleReasonComment;
    }

    /**
     * @return the date the request was submitted
     **/
    public LocalDate getSubmittedOnDate() {
        return this.submittedOnDate;
    }

    /**
     * @return the user that submitted the request
     **/
    public AppUser getSubmittedByUser() {
        return this.submittedByUser;
    }

    /**
     * @return the date the request was approved
     **/
    public LocalDate getApprovedOnDate() {
        return this.approvedOnDate;
    }

    /**
     * @return the user that approved the request
     **/
    public AppUser getApprovedByUser() {
        return this.approvedByUser;
    }

    /**
     * @return the date the request was rejected
     **/
    public LocalDate getRejectedOnDate() {
        return this.rejectedOnDate;
    }

    /**
     * @return the recalculate interest option (true/false)
     **/
    public Boolean getRecalculateInterest() {
        boolean recalculateInterest = false;

        if (this.recalculateInterest != null) {
            recalculateInterest = this.recalculateInterest;
        }

        return recalculateInterest;
    }

    /**
     * @return the user that rejected the request
     **/
    public AppUser getRejectedByUser() {
        return this.rejectedByUser;
    }

    /**
     * change the status of the loan reschedule request to approved, also updating the approvedByUser and approvedOnDate
     * properties
     *
     * @param approvedByUser
     *            the user who approved the request
     * @param approvedOnDate
     *            the date of the approval
     *
     **/
    public void approve(final AppUser approvedByUser, final LocalDate approvedOnDate) {

        if (approvedOnDate != null) {
            this.approvedByUser = approvedByUser;
            this.approvedOnDate = approvedOnDate;
            this.statusEnum = LoanStatus.APPROVED.getValue();
        }
    }

    /**
     * change the status of the loan reschedule request to rejected, also updating the approvedByUser and approvedOnDate
     * properties
     *
     * @param approvedByUser
     *            the user who approved the request
     * @param approvedOnDate
     *            the date of the approval
     *
     **/
    public void reject(final AppUser approvedByUser, final LocalDate approvedOnDate) {

        if (approvedOnDate != null) {
            this.rejectedByUser = approvedByUser;
            this.rejectedOnDate = approvedOnDate;
            this.statusEnum = LoanStatus.REJECTED.getValue();
        }
    }

    public void updateLoanRescheduleRequestToTermVariationMappings(final List<LoanRescheduleRequestToTermVariationMapping> mapping) {
        this.loanRescheduleRequestToTermVariationMappings.addAll(mapping);
    }

    public Set<LoanRescheduleRequestToTermVariationMapping> getLoanRescheduleRequestToTermVariationMappings() {
        return this.loanRescheduleRequestToTermVariationMappings;
    }

    public LoanTermVariations getDueDateTermVariationIfExists() {
        if (this.loanRescheduleRequestToTermVariationMappings != null && this.loanRescheduleRequestToTermVariationMappings.size() > 0) {
            for (LoanRescheduleRequestToTermVariationMapping mapping : this.loanRescheduleRequestToTermVariationMappings) {
                if (mapping.getLoanTermVariations().getTermType().isDueDateVariation()) {
                    return mapping.getLoanTermVariations();
                }
            }
        }
        return null;
    }
}
