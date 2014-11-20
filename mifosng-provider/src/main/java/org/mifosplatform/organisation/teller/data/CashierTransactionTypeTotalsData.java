/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.data;

import java.io.Serializable;
import java.math.BigDecimal;

public final class CashierTransactionTypeTotalsData implements Serializable {
	private final Integer cashierTxnType;
	private final BigDecimal cashTotal;

    private CashierTransactionTypeTotalsData (
    		final Integer cashierTxnType, 
    		final BigDecimal cashTotal
    		) {
    	this.cashierTxnType = cashierTxnType;
    	this.cashTotal = cashTotal;
    }

    public static CashierTransactionTypeTotalsData instance(
    		final Integer cashierTxnType, 
    		final BigDecimal cashTotal
    		) {
           return new CashierTransactionTypeTotalsData(
        	cashierTxnType, cashTotal);
    }

    public Integer getCashierTxnType() {
		return cashierTxnType;
	}
    
    public BigDecimal getCashTotal() {
		return cashTotal;
	}

}
