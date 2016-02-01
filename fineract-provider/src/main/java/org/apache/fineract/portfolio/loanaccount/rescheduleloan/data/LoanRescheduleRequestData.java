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
package org.apache.fineract.portfolio.loanaccount.rescheduleloan.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing loan reschedule request data.
 **/
public class LoanRescheduleRequestData {

    private final Long id;
    private final Long loanId;
    private final Long clientId;
    private final String clientName;
    private final String loanAccountNumber;
    private final LoanRescheduleRequestStatusEnumData statusEnum;
    private final Integer rescheduleFromInstallment;
    private final Integer graceOnPrincipal;
    private final Integer graceOnInterest;
    private final LocalDate rescheduleFromDate;
    private final Integer extraTerms;
    private final BigDecimal interestRate;
    private final Boolean recalculateInterest;
    private final CodeValueData rescheduleReasonCodeValue;
    private final LoanRescheduleRequestTimelineData timeline;
    private final String rescheduleReasonComment;
    private final LocalDate adjustedDueDate;
    private final Collection<CodeValueData> rescheduleReasons;
    
    /**
     * LoanRescheduleRequestData constructor
     **/
    private LoanRescheduleRequestData(Long id, Long loanId, LoanRescheduleRequestStatusEnumData statusEnum,
            Integer rescheduleFromInstallment, Integer graceOnPrincipal, Integer graceOnInterest, LocalDate rescheduleFromDate,
            LocalDate adjustedDueDate, Integer extraTerms, BigDecimal interestRate, CodeValueData rescheduleReasonCodeValue,
            String rescheduleReasonComment, LoanRescheduleRequestTimelineData timeline, final String clientName,
            final String loanAccountNumber, final Long clientId, final Boolean recalculateInterest, Collection<CodeValueData> rescheduleReasons) {

        this.id = id;
        this.loanId = loanId;
        this.statusEnum = statusEnum;
        this.rescheduleFromInstallment = rescheduleFromInstallment;
        this.graceOnPrincipal = graceOnPrincipal;
        this.graceOnInterest = graceOnInterest;
        this.rescheduleFromDate = rescheduleFromDate;
        this.extraTerms = extraTerms;
        this.interestRate = interestRate;
        this.rescheduleReasonCodeValue = rescheduleReasonCodeValue;
        this.rescheduleReasonComment = rescheduleReasonComment;
        this.adjustedDueDate = adjustedDueDate;
        this.timeline = timeline;
        this.clientName = clientName;
        this.loanAccountNumber = loanAccountNumber;
        this.clientId = clientId;
        this.recalculateInterest = recalculateInterest;
        this.rescheduleReasons = rescheduleReasons ;
    }

    /**
     * @return an instance of the LoanRescheduleRequestData class
     **/
    public static LoanRescheduleRequestData instance(Long id, Long loanId, LoanRescheduleRequestStatusEnumData statusEnum,
            Integer rescheduleFromInstallment, Integer graceOnPrincipal, Integer graceOnInterest, LocalDate rescheduleFromDate,
            LocalDate adjustedDueDate, Integer extraTerms, BigDecimal interestRate, CodeValueData rescheduleReasonCodeValue,
            String rescheduleReasonComment, LoanRescheduleRequestTimelineData timeline, final String clientName,
            final String loanAccountNumber, final Long clientId, final Boolean recalculateInterest, Collection<CodeValueData> rescheduleReasons) {

        return new LoanRescheduleRequestData(id, loanId, statusEnum, rescheduleFromInstallment, graceOnPrincipal, graceOnInterest,
                rescheduleFromDate, adjustedDueDate, extraTerms, interestRate, rescheduleReasonCodeValue, rescheduleReasonComment,
                timeline, clientName, loanAccountNumber, clientId, recalculateInterest, rescheduleReasons);
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the loanId
     */
    public Long getLoanId() {
        return loanId;
    }

    /**
     * @return the statusEnum
     */
    public LoanRescheduleRequestStatusEnumData getStatusEnum() {
        return statusEnum;
    }

    /**
     * @return the reschedule from installment number
     */
    public Integer getRescheduleFromInstallment() {
        return rescheduleFromInstallment;
    }

    /**
     * @return the graceOnPrincipal
     */
    public Integer getGraceOnPrincipal() {
        return graceOnPrincipal;
    }

    /**
     * @return the graceOnInterest
     */
    public Integer getGraceOnInterest() {
        return graceOnInterest;
    }

    /**
     * @return the reschedule from date
     */
    public LocalDate getRescheduleFromDate() {
        return rescheduleFromDate;
    }

    /**
     * @return the extraTerms
     */
    public Integer getExtraTerms() {
        return extraTerms;
    }

    /**
     * @return the interestRate
     */
    public BigDecimal getInterestRate() {
        return interestRate;
    }

    /**
     * @return the rescheduleReasonCodeValueId
     */
    public CodeValueData getRescheduleReasonCodeValueId() {
        return rescheduleReasonCodeValue;
    }

    /**
     * @return the rescheduleReasonText
     */
    public String getRescheduleReasonComment() {
        return rescheduleReasonComment;
    }

    /**
     * @return the timeline
     **/
    public LoanRescheduleRequestTimelineData getTimeline() {
        return this.timeline;
    }

    /**
     * @return the adjustedDueDate
     */
    public LocalDate getAdjustedDueDate() {
        return adjustedDueDate;
    }

    /**
     * @return the clientName
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @return the loanAccountNumber
     */
    public String getLoanAccountNumber() {
        return loanAccountNumber;
    }

    /**
     * @return the clientId
     */
    public Long getClientId() {
        return clientId;
    }

    /**
     * @return the recalculateInterest
     */
    public Boolean getRecalculateInterest() {
        boolean value = false;

        if (recalculateInterest != null) {
            value = recalculateInterest;
        }

        return value;
    }
}
