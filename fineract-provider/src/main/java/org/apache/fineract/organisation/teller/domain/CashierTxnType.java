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

import java.util.HashMap;

public class CashierTxnType {
	
	private Integer id;
	private String value;
	
	public static final CashierTxnType ALLOCATE 			= new CashierTxnType (101, "Allocate Cash");
	public static final CashierTxnType SETTLE 			= new CashierTxnType (102, "Settle Cash");
	public static final CashierTxnType INWARD_CASH_TXN 	= new CashierTxnType (103, "Cash In");
	public static final CashierTxnType OUTWARD_CASH_TXN 	= new CashierTxnType (104, "Cash Out");
	
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
