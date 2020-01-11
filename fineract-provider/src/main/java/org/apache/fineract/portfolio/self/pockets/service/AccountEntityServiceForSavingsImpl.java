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

package org.apache.fineract.portfolio.self.pockets.service;

import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.self.savings.service.AppuserSavingsMapperReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountEntityServiceForSavingsImpl implements AccountEntityService {

	private final String KEY = EntityAccountType.SAVINGS.name();

	private final PlatformSecurityContext context;
	private final AppuserSavingsMapperReadService appuserSavingsMapperReadService;
	private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;

	@Autowired
	public AccountEntityServiceForSavingsImpl(final PlatformSecurityContext context,
			final AppuserSavingsMapperReadService appuserSavingsMapperReadService,
			final SavingsAccountReadPlatformService savingsAccountReadPlatformService) {

		this.context = context;
		this.appuserSavingsMapperReadService = appuserSavingsMapperReadService;
		this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;

	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void validateSelfUserAccountMapping(Long accountId) {

		if (!this.appuserSavingsMapperReadService.isSavingsMappedToUser(accountId,
				this.context.getAuthenticatedUserIfPresent().getId())) {
			throw new SavingsAccountNotFoundException(accountId);

		}
	}

	@Override
	public String retrieveAccountNumberByAccountId(Long accountId) {
		return this.savingsAccountReadPlatformService.retrieveAccountNumberByAccountId(accountId);
	}

}
