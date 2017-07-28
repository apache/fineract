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
package org.apache.fineract.organisation.teller.data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.service.Page;

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

    private final Page<CashierTransactionData> cashierTransactions;

    private CashierTransactionsWithSummaryData(
    		final Page<CashierTransactionData> cashierTransactions, 
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
    		final Page<CashierTransactionData> cashierTransactions, 
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
	public Page<CashierTransactionData> getCashierTransactions() {
		return cashierTransactions;
	}
    
}
