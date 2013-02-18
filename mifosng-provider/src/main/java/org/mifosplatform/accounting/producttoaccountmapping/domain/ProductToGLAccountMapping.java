/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "acc_product_mapping", uniqueConstraints = { @UniqueConstraint(columnNames = { "product_id", "product_type",
        "financial_account_type" }, name = "financial_action") })
public class ProductToGLAccountMapping extends AbstractPersistable<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_account_id")
    private GLAccount glAccount;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_type", nullable = false)
    private int productType;

    @Column(name = "financial_account_type", nullable = false)
    private int financialAccountType;

    public static ProductToGLAccountMapping createNew(final GLAccount glAccount, final Long productId, final int productType,
            final int financialAccountType) {
        return new ProductToGLAccountMapping(glAccount, productId, productType, financialAccountType);
    }

    protected ProductToGLAccountMapping() {
        //
    }

    public ProductToGLAccountMapping(final GLAccount glAccount, final Long productId, final int productType, final int financialAccountType) {
        this.glAccount = glAccount;
        this.productId = productId;
        this.productType = productType;
        this.financialAccountType = financialAccountType;
    }

    public GLAccount getGlAccount() {
        return this.glAccount;
    }

    public void setGlAccount(final GLAccount glAccount) {
        this.glAccount = glAccount;
    }

    public Long getProductId() {
        return this.productId;
    }

    public void setProductId(final Long productId) {
        this.productId = productId;
    }

    public int getProductType() {
        return this.productType;
    }

    public void setProductType(final int productType) {
        this.productType = productType;
    }

    public int getFinancialAccountType() {
        return this.financialAccountType;
    }

    public void setFinancialAccountType(final int financialAccountType) {
        this.financialAccountType = financialAccountType;
    }

}