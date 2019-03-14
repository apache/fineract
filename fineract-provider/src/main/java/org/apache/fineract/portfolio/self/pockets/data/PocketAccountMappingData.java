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

package org.apache.fineract.portfolio.self.pockets.data;

import java.util.Collection;

import org.apache.fineract.portfolio.self.pockets.domain.PocketAccountMapping;

@SuppressWarnings("unused")
public class PocketAccountMappingData {

	private final Collection<PocketAccountMapping> loanAccounts;
	private final Collection<PocketAccountMapping> savingsAccounts;
	private final Collection<PocketAccountMapping> shareAccounts;

	private PocketAccountMappingData(final Collection<PocketAccountMapping> loanAccounts, Collection<PocketAccountMapping> savingsAccounts, final Collection<PocketAccountMapping> shareAccounts ) {
		this.loanAccounts = loanAccounts;
		this.savingsAccounts = savingsAccounts;
		this.shareAccounts = shareAccounts;
	}

	public static PocketAccountMappingData instance(final Collection<PocketAccountMapping> loanAccounts, final Collection<PocketAccountMapping> savingsAccounts, final Collection<PocketAccountMapping> shareAccounts) {

		return new PocketAccountMappingData(loanAccounts, savingsAccounts, shareAccounts);

	}

}
