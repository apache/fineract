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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

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

		return new EqualsBuilder()
						.append(entities, that.entities)
						.append(statusClient, that.statusClient)
						.append(statusGroup, that.statusGroup)
						.append(statusSavings, that.statusSavings)
						.append(statusLoans, that.statusLoans)
						.append(datatables, that.datatables)
						.append(loanProductDatas, that.loanProductDatas)
						.append(savingsProductDatas, that.savingsProductDatas)
						.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
						.append(entities)
						.append(statusClient)
						.append(statusGroup)
						.append(statusSavings)
						.append(statusLoans)
						.append(datatables)
						.append(loanProductDatas)
						.append(savingsProductDatas)
						.toHashCode();
	}
}