/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.domain;

public class MifosEntityAccessType {
	
	private String str;
	
	public static final MifosEntityAccessType OFFICE_ACCESS_TO_LOAN_PRODUCTS = new MifosEntityAccessType("Office Access to Loan Products");
	public static final MifosEntityAccessType OFFICE_ACCESS_TO_SAVINGS_PRODUCTS = new MifosEntityAccessType("Office Access to Savings Products");
	public static final MifosEntityAccessType OFFICE_ACCESS_TO_CHARGES = new MifosEntityAccessType("Office Access to Fees/Charges");
    
    private MifosEntityAccessType (String str) {
    	this.str = str;
    }
    
    public String toStr () {
    	return this.str;
    }
    
    public static MifosEntityAccessType get (String type) {
    	
    	MifosEntityAccessType retType = null;
    	
    	if (type.equals(OFFICE_ACCESS_TO_LOAN_PRODUCTS.str)) {
    		retType =  OFFICE_ACCESS_TO_LOAN_PRODUCTS;
    	} else if (type.equals(OFFICE_ACCESS_TO_SAVINGS_PRODUCTS.str)) {
    		retType = OFFICE_ACCESS_TO_SAVINGS_PRODUCTS;
    	} else if (type.equals(OFFICE_ACCESS_TO_CHARGES.str)) { 
    			retType = OFFICE_ACCESS_TO_CHARGES;
    	}
    	
    	return retType;
    }
}
