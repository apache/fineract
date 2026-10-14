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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;

@Getter
public class LoanTermVariationsData implements Comparable<LoanTermVariationsData> {

    private final Long id;
    private final EnumOptionData termType;
    private LocalDate termVariationApplicableFrom;
    private final BigDecimal decimalValue;
    private final LocalDate dateValue;
    private final boolean isSpecificToInstallment;
    private Boolean isProcessed;
    private OffsetDateTime createdDate;

    public LoanTermVariationsData(final Long id, final EnumOptionData termType, final LocalDate termVariationApplicableFrom,
            final BigDecimal decimalValue, final LocalDate dateValue, final boolean isSpecificToInstallment) {
        this.id = id;
        this.termType = termType;
        this.termVariationApplicableFrom = termVariationApplicableFrom;
        this.decimalValue = decimalValue;
        this.dateValue = dateValue;
        this.isSpecificToInstallment = isSpecificToInstallment;
    }

    public LoanTermVariationsData(final Long id, final Integer termType, final LocalDate termVariationApplicableFrom,
            final BigDecimal decimalValue, final LocalDate dateValue, final boolean isSpecificToInstallment) {
        this.id = id;
        this.termType = LoanEnumerations.loanVariationType(LoanTermVariationType.fromInt(termType));
        this.termVariationApplicableFrom = termVariationApplicableFrom;
        this.decimalValue = decimalValue;
        this.dateValue = dateValue;
        this.isSpecificToInstallment = isSpecificToInstallment;
    }

    public LoanTermVariationsData(final EnumOptionData termType, final LocalDate termVariationApplicableFrom, final BigDecimal decimalValue,
            LocalDate dateValue, final boolean isSpecificToInstallment) {
        this.id = null;
        this.termType = termType;
        this.termVariationApplicableFrom = termVariationApplicableFrom;
        this.decimalValue = decimalValue;
        this.dateValue = dateValue;
        this.isSpecificToInstallment = isSpecificToInstallment;
    }

    public LoanTermVariationsData(Long id, EnumOptionData type, LocalDate termApplicableFrom, BigDecimal decimalValue, LocalDate dateValue,
            boolean isSpecificToInstallment, OffsetDateTime createdDate) {
        this(id, type, termApplicableFrom, decimalValue, dateValue, isSpecificToInstallment);
        this.createdDate = createdDate;
    }

    public LoanTermVariationType getTermVariationType() {
        return LoanTermVariationType.fromInt(this.termType.getId().intValue());
    }

    public boolean isApplicable(final LocalDate fromDate, final LocalDate dueDate) {
        return occursOnDayFromAndUpTo(fromDate, dueDate, this.termVariationApplicableFrom);
    }

    private boolean occursOnDayFromAndUpTo(final LocalDate fromNotInclusive, final LocalDate upToInclusive, final LocalDate target) {
        return DateUtils.isAfter(target, fromNotInclusive) && !DateUtils.isAfter(target, upToInclusive);
    }

    public boolean isApplicable(final LocalDate fromDate) {
        return occursBefore(fromDate, this.termVariationApplicableFrom);
    }

    private boolean occursBefore(final LocalDate date, final LocalDate target) {
        return target != null && !DateUtils.isAfter(target, date);
    }

    public boolean isSpecificToInstallment() {
        return this.isSpecificToInstallment;
    }

    public boolean isIsSpecificToInstallment() {
        return this.isSpecificToInstallment;
    }

    public Boolean isProcessed() {
        return this.isProcessed != null && this.isProcessed;
    }

    public void setProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    @Override
    public int compareTo(LoanTermVariationsData o) {
        int comparsion = getTermVariationApplicableFrom().compareTo(o.getTermVariationApplicableFrom());
        if (comparsion == 0) {
            if (o.getTermVariationType().isDueDateVariation() || o.getTermVariationType().isInsertInstallment()) {
                comparsion = 1;
            } else {
                comparsion = Integer.compare(sameDateOrder(getTermVariationType()), sameDateOrder(o.getTermVariationType()));
            }
        }
        return comparsion;
    }

    /**
     * Deterministic order for variations sharing an applicable date. A reschedule request's variations reach the
     * schedule generator from an unordered set, so without this the sort above (stable) kept whatever order that set
     * happened to iterate in. The order matters: EXTEND_REPAYMENT_PERIOD recalculates the installment amount on the
     * spot from the principal-grace periods registered so far. Applied before the GRACE_ON_PRINCIPAL it belongs to, it
     * amortises the balance over the grace periods as well and leaves a balloon last installment. So grace goes first,
     * then the extension, then everything else. Due-date and insert-installment variations keep their existing first
     * place.
     */
    private static int sameDateOrder(final LoanTermVariationType type) {
        if (type.isDueDateVariation() || type.isInsertInstallment()) {
            return -1;
        }
        if (type.isGraceOnPrincipal() || type.isGraceOnInterest()) {
            return 0;
        }
        if (type.isExtendRepaymentPeriod()) {
            return 1;
        }
        return 2;
    }

    public void setApplicableFromDate(final LocalDate applicableFromDate) {
        this.termVariationApplicableFrom = applicableFromDate;
    }

}
