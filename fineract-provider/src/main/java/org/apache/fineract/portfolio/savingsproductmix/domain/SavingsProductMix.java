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
package org.apache.fineract.portfolio.savingsproductmix.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_savings_product_mix")
public class SavingsProductMix extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private SavingsProduct product;

    @ManyToOne
    @JoinColumn(name = "restricted_product_id", nullable = false)
    private SavingsProduct restrictedProduct;

    public SavingsProductMix() {
        //
    }

    private SavingsProductMix(final SavingsProduct product, final SavingsProduct restrictedProduct) {
        this.product = product;
        this.restrictedProduct = restrictedProduct;
    }

    public static SavingsProductMix createNew(final SavingsProduct product, final SavingsProduct restrictedProduct) {
        return new SavingsProductMix(product, restrictedProduct);
    }

    public Long getRestrictedProductId() {
        return this.restrictedProduct.getId();
    }

    public Long getProductId() {
        return this.product.getId();
    }

}
