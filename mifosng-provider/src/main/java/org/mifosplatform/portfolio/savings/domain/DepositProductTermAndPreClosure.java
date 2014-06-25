/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_deposit_product_term_and_preclosure")
public class DepositProductTermAndPreClosure extends AbstractPersistable<Long> {

    @Embedded
    private DepositPreClosureDetail preClosureDetail;

    @Embedded
    protected DepositTermDetail depositTermDetail;

    @OneToOne
    @JoinColumn(name = "savings_product_id", nullable = false)
    private FixedDepositProduct product;

    @Embedded
    private DepositProductAmountDetails depositProductAmountDetails;

    protected DepositProductTermAndPreClosure() {
        super();
    }

    public static DepositProductTermAndPreClosure createNew(DepositPreClosureDetail preClosureDetail, DepositTermDetail depositTermDetail,
            DepositProductAmountDetails depositProductMinMaxAmountDetails, SavingsProduct product) {

        return new DepositProductTermAndPreClosure(preClosureDetail, depositTermDetail, depositProductMinMaxAmountDetails, product);
    }

    private DepositProductTermAndPreClosure(DepositPreClosureDetail preClosureDetail, DepositTermDetail depositTermDetail,
            DepositProductAmountDetails depositProductMinMaxAmountDetails, SavingsProduct product) {
        this.preClosureDetail = preClosureDetail;
        this.depositTermDetail = depositTermDetail;
        this.depositProductAmountDetails = depositProductMinMaxAmountDetails;
        this.product = (FixedDepositProduct) product;
    }

    public Map<String, Object> update(final JsonCommand command, final DataValidatorBuilder baseDataValidator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);
        if (this.preClosureDetail != null) {
            actualChanges.putAll(this.preClosureDetail.update(command, baseDataValidator));
        }

        if (this.depositTermDetail != null) {
            actualChanges.putAll(this.depositTermDetail.update(command, baseDataValidator));
        }

        if (this.depositProductAmountDetails != null) {
            actualChanges.putAll(this.depositProductAmountDetails.update(command));
        }
        return actualChanges;
    }

    public DepositPreClosureDetail depositPreClosureDetail() {
        return this.preClosureDetail;
    }

    public DepositTermDetail depositTermDetail() {
        return this.depositTermDetail;
    }

    public DepositProductAmountDetails depositProductAmountDetails() {
        return this.depositProductAmountDetails;
    }

    public void updateProductReference(final SavingsProduct product) {
        this.product = (FixedDepositProduct) product;
    }
}