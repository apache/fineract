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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_organisation_creditbureau")
public class OrganisationCreditBureau extends AbstractPersistableCustom<Long> {

    private String alias;

    @OneToOne
    @JoinColumn(name = "creditbureau_id", nullable = false)
    private CreditBureau creditbureau;

    @Column(name = "is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "organisation_creditbureau", cascade = CascadeType.ALL)
    private List<CreditBureauLoanProductMapping> creditBureauLoanProductMapping = new ArrayList<>();

    public OrganisationCreditBureau(String alias, CreditBureau creditbureau, boolean isActive,
            List<CreditBureauLoanProductMapping> creditBureauLoanProductMapping) {
        this.alias = alias;
        this.creditbureau = creditbureau;
        this.isActive = isActive;
        this.creditBureauLoanProductMapping = creditBureauLoanProductMapping;
    }

    public OrganisationCreditBureau() {

    }

    public static OrganisationCreditBureau fromJson(final JsonCommand command, CreditBureau creditbureau) {
        final String alias = command.stringValueOfParameterNamed("alias");
        final boolean isActive = command.booleanPrimitiveValueOfParameterNamed("isActive");

        return new OrganisationCreditBureau(alias, creditbureau, isActive, null);
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

    public void setCreditBureau(CreditBureau creditbureau) {
        this.creditbureau = creditbureau;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public List<CreditBureauLoanProductMapping> getCreditBureauLoanProductMapping() {
        return this.creditBureauLoanProductMapping;
    }

    public void setCreditBureauLoanProductMapping(List<CreditBureauLoanProductMapping> creditBureauLoanProductMapping) {
        this.creditBureauLoanProductMapping = creditBureauLoanProductMapping;
    }

}
