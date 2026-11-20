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
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class ProvisioningCriteriaDefinitionData implements Comparable<ProvisioningCriteriaDefinitionData>, Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long minAge;
    private Long maxAge;
    private BigDecimal provisioningPercentage;
    private Long liabilityAccount;
    private String liabilityCode;
    private String liabilityName;
    private Long expenseAccount;
    private String expenseCode;
    private String expenseName;

    public static ProvisioningCriteriaDefinitionData template(Long categoryId, String categoryName) {
        return new ProvisioningCriteriaDefinitionData().setCategoryId(categoryId).setCategoryName(categoryName);
    }

    @Override
    public int compareTo(ProvisioningCriteriaDefinitionData obj) {
        if (obj == null) {
            return -1;
        }
        return obj.id.compareTo(this.id);
    }

}
