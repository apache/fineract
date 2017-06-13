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

public class OrganisationCreditBureauData {

	private final long organisationCreditBureauId;

	private final String alias;

	private final long creditBureauId;

	private final String creditBureauName;

	private final String creditBureauProduct;

	private final String creditBureauCountry;

	private final String creditBureauSummary;

	private final boolean is_active;

	private OrganisationCreditBureauData(final long organisationCreditBureauId, final String alias,
			final long creditBureauId, final String creditBureauName, final String creditBureauProduct,
			final String creditBureauCountry, final String creditBureauSummary, final boolean is_active) {
		this.organisationCreditBureauId = organisationCreditBureauId;
		this.alias = alias;
		this.creditBureauId = creditBureauId;
		this.creditBureauName = creditBureauName;
		this.creditBureauProduct = creditBureauProduct;
		this.creditBureauCountry = creditBureauCountry;
		this.creditBureauSummary = creditBureauSummary;
		this.is_active = is_active;
	}

	public static OrganisationCreditBureauData instance(final long organisationCreditBureauId, final String alias,
			final long creditBureauId, final String creditBureauName, final String creditBureauProduct,
			final String creditBureauCountry, final String creditBureauSummary, final boolean is_active) {
		return new OrganisationCreditBureauData(organisationCreditBureauId, alias, creditBureauId, creditBureauName,
				creditBureauProduct, creditBureauCountry, creditBureauSummary, is_active);
	}

	public long getOrganisationCreditBureauId() {
		return this.organisationCreditBureauId;
	}

	public String getCreditBureauName() {
		return this.creditBureauName;
	}

	public String getCreditBureauProduct() {
		return this.creditBureauProduct;
	}

	public String getCreditBureauCountry() {
		return this.creditBureauCountry;
	}

	public String getCreditBureauSummary() {
		return this.creditBureauSummary;
	}

	public String getAlias() {
		return this.alias;
	}

	public long getCreditBureauId() {
		return this.creditBureauId;
	}

	public boolean isActive() {
		return this.is_active;
	}

}
