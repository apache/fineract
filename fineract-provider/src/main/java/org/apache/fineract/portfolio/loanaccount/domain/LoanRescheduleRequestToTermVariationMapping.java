package org.apache.fineract.portfolio.loanaccount.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name="m_loan_reschedule_request_term_variations_mapping")
public class LoanRescheduleRequestToTermVariationMapping extends AbstractPersistable<Long> {
    
    
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
