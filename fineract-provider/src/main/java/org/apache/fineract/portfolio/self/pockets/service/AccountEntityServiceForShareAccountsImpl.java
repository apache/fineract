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
import org.apache.fineract.portfolio.accounts.exceptions.ShareAccountNotFoundException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.self.shareaccounts.service.AppUserShareAccountsMapperReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountEntityServiceForShareAccountsImpl implements AccountEntityService {

	private final String KEY = EntityAccountType.SHARES.name();

	private final PlatformSecurityContext context;
	private final AppUserShareAccountsMapperReadPlatformService appUserShareAccountsMapperReadPlatformService;
	private final ShareAccountReadPlatformService shareAccountReadPlatformService;


	@Autowired
	public AccountEntityServiceForShareAccountsImpl(final PlatformSecurityContext context,
			final AppUserShareAccountsMapperReadPlatformService appUserShareAccountsMapperReadPlatformService,
			final ShareAccountReadPlatformService shareAccountReadPlatformService) {
		this.context = context;
		this.appUserShareAccountsMapperReadPlatformService = appUserShareAccountsMapperReadPlatformService;
		this.shareAccountReadPlatformService = shareAccountReadPlatformService;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public void validateSelfUserAccountMapping(Long accountId) {
		if (!this.appUserShareAccountsMapperReadPlatformService.isShareAccountsMappedToUser(accountId,
				this.context.getAuthenticatedUserIfPresent().getId())) {
			throw new ShareAccountNotFoundException(accountId);

		}
	}

	@Override
	public String retrieveAccountNumberByAccountId(Long accountId) {
		return this.shareAccountReadPlatformService.retrieveAccountNumberByAccountId(accountId);
	}

}
