/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTermVariationsData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_term_variations")
public class LoanTermVariations extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "term_type", nullable = false)
    private Integer termType;

    @Temporal(TemporalType.DATE)
    @Column(name = "applicable_from")
    private Date termApplicableFrom;

    @Column(name = "term_value", scale = 6, precision = 19)
    private BigDecimal termValue;

    public LoanTermVariations(final Integer termType, final Date termApplicableFrom, final BigDecimal termValue, final Loan loan) {

        this.loan = loan;
        this.termApplicableFrom = termApplicableFrom;
        this.termType = termType;
        this.termValue = termValue;
    }

    protected LoanTermVariations() {

    }

    public LoanTermVariationType getTermType() {
        return LoanTermVariationType.fromInt(this.termType);
    }

    public LoanTermVariationsData toData() {
        LocalDate termStartDate = new LocalDate(this.termApplicableFrom);
        EnumOptionData type = LoanEnumerations.loanvariationType(this.termType);
        return new LoanTermVariationsData(getId(), type, termStartDate, this.termValue);
    }

    public Date getTermApplicableFrom() {
        return this.termApplicableFrom;
    }

    public BigDecimal getTermValue() {
        return this.termValue;
    }

}
