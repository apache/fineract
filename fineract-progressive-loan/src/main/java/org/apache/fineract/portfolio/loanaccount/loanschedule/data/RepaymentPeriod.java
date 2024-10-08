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
package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.organisation.monetary.domain.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ToString
@EqualsAndHashCode(exclude = {"previous", "next"})
public class RepaymentPeriod {

    @ToString.Exclude
    private final RepaymentPeriod previous;
    @Setter
    @ToString.Exclude
    private RepaymentPeriod next;
    @Getter
    private final LocalDate fromDate;
    @Getter
    private final LocalDate dueDate;
    @Getter
    private final List<InterestPeriod> interestPeriods;
    @Getter
    @Setter
    private Money emi;
    @Getter
    @Setter
    private BigDecimal rateFactor;
    @Getter
    @Setter
    private Money paidPrincipal;
    @Getter
    @Setter
    private Money paidInterest;

    public RepaymentPeriod(RepaymentPeriod previous, LocalDate fromDate, LocalDate dueDate, Money emi, BigDecimal rateFactor) {
        this.previous = previous;
        if(previous != null) {
            previous.setNext(this);
        }
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.emi = emi;
        this.rateFactor = rateFactor;
        this.interestPeriods = new ArrayList<>();
        //There is always at least 1 interest period, by default with same from-due date as repayment period
        getInterestPeriods().add(new InterestPeriod(this, getFromDate(), getDueDate(), getZero(), getZero()));
    }

    public RepaymentPeriod(RepaymentPeriod repaymentPeriod,RepaymentPeriod previous) {
        this.previous = previous;
        if(previous != null) {
            previous.setNext(this);
        }
        this.fromDate = repaymentPeriod.fromDate;
        this.dueDate = repaymentPeriod.dueDate;
        this.emi = repaymentPeriod.emi;
        this.rateFactor = repaymentPeriod.rateFactor;
        this.interestPeriods = new ArrayList<>();
        //There is always at least 1 interest period, by default with same from-due date as repayment period
        for (var interestPeriod : repaymentPeriod.interestPeriods) {
            interestPeriods.add(new InterestPeriod(interestPeriod, this));
        }
    }

    public void addInterestPeriod(InterestPeriod interestPeriod) {
        interestPeriods.add(interestPeriod);
        Collections.sort(interestPeriods);
    }

    public Optional<RepaymentPeriod> getPrevious() {
        return Optional.ofNullable(previous);
    }

    public Optional<RepaymentPeriod> getNext() {
        return Optional.ofNullable(next);
    }

    public Money getCalculatedDueInterest() {
        return getInterestPeriods().stream().map(InterestPeriod::getCalculatedDueInterest).reduce(getZero(), Money::plus);
    }

    private Money getZero() {
        //EMI is always initiated
        return getEmi().zero();
    }

    public Money getCalculatedDuePrincipal() {
        return getEmi().minus(getCalculatedDueInterest());
    }

    public boolean isFullyPaid() {
        return getEmi().equals(getPaidPrincipal().plus(getPaidInterest()));
    }

    public Money getDueInterest() {
        return getPaidPrincipal().compareTo(getCalculatedDuePrincipal()) > 0 ? getEmi().minus(getPaidPrincipal()) : getCalculatedDueInterest();
    }

    public Money getDuePrincipal() {
        return getEmi().minus(getDueInterest());
    }

    public Money getUnrecognizedInterest() {
        return getCalculatedDueInterest().minus(getDueInterest());
    }

    public boolean isLastPeriod() {
        return next == null;
    }
}
