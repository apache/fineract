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
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;

/**
 * Immutable data object for role data.
 */
public class EntityDataTableChecksTemplateData implements Serializable {

    private final List<String> entities;
    private final List<DatatableCheckStatusData> statusClient;
    private final List<DatatableCheckStatusData> statusGroup;
    private final List<DatatableCheckStatusData> statusSavings;
    private final List<DatatableCheckStatusData> statusLoans;
    private final List<DatatableChecksData> datatables;
    private final Collection<LoanProductData> loanProductDatas;
    private final Collection<SavingsProductData> savingsProductDatas;

    public EntityDataTableChecksTemplateData(final List<String> entities, List<DatatableCheckStatusData> statusClient,
            List<DatatableCheckStatusData> statusGroup, List<DatatableCheckStatusData> statusSavings,
            List<DatatableCheckStatusData> statusLoans, List<DatatableChecksData> datatables,
            Collection<LoanProductData> loanProductDatas, Collection<SavingsProductData> savingsProductDatas) {

        this.entities = entities;
        this.statusClient = statusClient;
        this.statusGroup = statusGroup;
        this.statusSavings = statusSavings;
        this.statusLoans = statusLoans;
        this.datatables = datatables;
        this.loanProductDatas = loanProductDatas;
        this.savingsProductDatas = savingsProductDatas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof EntityDataTableChecksTemplateData)) return false;

        EntityDataTableChecksTemplateData that = (EntityDataTableChecksTemplateData) o;

        return Objects.equals(entities, that.entities) &&
               Objects.equals(statusClient, that.statusClient) &&
                Objects.equals(statusGroup, that.statusGroup) &&
                Objects.equals(statusSavings, that.statusSavings) &&
                Objects.equals(statusLoans, that.statusLoans) &&
                Objects.equals(datatables, that.datatables) &&
                Objects.equals(loanProductDatas, that.loanProductDatas) &&
                Objects.equals(savingsProductDatas, that.savingsProductDatas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entities, statusClient, statusGroup, statusSavings, statusLoans, datatables, loanProductDatas, savingsProductDatas);
    }
}