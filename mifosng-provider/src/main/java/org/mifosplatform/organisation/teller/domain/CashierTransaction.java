/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.domain;


import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.office.domain.Office;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "m_cashier_transactions")
public class CashierTransaction extends AbstractPersistable<Long> {

	@Transient
    private Office office;
	
	@Transient
    private Teller teller;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", nullable = false)
    private Cashier cashier;
    
    @Column(name = "txn_type", nullable = false)
    private Integer txnType;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "txn_date", nullable = false)
    private Date txnDate;

    @Column(name = "txn_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal txnAmount;
    
    @Column(name = "txn_note", nullable = true)
    private String txnNote;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;
    
    @Column(name = "currency_code", nullable = true)
    private String currencyCode;

    /**
     * Creates a new cashier.
     */
    public CashierTransaction() {
        super();
    }
    
    public static CashierTransaction fromJson(
    		final Cashier cashier,
    		final JsonCommand command) {
        final Integer txnType = command.integerValueOfParameterNamed("txnType");
        final BigDecimal txnAmount = command.bigDecimalValueOfParameterNamed("txnAmount");
        final LocalDate txnDate = command.localDateValueOfParameterNamed("txnDate");
        final String entityType = command.stringValueOfParameterNamed("entityType");
        final String txnNote = command.stringValueOfParameterNamed("txnNote");
        final Long entityId = command.longValueOfParameterNamed("entityId");
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");

        // TODO: get client/loan/savings details
        return new CashierTransaction (cashier, txnType, txnAmount, txnDate, 
        		entityType, entityId, txnNote, currencyCode);
        
    }
    
    public CashierTransaction (Cashier cashier, Integer txnType, BigDecimal txnAmount, 
    		LocalDate txnDate, String entityType, Long entityId, String txnNote, String currencyCode) {
    	this.cashier = cashier;
    	this.txnType = txnType;
    	if (txnDate != null) {
    		this.txnDate = txnDate.toDate();
    	}
    	this.txnAmount = txnAmount;
    	this.entityType = entityType;
    	this.entityId = entityId;
    	this.txnNote = txnNote;
    	this.createdDate = new Date(); 
    	this.currencyCode = currencyCode;
    }
    
    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();        
      
        final String txnTypeParamName = "txnType";
        if (command.isChangeInIntegerParameterNamed(txnTypeParamName, this.txnType)) {
            final Integer newValue = command.integerValueOfParameterNamed(txnTypeParamName);
            actualChanges.put(txnTypeParamName, newValue);
            this.txnType = newValue;
        }

        final String txnDateParamName = "txnDate";
        if (command.isChangeInLocalDateParameterNamed(txnDateParamName, getTxnLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(txnDateParamName);
            actualChanges.put(txnDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(txnDateParamName);
            this.txnDate = newValue.toDate();
        }
        
        final String txnAmountParamName = "txnAmount";
        if (command.isChangeInBigDecimalParameterNamed(txnAmountParamName, this.txnAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(txnAmountParamName);
            actualChanges.put(txnAmountParamName, newValue);
            this.txnAmount = newValue;
        }
        
        final String txnNoteParamName = "txnNote";
        if (command.isChangeInStringParameterNamed(txnNoteParamName, this.txnNote)) {
            final String newValue = command.stringValueOfParameterNamed(txnNoteParamName);
            actualChanges.put(txnNoteParamName, newValue);
            this.txnNote = newValue;
        }
        
        final String currencyCodeParamName = "currencyCode";
        if (command.isChangeInStringParameterNamed(currencyCodeParamName, this.currencyCode)) {
            final String newValue = command.stringValueOfParameterNamed(currencyCodeParamName);
            actualChanges.put(currencyCodeParamName, newValue);
            this.currencyCode = newValue;
        }
                
        return actualChanges;
    }


    /**
     * Returns the office of this cashier transaction.
     *
     * @return the office of this cashier transaction
     * @see org.mifosplatform.organisation.office.domain.Office
     */
    public Office getOffice() {
        return office;
    }

    /**
     * Sets the office of this cashier transaction.
     *
     * @param office the office of this cashier transaction
     * @see org.mifosplatform.organisation.office.domain.Office
     */
    public void setOffice(Office office) {
        this.office = office;
    }

    /**
     * Returns the teller of this cashier transaction.
     *
     * @return the teller of this cashier transaction
     * @see org.mifosplatform.organisation.teller.domain.Teller
     */
    public Teller getTeller() {
        return teller;
    }

    /**
     * Sets the teller of this cashier transaction.
     *
     * @param teller the teller of this cashier transaction
     * @see org.mifosplatform.organisation.teller.domain.Teller
     */
    public void setTeller(Teller teller) {
        this.teller = teller;
    }

    /**
     * Returns the transaction type of this cashier transaction.
     * .
     *
     * @return the transaction type of this cashier transaction or {@code null} if not present.
     */
    public Integer getTxnType() {
        return txnType;
    }

    /**
     * Sets the transaction type of this cashier transaction.
     *
     * @param description the transaction type of this cashier transaction
     */
    public void setTxnType(Integer txnType) {
        this.txnType = txnType;
    }

    /**
     * Returns the transaction date of this cashier transaction.
     *
     * @return the transaction date of this cashier transaction
     */
    public Date getTxnDate() {
        return txnDate;
    }
    
    public LocalDate getTxnLocalDate() {
        LocalDate txnLocalDate = null;
        if (this.txnDate != null) {
            txnLocalDate = LocalDate.fromDateFields(this.txnDate);
        }
        return txnLocalDate;
    }

    /**
     * Sets the transaction date of this cashier transaction.
     *
     * @param transaction date of this cashier transaction
     */
    public void setTxnDate(Date txnDate) {
        this.txnDate = txnDate;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getTxnNote() {
        return txnNote;
    }
    
    public BigDecimal getTxnAmount() {
        return txnAmount;
    }

    public void setTxnNote (String txnNote) {
        this.txnNote = txnNote;
    }
    
    public String getCurrencyCode() {
        return this.currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

}
