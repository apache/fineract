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

import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public class SelfBeneficiariesTPTData {
	@SuppressWarnings("unused")
	private final Long id;
	@SuppressWarnings("unused")
	private final String name;
	@SuppressWarnings("unused")
	private final String officeName;
	@SuppressWarnings("unused")
	private final String clientName;
	@SuppressWarnings("unused")
	private final EnumOptionData accountType;
	@SuppressWarnings("unused")
	private final String accountNumber;
	@SuppressWarnings("unused")
	private final Long transferLimit;
	@SuppressWarnings("unused")
	private final Collection<EnumOptionData> accountTypeOptions;

	public SelfBeneficiariesTPTData(
			final Collection<EnumOptionData> accountTypeOptions) {
		this.accountTypeOptions = accountTypeOptions;
		this.id = null;
		this.name = null;
		this.officeName = null;
		this.clientName = null;
		this.accountType = null;
		this.accountNumber = null;
		this.transferLimit = null;
	}

	public SelfBeneficiariesTPTData(final Long id, final String name,
			final String officeName, final String clientName,
			final EnumOptionData accountType, final String accountNumber,
			final Long transferLimit) {
		this.accountTypeOptions = null;
		this.id = id;
		this.name = name;
		this.officeName = officeName;
		this.clientName = clientName;
		this.accountType = accountType;
		this.accountNumber = accountNumber;
		this.transferLimit = transferLimit;
	}

}
