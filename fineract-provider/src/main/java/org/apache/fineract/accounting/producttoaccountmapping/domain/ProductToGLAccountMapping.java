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
package org.apache.fineract.accounting.producttoaccountmapping.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "acc_product_mapping", uniqueConstraints = { @UniqueConstraint(columnNames = { "product_id", "product_type",
        "financial_account_type", "payment_type" }, name = "financial_action") })
public class ProductToGLAccountMapping extends AbstractPersistableCustom {

    @ManyToOne(optional = true)
    @JoinColumn(name = "gl_account_id")
    private GLAccount glAccount;

    @Column(name = "product_id", nullable = true)
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "payment_type", nullable = true)
    private PaymentType paymentType;

    @ManyToOne
    @JoinColumn(name = "charge_id", nullable = true)
    private Charge charge;

    @Column(name = "product_type", nullable = true)
    private int productType;

    @Column(name = "financial_account_type", nullable = true)
    private int financialAccountType;

    public static ProductToGLAccountMapping createNew(final GLAccount glAccount, final Long productId, final int productType,
            final int financialAccountType) {

        return new ProductToGLAccountMapping().setGlAccount(glAccount).setProductId(productId).setProductType(productType)
                .setFinancialAccountType(financialAccountType);
    }

}
