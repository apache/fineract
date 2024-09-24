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
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.jetbrains.annotations.NotNull;

@Getter
public class ChargeOrTransaction implements Comparable<ChargeOrTransaction> {

    private final Optional<LoanCharge> loanCharge;
    private final Optional<LoanTransaction> loanTransaction;

    public ChargeOrTransaction(LoanCharge loanCharge) {
        this.loanCharge = Optional.of(loanCharge);
        this.loanTransaction = Optional.empty();
    }

    public ChargeOrTransaction(LoanTransaction loanTransaction) {
        this.loanTransaction = Optional.of(loanTransaction);
        this.loanCharge = Optional.empty();
    }

    public boolean isTransaction() {
        return loanTransaction.isPresent();
    }

    public boolean isCharge() {
        return loanCharge.isPresent();
    }

    private LocalDate getEffectiveDate() {
        if (loanCharge.isPresent()) {
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

    private boolean isAccrualActivity() {
        return isTransaction() && loanTransaction.get().isAccrualActivity();
    }

    private boolean isBackdatedCharge() {
        return isCharge() && DateUtils.isBefore(loanCharge.get().getDueDate(), loanCharge.get().getSubmittedOnDate());
    }

    private LocalDate getSubmittedOnDate() {
        if (loanCharge.isPresent()) {
            return loanCharge.get().getSubmittedOnDate();
        } else if (loanTransaction.isPresent()) {
            return loanTransaction.get().getSubmittedOnDate();
        } else {
            throw new RuntimeException("Either charge or transaction should be present");
        }
    }

    private OffsetDateTime getCreatedDateTime() {
        if (loanCharge.isPresent() && loanCharge.get().getCreatedDate().isPresent()) {
            return loanCharge.get().getCreatedDate().get();
        } else if (loanTransaction.isPresent()) {
            return loanTransaction.get().getCreatedDateTime();
        } else {
            throw new RuntimeException("Either charge with createdDate or transaction created datetime should be present");
        }
    }

    @Override
    @SuppressFBWarnings(value = "EQ_COMPARETO_USE_OBJECT_EQUALS", justification = "TODO: fix this! See: https://stackoverflow.com/questions/2609037/findbugs-how-to-solve-eq-compareto-use-object-equals")
    public int compareTo(@NotNull ChargeOrTransaction o) {
        int datePortion = DateUtils.compare(this.getEffectiveDate(), o.getEffectiveDate());
        if (datePortion == 0) {
            boolean isAccrual = isAccrualActivity();
            if (isAccrual != o.isAccrualActivity()) {
                return isAccrual ? 1 : -1;
            }
            int submittedDate = DateUtils.compare(getSubmittedOnDate(), o.getSubmittedOnDate());
            if (submittedDate == 0) {
                return DateUtils.compare(getCreatedDateTime(), o.getCreatedDateTime());
            }
            return submittedDate;
        }
        return datePortion;
    }

}
