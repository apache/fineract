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
package org.apache.fineract.organisation.provisioning.data;

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
