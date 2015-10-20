/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.data;

import java.io.Serializable;
import java.math.BigDecimal;

public final class ProvisioningCriteriaDefinitionData implements Comparable<ProvisioningCriteriaDefinitionData>, Serializable {

    private final Long id;
    private final Long categoryId;
    private final String categoryName;
    private final Long minAge;
    private final Long maxAge;
    private final BigDecimal provisioningPercentage;
    private final Long liabilityAccount;
    private final Long expenseAccount;
    private final String liabilityCode;
    private final String expenseCode;

    public ProvisioningCriteriaDefinitionData(Long id, Long categoryId, String categoryName, Long minAge, Long maxAge,
            BigDecimal provisioningPercentage, Long liabilityAccount, Long expenseAccount, final String liabilityCode,
            final String expenseCode) {
        this.id = id;
        this.categoryId = categoryId;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.provisioningPercentage = provisioningPercentage;
        this.liabilityAccount = liabilityAccount;
        this.expenseAccount = expenseAccount;
        this.categoryName = categoryName;
        this.liabilityCode = liabilityCode;
        this.expenseCode = expenseCode;

    }

    public static ProvisioningCriteriaDefinitionData template(Long categoryId, String categoryName) {
        return new ProvisioningCriteriaDefinitionData(null, categoryId, categoryName, null, null, null, null, null, null, null);
    }

    public Long getId() {
        return this.id;
    }

    public Long getCategoryId() {
        return this.categoryId;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public Long getMinAge() {
        return this.minAge;
    }

    public Long getMaxAge() {
        return this.maxAge;
    }

    public BigDecimal getProvisioningPercentage() {
        return this.provisioningPercentage;
    }

    public Long getLiabilityAccount() {
        return this.liabilityAccount;
    }

    public Long getExpenseAccount() {
        return this.expenseAccount;
    }

    public String getLiabilityCode() {
        return this.liabilityCode;
    }

    public String getExpenseCode() {
        return this.expenseCode;
    }

    @Override
    public int compareTo(ProvisioningCriteriaDefinitionData obj) {
        if (obj == null) { return -1; }
        return obj.id.compareTo(this.id);
    }

}
