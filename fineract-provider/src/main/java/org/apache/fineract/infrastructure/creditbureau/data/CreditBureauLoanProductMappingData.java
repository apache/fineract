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
package org.apache.fineract.infrastructure.creditbureau.data;

public class CreditBureauLoanProductMappingData {

	private final long creditbureauLoanProductMappingId;

	private final long organisationCreditBureauId;

	private final String alias;

	private final String creditbureauSummary;

	private final String loanProductName;

	private final long loanProductId;

	private final boolean isCreditCheckMandatory;

	private final boolean skipCrediCheckInFailure;

	private final long stalePeriod;

	private final boolean is_active;

	private CreditBureauLoanProductMappingData(final long creditbureauLoanProductMappingId,
			final long organisationCreditBureauId, final String alias, final String creditbureauSummary,
			final String loanProductName, final long loanProductId, final boolean isCreditCheckMandatory,
			final boolean skipCrediCheckInFailure, final long stalePeriod, final boolean is_active) {
		this.creditbureauLoanProductMappingId = creditbureauLoanProductMappingId;
		this.organisationCreditBureauId = organisationCreditBureauId;
		this.alias = alias;
		this.creditbureauSummary = creditbureauSummary;
		this.loanProductName = loanProductName;
		this.loanProductId = loanProductId;
		this.isCreditCheckMandatory = isCreditCheckMandatory;
		this.skipCrediCheckInFailure = skipCrediCheckInFailure;
		this.stalePeriod = stalePeriod;
		this.is_active = is_active;
	}

	public static CreditBureauLoanProductMappingData instance(final long creditbureauLoanProductMappingId,
			final long organisationCreditBureauId, final String alias, final String creditbureauSummary,
			final String loanProductName, final long loanProductId, final boolean isCreditCheckMandatory,
			final boolean skipCrediCheckInFailure, final long stalePeriod, final boolean is_active) {
		return new CreditBureauLoanProductMappingData(creditbureauLoanProductMappingId, organisationCreditBureauId, alias,
				creditbureauSummary, loanProductName, loanProductId, isCreditCheckMandatory, skipCrediCheckInFailure,
				stalePeriod, is_active);
	}

	public static CreditBureauLoanProductMappingData instance1(final String loanProductName, final long loanProductId) {
		return new CreditBureauLoanProductMappingData(0, 0, "", "", loanProductName, loanProductId, false, false, 0, false);
	}

	public long getCreditbureauLoanProductMappingId() {
		return this.creditbureauLoanProductMappingId;
	}

	public String getAlias() {
		return this.alias;
	}

	public String getCreditbureauSummary() {
		return this.creditbureauSummary;
	}

	public String getLoanProductName() {
		return this.loanProductName;
	}

	public long getOrganisationCreditBureauId() {
		return this.organisationCreditBureauId;
	}

	public long getLoanProductId() {
		return this.loanProductId;
	}

	public boolean isCreditCheckMandatory() {
		return this.isCreditCheckMandatory;
	}

	public boolean isSkipCrediCheckInFailure() {
		return this.skipCrediCheckInFailure;
	}

	public long getStalePeriod() {
		return this.stalePeriod;
	}

	public boolean isIs_active() {
		return this.is_active;
	}

}
