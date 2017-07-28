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
package org.apache.fineract.organisation.teller.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.organisation.office.domain.Office;
import org.joda.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_tellers", uniqueConstraints = {
        @UniqueConstraint(name = "ux_tellers_name", columnNames = {"name"})
})
public class Teller extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "debit_account_id", nullable = true)
    private GLAccount debitAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "credit_account_id", nullable = true)
    private GLAccount creditAccount;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = true, length = 500)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "valid_from", nullable = true)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "valid_to", nullable = true)
    private Date endDate;

    @Column(name = "state", nullable = false)
    private Integer status;

    @OneToMany(mappedBy = "teller", fetch = FetchType.LAZY)
    private Set<Cashier> cashiers;

    public Teller() {
        super();
    }
    
    private Teller (final Office staffOffice, final String name, final String description, final LocalDate startDate, 
    		final LocalDate endDate, final TellerStatus status) {
    	
    	this.name = StringUtils.defaultIfEmpty(name, null);
    	this.description = StringUtils.defaultIfEmpty(description, null);
    	if (startDate != null) {
    		this.startDate = startDate.toDateTimeAtStartOfDay().toDate();
    	}
    	if (endDate != null) {
    		this.endDate = endDate.toDateTimeAtStartOfDay().toDate();
    	}
    	if (status != null) {
    		this.status = status.getValue();
    	}
    	this.office = staffOffice;
    	
    	/*
        if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
        
        if (StringUtils.isNotBlank(description)) {
            this.description = description.trim();
        } else {
            this.description = null;
        } */

    }

    
    public static Teller fromJson(final Office tellerOffice, final JsonCommand command) {
        final String name = command.stringValueOfParameterNamed("name");
        final String description = command.stringValueOfParameterNamed("description");
        final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
        final LocalDate endDate = command.localDateValueOfParameterNamed("endDate");
        final Integer tellerStatusInt = command.integerValueOfParameterNamed("status");
        final TellerStatus status = TellerStatus.fromInt(tellerStatusInt);

        return new Teller (tellerOffice, name, description, startDate, endDate, status);
    }
    
    public Map<String, Object> update(Office tellerOffice, final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();
        
        final String officeIdParamName = "officeId";
        if (command.isChangeInLongParameterNamed(officeIdParamName, this.officeId())) {
            final long newValue = command.longValueOfParameterNamed(officeIdParamName);
            actualChanges.put(officeIdParamName, newValue);
            this.office = tellerOffice;
        }

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }
        
        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        final String startDateParamName = "startDate";
        if (command.isChangeInLocalDateParameterNamed(startDateParamName, getStartLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(startDateParamName);
            actualChanges.put(startDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(startDateParamName);
            this.startDate = newValue.toDate();
        }

        final String endDateParamName = "endDate";
        if (command.isChangeInLocalDateParameterNamed(endDateParamName, getEndLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(endDateParamName);
            actualChanges.put(endDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(endDateParamName);
            this.endDate = newValue.toDate();
        }
        
        final String statusParamName = "status";
        if (command.isChangeInIntegerParameterNamed(statusParamName, getStatus())) {
            final Integer valueAsInput = command.integerValueOfParameterNamed(statusParamName);
            actualChanges.put(statusParamName, valueAsInput);
            final Integer newValue = command.integerValueOfParameterNamed(statusParamName);
            final TellerStatus status = TellerStatus.fromInt(newValue);
            if (status != TellerStatus.INVALID) {
            	this.status = status.getValue(); 
            }
        }

        return actualChanges;
    }



    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public GLAccount getDebitAccount() {
        return debitAccount;
    }

    public void setDebitAccount(GLAccount debitAccount) {
        this.debitAccount = debitAccount;
    }

    public GLAccount getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(GLAccount creditAccount) {
        this.creditAccount = creditAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }
    
    public LocalDate getStartLocalDate() {
        LocalDate startLocalDate = null;
        if (this.startDate != null) {
            startLocalDate = LocalDate.fromDateFields(this.startDate);
        }
        return startLocalDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    
    public LocalDate getEndLocalDate() {
        LocalDate endLocalDate = null;
        if (this.endDate != null) {
            endLocalDate = LocalDate.fromDateFields(this.endDate);
        }
        return endLocalDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Long officeId() {
        return this.office.getId();
    }

    public Set<Cashier> getCashiers() {
        return cashiers;
    }

    public void setCashiers(Set<Cashier> cashiers) {
        this.cashiers = cashiers;
    }

	public void initializeLazyCollections() {
		this.office.getId();
		this.cashiers.size();
	}
}
