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
package org.apache.fineract.integrationtests.common.provisioning;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;

import com.google.gson.Gson;

public class ProvisioningHelper {

    public final static Map createProvisioingCriteriaJson(ArrayList<Integer> loanProducts, ArrayList categories, Account liability,
            Account expense) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("loanProducts", addLoanProducts(loanProducts));
        map.put("definitions", addProvisioningCategories(categories, liability, expense));
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy");
        String formattedString = simple.format(Utils.getLocalDateOfTenant().toDate());
        Random rand = new Random() ;
        String criteriaName = "General Provisioning Criteria" + formattedString+rand.nextLong();
        map.put("criteriaName", criteriaName);
        map.put("locale", "en");
       return map ;
    }

    public final static String createProvisioningEntryJson() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("createjournalentries", Boolean.FALSE);
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy");
        map.put("date", simple.format(Utils.getLocalDateOfTenant().toDate()));
        String provisioningEntryCreateJson = new Gson().toJson(map);
        return provisioningEntryCreateJson;
    }
    
    public final static String createProvisioningEntryJsonWithJournalsEnabled() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("createjournalentries", Boolean.TRUE);
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy");
        map.put("date", simple.format(Utils.getLocalDateOfTenant().toDate()));
        String provisioningEntryCreateJson = new Gson().toJson(map);
        return provisioningEntryCreateJson;
    }

    private static ArrayList addLoanProducts(ArrayList<Integer> loanProducts) {
        ArrayList list = new ArrayList<>();
        for (int i = 0; i < loanProducts.size(); i++) {
            HashMap map = new HashMap();
            map.put("id", loanProducts.get(i));
            list.add(map);
        }
        return list;
    }

    public static ArrayList addProvisioningCategories(ArrayList categories, Account liability, Account expense) {
        ArrayList list = new ArrayList();
        int minStart = 0;
        int maxStart = 30;

        for (int i = 0; i < categories.size(); i++) {
            HashMap map = new HashMap();
            HashMap category = (HashMap) categories.get(i);
            map.put("categoryId", category.get("id"));
            map.put("categoryName", category.get("categoryName"));
            map.put("minAge", (i * 30) + 1);
            if (i == categories.size() - 1) {
                map.put("maxAge", 90000);
            } else {
                map.put("maxAge", (i+1) * 30);
            }
            map.put("provisioningPercentage", new Float((i + 1) * 5.5));
            map.put("liabilityAccount", liability.getAccountID());
            map.put("expenseAccount", expense.getAccountID());
            list.add(map);
        }
        return list;
    }
}
