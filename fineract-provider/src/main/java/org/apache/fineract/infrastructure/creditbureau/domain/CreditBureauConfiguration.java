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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_creditbureau_configuration")
public class CreditBureauConfiguration extends AbstractPersistableCustom<Long> {
	@Column(name = "configkey")
	private String configurationKey;

	@Column(name = "value")
	private String value;

	@Column(name = "description")
	private String description;

	@ManyToOne
	@JoinColumn(name = "organisation_creditbureau_id")
	private OrganisationCreditBureau organisationCreditbureau;

	public CreditBureauConfiguration() {

	}

	public CreditBureauConfiguration(String configkey, String value, String description,
			OrganisationCreditBureau organisationCreditbureau) {
		this.configurationKey = configkey;
		this.value = value;
		this.description = description;
		this.organisationCreditbureau = organisationCreditbureau;

	}

	public CreditBureauConfiguration fromJson(JsonCommand command, OrganisationCreditBureau organisation_creditbureau) {
		final String configkey = command.stringValueOfParameterNamed("configkey");
		final String value = command.stringValueOfParameterNamed("value");
		final String description = command.stringValueOfParameterNamed("description");

		return new CreditBureauConfiguration(configkey, value, description, organisation_creditbureau);

	}

	public String getConfigkey() {
		return this.configurationKey;
	}

	public void setConfigkey(String configkey) {
		this.configurationKey = configkey;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public OrganisationCreditBureau getOrganisation_creditbureau() {
		return this.organisationCreditbureau;
	}

	public void setOrganisation_creditbureau(OrganisationCreditBureau organisation_creditbureau) {
		this.organisationCreditbureau = organisation_creditbureau;
	}

}
