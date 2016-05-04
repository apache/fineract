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

public class FineractEntityAccessType {
	
	private String str;
	
	public static final FineractEntityAccessType OFFICE_ACCESS_TO_LOAN_PRODUCTS = new FineractEntityAccessType("office_access_to_loan_products");
	public static final FineractEntityAccessType OFFICE_ACCESS_TO_SAVINGS_PRODUCTS = new FineractEntityAccessType("office_access_to_savings_products");
	public static final FineractEntityAccessType OFFICE_ACCESS_TO_CHARGES = new FineractEntityAccessType("office_access_to_fees/charges");
     
    private FineractEntityAccessType (String str) {
    	this.str = str;
    }
    
    public String toStr () {
    	return this.str;
    }
    
    public static FineractEntityAccessType get (String type) {
    	
    	FineractEntityAccessType retType = null;
    	
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
