/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.data;

import java.io.Serializable;

/**
 * Immutable object representing organization's provision category data
 */
public class ProvisioningCategoryData implements Comparable<ProvisioningCategoryData>, Serializable {

    private final Long id;
    private final String categoryName;
    private final String categoryDescription;

    public ProvisioningCategoryData(final Long id, final String categoryName, final String categoryDescription) {
        this.id = id;
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
    }

    public Long getId() {
        return this.id;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public String getCategoryDescription() {
        return this.categoryDescription;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ProvisioningCategoryData)) return false;
        final ProvisioningCategoryData provisionCategoryData = (ProvisioningCategoryData) obj;
        return provisionCategoryData.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public int compareTo(ProvisioningCategoryData obj) {
        if (obj == null) { return -1; }
        return obj.id.compareTo(this.id);
    }
}
