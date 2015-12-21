/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.joda.time.LocalDate;

public class LoanTermVariationsDataWrapper {

    private final List<LoanTermVariationsData> exceptionData;
    private final ListIterator<LoanTermVariationsData> iterator;
    private final List<LoanTermVariationsData> interestRateChanges;
    private final List<LoanTermVariationsData> dueDateVariation;
    private final ListIterator<LoanTermVariationsData> dueDateIterator;

    public LoanTermVariationsDataWrapper(final List<LoanTermVariationsData> exceptionData) {
        if (exceptionData == null) {
            this.exceptionData = new ArrayList<>(1);
        } else {
            this.exceptionData = exceptionData;
            Collections.sort(this.exceptionData);
        }
        this.interestRateChanges = new ArrayList<>();
        this.dueDateVariation = new ArrayList<>();
        for (LoanTermVariationsData loanTermVariationsData : this.exceptionData) {
            if (loanTermVariationsData.getTermVariationType().isInterestRateVariation()) {
                this.interestRateChanges.add(loanTermVariationsData);
            } else if (loanTermVariationsData.getTermVariationType().isDueDateVariation()) {
                this.dueDateVariation.add(loanTermVariationsData);
            }
        }
        this.exceptionData.removeAll(this.interestRateChanges);
        this.exceptionData.removeAll(this.dueDateVariation);
        iterator = this.exceptionData.listIterator();
        dueDateIterator = this.dueDateVariation.listIterator();
    }

    public boolean hasVariation(final LocalDate date) {
        ListIterator<LoanTermVariationsData> iterator = this.iterator;
        return hasNext(date, iterator);
    }

    private boolean hasNext(final LocalDate date, ListIterator<LoanTermVariationsData> iterator) {
        boolean hasVariation = false;
        if (iterator.hasNext()) {
            LoanTermVariationsData loanTermVariationsData = iterator.next();
            if (!loanTermVariationsData.getTermApplicableFrom().isAfter(date)) {
                hasVariation = true;
            }
            iterator.previous();
        }
        return hasVariation;
    }

    public boolean hasDueDateVariation(final LocalDate date) {
        ListIterator<LoanTermVariationsData> iterator = this.dueDateIterator;
        return hasNext(date, iterator);
    }

    public LoanTermVariationsData nextVariation() {
        return this.iterator.next();
    }

    public LoanTermVariationsData nextDueDateVariation() {
        return this.dueDateIterator.next();
    }

    public List<LoanTermVariationsData> getInterestRateChanges() {
        return this.interestRateChanges;
    }

    public List<LoanTermVariationsData> getDueDateVariation() {
        return this.dueDateVariation;
    }

    public List<LoanTermVariationsData> getExceptionData() {
        return this.exceptionData;
    }

    public int adjustNumberOfRepayments() {
        int repaymetsForAdjust = 0;
        for (LoanTermVariationsData loanTermVariations : this.exceptionData) {
            if (loanTermVariations.getTermVariationType().isInsertInstallment()) {
                repaymetsForAdjust++;
            } else if (loanTermVariations.getTermVariationType().isDeleteInstallment()) {
                repaymetsForAdjust--;
            }
        }
        return repaymetsForAdjust;
    }

    public LoanTermVariationsData fetchLoanTermDueDateVariationsData(final LocalDate onDate) {
        LoanTermVariationsData data = null;
        for (LoanTermVariationsData termVariationsData : this.dueDateVariation) {
            if (onDate.isEqual(termVariationsData.getTermApplicableFrom())) {
                data = termVariationsData;
                break;
            }
        }
        return data;
    }

}
