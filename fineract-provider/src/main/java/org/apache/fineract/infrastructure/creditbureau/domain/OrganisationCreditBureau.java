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
package org.apache.fineract.infrastructure.creditbureau.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_organisation_creditbureau")
public class OrganisationCreditBureau extends AbstractPersistableCustom<Long> {

	private String alias;

	@OneToOne
	private CreditBureau creditbureau;

	private boolean is_active;

	@OneToMany(mappedBy = "organisation_creditbureau", cascade = CascadeType.ALL)
	private List<CreditBureauLoanProductMapping> creditBureauLoanProductMapping = new ArrayList<>();

	public OrganisationCreditBureau(String alias, CreditBureau creditBureau, boolean is_active,
			List<CreditBureauLoanProductMapping> creditBureauLoanProductMapping) {
		this.alias = alias;
		this.creditbureau = creditBureau;
		this.is_active = is_active;
		this.creditBureauLoanProductMapping = creditBureauLoanProductMapping;
	}

	public OrganisationCreditBureau() {

	}

	public static OrganisationCreditBureau fromJson(final JsonCommand command, CreditBureau creditBureau) {
		final String alias = command.stringValueOfParameterNamed("alias");
		final boolean is_active = command.booleanPrimitiveValueOfParameterNamed("is_active");

		return new OrganisationCreditBureau(alias, creditBureau, is_active, null);
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public CreditBureau getCreditBureau() {
		return this.creditbureau;
	}

	public void setCreditBureau(CreditBureau creditBureau) {
		this.creditbureau = creditBureau;
	}

	public boolean isActive() {
		return this.is_active;
	}

	public void setIsActive(boolean is_active) {
		this.is_active = is_active;
	}

	public List<CreditBureauLoanProductMapping> getCreditBureauLoanProductMapping() {
		return this.creditBureauLoanProductMapping;
	}

	public void setCreditBureauLoanProductMapping(List<CreditBureauLoanProductMapping> creditBureauLoanProductMapping) {
		this.creditBureauLoanProductMapping = creditBureauLoanProductMapping;
	}

	

}
