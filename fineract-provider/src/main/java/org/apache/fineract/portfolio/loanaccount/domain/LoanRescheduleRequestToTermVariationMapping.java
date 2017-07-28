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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name="m_loan_reschedule_request_term_variations_mapping")
public class LoanRescheduleRequestToTermVariationMapping extends AbstractPersistableCustom<Long> {
    
    
    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "loan_term_variations_id", nullable = false)
    private LoanTermVariations loanTermVariations;
    
    protected LoanRescheduleRequestToTermVariationMapping(){
        
    }

    private LoanRescheduleRequestToTermVariationMapping(final LoanTermVariations loanTermVariations) {
        this.loanTermVariations = loanTermVariations;
    }

    public static LoanRescheduleRequestToTermVariationMapping createNew(final LoanTermVariations loanTermVariation) {
        return new LoanRescheduleRequestToTermVariationMapping(loanTermVariation);
    }
    
    public LoanTermVariations getLoanTermVariations() {
        return this.loanTermVariations;
    }

}
