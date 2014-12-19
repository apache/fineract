/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.domain;

import java.util.HashMap;

public class CashierTxnType {
	
	private Integer id;
	private String value;
	
	public static CashierTxnType ALLOCATE 			= new CashierTxnType (101, "Allocate Cash");
	public static CashierTxnType SETTLE 			= new CashierTxnType (102, "Settle Cash");
	public static CashierTxnType INWARD_CASH_TXN 	= new CashierTxnType (103, "Cash In");
	public static CashierTxnType OUTWARD_CASH_TXN 	= new CashierTxnType (104, "Cash Out");
	
	private CashierTxnType () {
	}
	
	private CashierTxnType (Integer id, String value) {
		this.id = id;
		this.value = value;
	}
		
	public Integer getId () {
		return id;
	}
	
	public String getValue () {
		return value;
	}
	
	public static CashierTxnType getCashierTxnType (Integer id) {
		CashierTxnType retVal = null;
		
		switch(id) {
		case 101: 
			retVal = ALLOCATE; break;
		case 102: 
			retVal = SETTLE; break;
		case 103: 
			retVal = INWARD_CASH_TXN; break;
		case 104: 
			retVal = OUTWARD_CASH_TXN; break;
		default:
			break;
		}
		return retVal;
	}
}
