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

package org.apache.fineract.portfolio.loanaccount.guarantor.data;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class ObligeeData {

	private final String firstName;
	private final String lastName;
	private final String displayName;
	private final String accountNumber;
	private final BigDecimal loanAmount;
	private final BigDecimal guaranteeAmount;
	private final BigDecimal amountReleased;
	private final BigDecimal amountTransferred;

	private ObligeeData(String firstname, String lastname, String displayName, String accountNumber,
			BigDecimal loanAmount, BigDecimal guaranteeAmount, BigDecimal amountReleased,
			BigDecimal amountTransferred) {
		this.firstName = firstname;
		this.lastName = lastname;
		this.displayName = displayName;
		this.accountNumber = accountNumber;
		this.loanAmount = loanAmount;
		this.guaranteeAmount = guaranteeAmount;
		this.amountReleased = amountReleased;
		this.amountTransferred = amountTransferred;
	}

	public static ObligeeData instance(final String firstname, final String lastname, final String displayName,
			final String accountNumber, final BigDecimal loanAmount, final BigDecimal guaranteeAmount,
			final BigDecimal amountReleased, final BigDecimal amountTransferred) {
		return new ObligeeData(firstname, lastname, displayName, accountNumber, loanAmount, guaranteeAmount,
				amountReleased, amountTransferred);
	}

}
