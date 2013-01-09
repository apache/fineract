package org.mifosplatform.accounting.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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

    public ProductToGLAccountMapping(GLAccount glAccount, Long productId, int productType, int financialAccountType) {
        this.glAccount = glAccount;
        this.productId = productId;
        this.productType = productType;
        this.financialAccountType = financialAccountType;
    }

    public GLAccount getGlAccount() {
        return this.glAccount;
    }

    public void setGlAccount(GLAccount glAccount) {
        this.glAccount = glAccount;
    }

    public Long getProductId() {
        return this.productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getProductType() {
        return this.productType;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public int getFinancialAccountType() {
        return this.financialAccountType;
    }

    public void setFinancialAccountType(int financialAccountType) {
        this.financialAccountType = financialAccountType;
    }

}