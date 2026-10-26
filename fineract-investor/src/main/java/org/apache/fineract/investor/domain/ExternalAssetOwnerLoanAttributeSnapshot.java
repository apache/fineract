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
package org.apache.fineract.investor.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "m_external_asset_owner_loan_attribute_snapshot")
public class ExternalAssetOwnerLoanAttributeSnapshot extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "loan_id", nullable = false, unique = true, updatable = false)
    private Long loanId;

    @Column(name = "external_asset_owner_transfer_id", nullable = false, updatable = false)
    private Long externalAssetOwnerTransferId;

    @OrderBy("id")
    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExternalAssetOwnerLoanAttributes> attributes = new ArrayList<>();

    public ExternalAssetOwnerLoanAttributeSnapshot(final Long loanId, final Long externalAssetOwnerTransferId) {
        this.loanId = loanId;
        this.externalAssetOwnerTransferId = externalAssetOwnerTransferId;
    }

    public void putAttribute(final String attributeKey, final String attributeValue) {
        attributes.stream().filter(attribute -> attribute.getAttributeKey().equals(attributeKey)).findFirst().ifPresentOrElse(
                attribute -> attribute.setAttributeValue(attributeValue),
                () -> attributes.add(new ExternalAssetOwnerLoanAttributes(this, attributeKey, attributeValue)));
    }
}
