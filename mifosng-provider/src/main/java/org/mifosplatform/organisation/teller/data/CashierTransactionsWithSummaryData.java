/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;

public final class CashierTransactionsWithSummaryData implements Serializable {
	
	private final BigDecimal sumCashAllocation;
	private final BigDecimal sumInwardCash;
	private final BigDecimal sumOutwardCash;
	private final BigDecimal sumCashSettlement;
	private final BigDecimal netCash;
	private final String officeName;
	private final long tellerId;
	private final String tellerName;
	private final long cashierId;
	private final String cashierName;

    private final Collection<CashierTransactionData> cashierTransactions;

    private CashierTransactionsWithSummaryData(
    		final Collection<CashierTransactionData> cashierTransactions, 
    		final BigDecimal sumCashAllocation,
    		final BigDecimal sumInwardCash,
    		final BigDecimal sumOutwardCash,
    		final BigDecimal sumCashSettlement,
    		final BigDecimal netCash,
    		final String officeName,
    		final Long tellerId,
    		final String tellerName,
    		final Long cashierId,
    		final String cashierName
    		) {
    	this.cashierTransactions = cashierTransactions;
    	this.sumCashAllocation = sumCashAllocation;
    	this.sumInwardCash = sumInwardCash;
    	this.sumOutwardCash = sumOutwardCash;
    	this.sumCashSettlement = sumCashSettlement;
    	this.netCash = netCash;
    	this.officeName = officeName;
    	this.tellerId = tellerId;
    	this.tellerName = tellerName;
    	this.cashierId = cashierId;
    	this.cashierName = cashierName;
    }

    public static CashierTransactionsWithSummaryData instance(
    		final Collection<CashierTransactionData> cashierTransactions, 
    		final BigDecimal sumCashAllocation,
    		final BigDecimal sumInwardCash,
    		final BigDecimal sumOutwardCash,
    		final BigDecimal sumCashSettlement,
    		final String officeName,
    		final Long tellerId,
    		final String tellerName,
    		final Long cashierId,
    		final String cashierName
    		) {
    	
    	final BigDecimal netCash = 
    			sumCashAllocation.add(sumInwardCash).
    				subtract(sumOutwardCash).
    				subtract(sumCashSettlement); 
        return new CashierTransactionsWithSummaryData(
        		cashierTransactions, 
        		sumCashAllocation,
        		sumInwardCash,
        		sumOutwardCash,
        		sumCashSettlement, netCash,
        		officeName, tellerId, tellerName, cashierId, cashierName);
    }

	public BigDecimal getSumCashAllocation() {
		return sumCashAllocation;
	}

	public BigDecimal getSumInwardCash() {
		return sumInwardCash;
	}

	public BigDecimal getSumOutwardCash() {
		return sumOutwardCash;
	}

	public BigDecimal getSumCashSettlement() {
		return sumCashSettlement;
	}

	public BigDecimal getNetCash() {
		return netCash;
	}
	
	public String getOfficeName() {
		return officeName;
	}
	
	public Long getTellerId() {
		return tellerId;
	}
	
	public String getTellerName() {
		return tellerName;
	}

	public Long getCashierId() {
		return cashierId;
	}
	
	public String getCashierName() {
		return cashierName;
	}
	public Collection<CashierTransactionData> getCashierTransactions() {
		return cashierTransactions;
	}
    
}
