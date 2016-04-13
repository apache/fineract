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
package org.apache.fineract.infrastructure.entityaccess.domain;

public class FineractEntityType {
	private String type;
	private String description;
	private String table_name;
	
	public static FineractEntityType OFFICE = new FineractEntityType ("office", "Offices", "m_office"); 
	public static FineractEntityType LOAN_PRODUCT = new FineractEntityType ("loan_product", "Loan Products", "m_product_loan");
	public static FineractEntityType SAVINGS_PRODUCT = new FineractEntityType ("savings_product", "Savings Products", "m_savings_product");
	public static FineractEntityType CHARGE = new FineractEntityType ("charge", "Fees/Charges", "m_charge");
	public static FineractEntityType SHARE_PRODUCT = new FineractEntityType("shares_product", "Shares Products", "m_share_product") ;
	
	private FineractEntityType (String type, String description, String table_name) {
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
	
	public static FineractEntityType get (String type) {

    	FineractEntityType retType = null;
    	
    	if (type.equals(OFFICE.type)) {
    		retType =  OFFICE;
    	} else if (type.equals(LOAN_PRODUCT.type)) { 
    			retType = LOAN_PRODUCT;
    	} else if (type.equals(SAVINGS_PRODUCT)) { 
    			retType = SAVINGS_PRODUCT;
    	} else if (type.equals(CHARGE)) {
    		retType = CHARGE;
    	}else if(type.equals(SHARE_PRODUCT)) {
    		retType = SHARE_PRODUCT ;
    	}
    	return retType;
	}

}
