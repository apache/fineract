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
package org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.jetbrains.annotations.NotNull;

@Getter
public class ChangeOperation implements Comparable<ChangeOperation> {

    private final Optional<LoanTermVariationsData> interestRateChange;
    private final Optional<LoanCharge> loanCharge;
    private final Optional<LoanTransaction> loanTransaction;

    public ChangeOperation(LoanCharge loanCharge) {
        this.interestRateChange = Optional.empty();
        this.loanCharge = Optional.of(loanCharge);
        this.loanTransaction = Optional.empty();
    }

    public ChangeOperation(LoanTransaction loanTransaction) {
        this.interestRateChange = Optional.empty();
        this.loanTransaction = Optional.of(loanTransaction);
        this.loanCharge = Optional.empty();
    }

    public ChangeOperation(LoanTermVariationsData interestRateChange) {
        this.interestRateChange = Optional.of(interestRateChange);
        this.loanTransaction = Optional.empty();
        this.loanCharge = Optional.empty();
    }

    public boolean isInterestRateChange() {
        return interestRateChange.isPresent();
    }

    public boolean isTransaction() {
        return loanTransaction.isPresent();
    }

    public boolean isCharge() {
        return loanCharge.isPresent();
    }

    private boolean isAccrualActivity() {
        return isTransaction() && loanTransaction.get().isAccrualActivity();
    }

    private boolean isBackdatedCharge() {
        return isCharge() && DateUtils.isBefore(loanCharge.get().getDueDate(), loanCharge.get().getSubmittedOnDate());
    }

    private LocalDate getEffectiveDate() {
        if (interestRateChange.isPresent()) {
            return getSubmittedOnDate();
        } else if (loanCharge.isPresent()) {
            if (isBackdatedCharge()) {
                return loanCharge.get().getDueDate();
            } else {
                return loanCharge.get().getSubmittedOnDate();
            }
        } else if (loanTransaction.isPresent()) {
            return loanTransaction.get().getTransactionDate();
        } else {
            throw new RuntimeException("Either charge or transaction should be present");
        }
    }

    private LocalDate getSubmittedOnDate() {
        if (interestRateChange.isPresent()) {
            return interestRateChange.get().getTermVariationApplicableFrom();
        } else if (loanCharge.isPresent()) {
            return loanCharge.get().getSubmittedOnDate();
        } else if (loanTransaction.isPresent()) {
            return loanTransaction.get().getSubmittedOnDate();
        } else {
            throw new RuntimeException("Either charge or transaction should be present");
        }
    }

    private OffsetDateTime getCreatedDateTime() {
        if (interestRateChange.isPresent()) {
            return DateUtils.getOffsetDateTimeOfTenantFromLocalDate(getSubmittedOnDate());
        } else if (loanCharge.isPresent() && loanCharge.get().getCreatedDate().isPresent()) {
            return loanCharge.get().getCreatedDate().get();
        } else if (loanTransaction.isPresent()) {
            return loanTransaction.get().getCreatedDate().orElse(null);
        } else {
            throw new RuntimeException("Either charge with createdDate or transaction created datetime should be present");
        }
    }

    @Override
    @SuppressFBWarnings(value = "EQ_COMPARETO_USE_OBJECT_EQUALS", justification = "TODO: fix this! See: https://stackoverflow.com/questions/2609037/findbugs-how-to-solve-eq-compareto-use-object-equals")
    public int compareTo(@NotNull ChangeOperation o) {
        int datePortion = DateUtils.compareWithNullsLast(this.getEffectiveDate(), o.getEffectiveDate());
        if (datePortion == 0) {
            final boolean isAccrual = isAccrualActivity();
            if (isAccrual != o.isAccrualActivity()) {
                return isAccrual ? 1 : -1;
            }
            int submittedDate = DateUtils.compareWithNullsLast(getSubmittedOnDate(), o.getSubmittedOnDate());
            if (submittedDate == 0) {
                return DateUtils.compareWithNullsLast(getCreatedDateTime(), o.getCreatedDateTime());
            }
            return submittedDate;
        }
        return datePortion;
    }

}
