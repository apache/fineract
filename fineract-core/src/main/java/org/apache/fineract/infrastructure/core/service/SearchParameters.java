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
package org.apache.fineract.infrastructure.core.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Builder
@Getter
@AllArgsConstructor
public class SearchParameters {

    public static final int DEFAULT_MAX_LIMIT = 200;

    private Long officeId;
    private String externalId;
    private String name;
    private String hierarchy;
    private String firstname;
    private String lastname;
    private String status;
    private Integer offset;
    @Getter(AccessLevel.NONE)
    private Integer limit;
    private String orderBy;
    private String sortOrder;
    private String accountNo;
    private String currencyCode;
    private Long staffId;
    private Long loanId;
    private Long savingsId;
    @Getter(AccessLevel.NONE)
    private Boolean orphansOnly;
    private Long provisioningEntryId;
    private Long productId;
    private Long categoryId;
    @Getter(AccessLevel.NONE)
    private Boolean isSelfUser;

    public Integer getLimit() {
        if (limit == null) {
            return DEFAULT_MAX_LIMIT;
        }

        if (limit > 0) {
            return limit;
        }

        return null; // unlimited (0 or less)
    }

    public Boolean getOrphansOnly() {
        return Boolean.TRUE.equals(orphansOnly);
    }

    public Boolean getIsSelfUser() {
        return Boolean.TRUE.equals(isSelfUser);
    }

    public boolean hasOrderBy() {
        return StringUtils.isNotBlank(this.orderBy);
    }

    public boolean hasSortOrder() {
        return StringUtils.isNotBlank(this.sortOrder);
    }

    public boolean hasOfficeId() {
        return this.officeId != null && this.officeId != 0;
    }

    public boolean hasCurrencyCode() {
        return StringUtils.isNotBlank(this.currencyCode);
    }

    public boolean hasLimit() {
        return this.limit != null && this.limit > 0;
    }

    public boolean hasOffset() {
        return this.offset != null;
    }

    public boolean hasHierarchy() {
        return StringUtils.isNotBlank(this.hierarchy);
    }

    public boolean hasStaffId() {
        return this.staffId != null && this.staffId != 0;
    }

    public boolean hasLoanId() {
        return this.loanId != null && this.loanId != 0;
    }

    public boolean hasSavingsId() {
        return this.savingsId != null && this.savingsId != 0;
    }

    public boolean hasProvisioningEntryId() {
        return this.provisioningEntryId != null && this.provisioningEntryId != 0;
    }

    public boolean hasProductId() {
        return this.productId != null && this.productId != 0;
    }

    public boolean hasCategoryId() {
        return this.categoryId != null && this.categoryId != 0;
    }
}
