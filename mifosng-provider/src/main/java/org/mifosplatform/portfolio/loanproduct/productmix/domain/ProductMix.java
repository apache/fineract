package org.mifosplatform.portfolio.loanproduct.productmix.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_product_mix")
public class ProductMix extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct product;

    @ManyToOne
    @JoinColumn(name = "restricted_product_id", nullable = false)
    private LoanProduct restrictedProduct;

    public ProductMix() {
        //
    }

    private ProductMix(final LoanProduct product, final LoanProduct restrictedProduct) {
        this.product = product;
        this.restrictedProduct = restrictedProduct;
    }

    public static ProductMix createNew(final LoanProduct product, final LoanProduct restrictedProduct) {
        return new ProductMix(product, restrictedProduct);
    }

    public Long getRestrictedProductId() {
        return this.restrictedProduct.getId();
    }

    public Long getProductId() {
        return this.product.getId();
    }

}
