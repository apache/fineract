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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.joda.time.LocalDate;

public class LoanTermVariationsDataWrapper {

    private final List<LoanTermVariationsData> exceptionData;
    private ListIterator<LoanTermVariationsData> iterator;
    private final List<LoanTermVariationsData> interestRateChanges;
    private final List<LoanTermVariationsData> interestRateFromInstallment;
    private final List<LoanTermVariationsData> dueDateVariation;
    private ListIterator<LoanTermVariationsData> dueDateIterator;

    public LoanTermVariationsDataWrapper(final List<LoanTermVariationsData> exceptionData) {
        if (exceptionData == null) {
            this.exceptionData = new ArrayList<>(1);
        } else {
            this.exceptionData = exceptionData;
        }
        this.interestRateChanges = new ArrayList<>();
        this.dueDateVariation = new ArrayList<>();
        this.interestRateFromInstallment = new ArrayList<>();
        deriveLoanTermVariations();
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
    
    public LoanTermVariationsData previousDueDateVariation() {
        return this.dueDateIterator.previous();
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
    
    public void setExceptionData(final List<LoanTermVariationsData> exceptionData) {
        clearTerms();
        this.exceptionData.addAll(exceptionData);
        deriveLoanTermVariations();
    }

    public void clearTerms() {
        this.exceptionData.clear();
        this.interestRateChanges.clear();
        this.dueDateVariation.clear();
        this.interestRateFromInstallment.clear();
    }
    
    public List<LoanTermVariationsData> getInterestRateFromInstallment() {
        return this.interestRateFromInstallment;
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

    public boolean hasExceptionVariation(final LocalDate date, ListIterator<LoanTermVariationsData> exceptionDataListIterator) {
        ListIterator<LoanTermVariationsData> iterator = exceptionDataListIterator;
        return hasNext(date, iterator);
    }

    public void updateLoanTermVariationsData(final List<LoanTermVariationsData> exceptionData){
        if(this.exceptionData != null && exceptionData != null && exceptionData.size() > 0){
            this.exceptionData.addAll(exceptionData);
            deriveLoanTermVariations();
        }
    }
    
    private void deriveLoanTermVariations() {
        Collections.sort(this.exceptionData);
        for (LoanTermVariationsData loanTermVariationsData : this.exceptionData) {
            if (loanTermVariationsData.getTermVariationType().isInterestRateVariation()) {
                this.interestRateChanges.add(loanTermVariationsData);
            } else if (loanTermVariationsData.getTermVariationType().isDueDateVariation()) {
                this.dueDateVariation.add(loanTermVariationsData);
            } else if (loanTermVariationsData.getTermVariationType().isInterestRateFromInstallment()) {
                this.interestRateFromInstallment.add(loanTermVariationsData);
            }
        }
        Collections.sort(this.dueDateVariation);
        this.exceptionData.removeAll(this.interestRateChanges);
        this.exceptionData.removeAll(this.dueDateVariation);
        this.exceptionData.removeAll(this.interestRateFromInstallment);
        this.iterator = this.exceptionData.listIterator();
        this.dueDateIterator = this.dueDateVariation.listIterator();
    }
    
    public void resetVariations(){
        
        for (LoanTermVariationsData loanTermVariationsData : this.exceptionData) {
            loanTermVariationsData.setProcessed(false);
        }
        this.iterator = this.exceptionData.listIterator();
        this.dueDateIterator = this.dueDateVariation.listIterator();
    }

}
