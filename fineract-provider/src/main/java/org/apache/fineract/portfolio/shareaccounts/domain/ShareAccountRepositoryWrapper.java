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
package org.apache.fineract.portfolio.shareaccounts.domain;

import org.apache.fineract.portfolio.accounts.exceptions.ShareAccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShareAccountRepositoryWrapper {

	private final ShareAccountRepository shareAccountRepository ;

	@Autowired
	public ShareAccountRepositoryWrapper(final ShareAccountRepository shareAccountRepository) {
		this.shareAccountRepository = shareAccountRepository ;
	}
	
	public ShareAccount findOneWithNotFoundDetection(final Long accountId) {
		ShareAccount account = this.shareAccountRepository.findOne(accountId) ;
		if(account == null) {
			throw new ShareAccountNotFoundException(accountId) ;
		}
		return account ;
	}
	public void save(final ShareAccount shareAccount) {
		this.shareAccountRepository.save(shareAccount) ;
	}
	
	public void saveAndFlush(final ShareAccount shareAccount) {
		this.shareAccountRepository.saveAndFlush(shareAccount) ;
	}
}
