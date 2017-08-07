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
package org.apache.fineract.infrastructure.bulkimport.data;

import java.util.HashMap;
import java.util.Map;

public enum GlobalEntityType {
	

        INVALID(0, "invalid"),
        CLIENTS(1, "clients"),
        GROUPS(2, "groups"),
        CENTERS(3, "centers"),
        OFFICES(4, "offices"),
        STAFF(5, "staff"),
        USERS(6, "users"),
        SMS(7, "sms"),
        DOCUMENTS(8, "documents"),
        TEMPLATES(9, "templates"),
        NOTES(10, "templates"),
        CALENDAR(11, "calendar"),
        MEETINGS(12, "meetings"),
        HOLIDAYS(13, "holidays"),
        LOANS(14, "loans"),
        LOAN_PRODUCTS(15, "loanproducts"),
        LOAN_CHARGES(16, "loancharges"),
        LOAN_TRANSACTIONS(17, "loantransactions"),
        GUARANTORS(18, "guarantors"),
        COLLATERALS(19, "collaterals"),
        FUNDS(20, "funds"),
        CURRENCY(21, "currencies"),
        SAVINGS_ACCOUNT(22, "savingsaccount"),
        SAVINGS_CHARGES(23, "savingscharges"),
        SAVINGS_TRANSACTIONS(24, "savingstransactions"),
        SAVINGS_PRODUCTS(25, "savingsproducts"),
        GL_JOURNAL_ENTRIES(26, "gljournalentries"),
        CODE_VALUE(27, "codevalue"),
        CODE(28, "code");

	    private final Integer value;
	    private final String code;

	    private static final Map<Integer, GlobalEntityType> intToEnumMap = new HashMap<>();
	    private static final Map<String, GlobalEntityType> stringToEnumMap = new HashMap<>();
	    private static int minValue;
	    private static int maxValue;
	    
	    static {
	        int i = 0;
	        for (final GlobalEntityType entityType : GlobalEntityType.values()) {
	            if (i == 0) {
	                minValue = entityType.value;
	            }
	            intToEnumMap.put(entityType.value, entityType);
	            stringToEnumMap.put(entityType.code, entityType);
	            if (minValue >= entityType.value) {
	                minValue = entityType.value;
	            }
	            if (maxValue < entityType.value) {
	                maxValue = entityType.value;
	            }
	            i = i + 1;
	        }
	    }

	    private GlobalEntityType(final Integer value, final String code) {
	        this.value = value;
	        this.code = code;
	    }

	    public Integer getValue() {
	        return this.value;
	    }

	    public String getCode() {
	        return this.code;
	    }
	    
	    public static GlobalEntityType fromInt(final int i) {
	        final GlobalEntityType entityType = intToEnumMap.get(Integer.valueOf(i));
	        return entityType;
	    }
	    
	    public static GlobalEntityType fromCode(final String key) {
	        final GlobalEntityType entityType = stringToEnumMap.get(key);
	        return entityType;
	    }
	    
	    @Override
	    public String toString() {
	        return name().toString();
	    }

}
