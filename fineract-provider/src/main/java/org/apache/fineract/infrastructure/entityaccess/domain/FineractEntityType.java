/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.domain;

public class MifosEntityType {
	private String type;
	private String description;
	private String table_name;
	
	public static MifosEntityType OFFICE = new MifosEntityType ("office", "Offices", "m_office"); 
	public static MifosEntityType LOAN_PRODUCT = new MifosEntityType ("loan_product", "Loan Products", "m_product_loan");
	public static MifosEntityType SAVINGS_PRODUCT = new MifosEntityType ("savings_product", "Savings Products", "m_savings_product");
	public static MifosEntityType CHARGE = new MifosEntityType ("charge", "Fees/Charges", "m_charge");
		
	private MifosEntityType (String type, String description, String table_name) {
		this.type = type;
		this.description = description;
		this.table_name = table_name;
	}
	
	public String getType () {
		return this.type;
	}
	
	public String getDescription () {
		return this.description;
	}
	
	public String getTable () {
		return this.table_name;
	}
	
	public static MifosEntityType get (String type) {

    	MifosEntityType retType = null;
    	
    	if (type.equals(OFFICE.type)) {
    		retType =  OFFICE;
    	} else if (type.equals(LOAN_PRODUCT.type)) { 
    			retType = LOAN_PRODUCT;
    	} else if (type.equals(SAVINGS_PRODUCT)) { 
    			retType = SAVINGS_PRODUCT;
    	} else if (type.equals(CHARGE)) 
    			retType = CHARGE;
    	
    	return retType;
	}

}
