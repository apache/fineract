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
package org.apache.fineract.infrastructure.dataqueries.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;

/**
 * Immutable data object for role data.
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EntityDataTableChecksTemplateData implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> entities;
    private List<DatatableCheckStatusData> statusClient;
    private List<DatatableCheckStatusData> statusGroup;
    private List<DatatableCheckStatusData> statusSavings;
    private List<DatatableCheckStatusData> statusLoans;
    private List<DatatableChecksData> datatables;
    private Collection<LoanProductData> loanProductDatas;
    private Collection<SavingsProductData> savingsProductDatas;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof EntityDataTableChecksTemplateData)) {
            return false;
        }

        EntityDataTableChecksTemplateData that = (EntityDataTableChecksTemplateData) o;

        return Objects.equals(entities, that.entities) && Objects.equals(statusClient, that.statusClient)
                && Objects.equals(statusGroup, that.statusGroup) && Objects.equals(statusSavings, that.statusSavings)
                && Objects.equals(statusLoans, that.statusLoans) && Objects.equals(datatables, that.datatables)
                && CollectionUtils.isEqualCollection(loanProductDatas, that.loanProductDatas)
                && CollectionUtils.isEqualCollection(savingsProductDatas, that.savingsProductDatas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entities, statusClient, statusGroup, statusSavings, statusLoans, datatables, loanProductDatas,
                savingsProductDatas);
    }
}
