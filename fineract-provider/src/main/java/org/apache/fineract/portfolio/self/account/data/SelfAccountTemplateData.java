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
package org.apache.fineract.portfolio.self.account.data;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.account.service.AccountTransferEnumerations;

@SuppressWarnings("unused")
public class SelfAccountTemplateData implements
		Comparable<SelfAccountTemplateData> {

	private final Long accountId;
	private final String accountNo;
	private final EnumOptionData accountType;
	private final Long clientId;
	private final String clientName;
	private final Long officeId;
	private final String officeName;

	public SelfAccountTemplateData(final Long accountId,
			final String accountNo, final Integer accountType,
			final Long clientId, final String clientName, final Long officeId,
			final String officeName) {
		this.accountId = accountId;
		this.accountNo = accountNo;
		this.accountType = AccountTransferEnumerations.accountType(accountType);
		this.clientId = clientId;
		this.clientName = clientName;
		this.officeId = officeId;
		this.officeName = officeName;
	}

	public SelfAccountTemplateData(final Long accountId,
			final Integer accountType, final Long clientId, final Long officeId) {
		this.accountId = accountId;
		this.accountNo = null;
		this.accountType = AccountTransferEnumerations.accountType(accountType);
		this.clientId = clientId;
		this.clientName = null;
		this.officeId = officeId;
		this.officeName = null;
	}

	@Override
	public int compareTo(final SelfAccountTemplateData obj) {
		if (obj == null) {
			return -1;
		}
		return new CompareToBuilder() //
				.append(this.accountId, obj.accountId) //
				.append(this.accountType.getValue(), obj.accountType.getValue()) //
				.append(this.clientId, obj.clientId) //
				.append(this.officeId, obj.officeId) //
				.toComparison();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		final SelfAccountTemplateData rhs = (SelfAccountTemplateData) obj;
		return new EqualsBuilder() //
				.append(this.accountId, rhs.accountId) //
				.append(this.accountType.getValue(), rhs.accountType.getValue()) //
				.append(this.clientId, rhs.clientId) //
				.append(this.officeId, rhs.officeId) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37) //
				.append(this.accountId) //
				.append(this.accountType.getValue()) //
				.append(this.clientId) //
				.append(this.officeId) //
				.toHashCode();
	}

	public Long getAccountId() {
		return this.accountId;
	}

	public Integer getAccountType() {
		return this.accountType.getId().intValue();
	}

	
}
