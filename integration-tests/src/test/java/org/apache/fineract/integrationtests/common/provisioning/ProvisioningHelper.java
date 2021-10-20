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

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;

public final class ProvisioningHelper {

    private static final SecureRandom rand = new SecureRandom();

    private ProvisioningHelper() {

    }

    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public static Map createProvisioingCriteriaJson(ArrayList<Integer> loanProducts, ArrayList categories, Account liability,
            Account expense) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("loanProducts", addLoanProducts(loanProducts));
        map.put("definitions", addProvisioningCategories(categories, liability, expense));
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy");
        String formattedString = simple
                .format(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant()));

        String criteriaName = "General Provisioning Criteria" + formattedString + rand.nextLong();
        map.put("criteriaName", criteriaName);
        map.put("locale", "en");
        return map;
    }

    public static String createProvisioningEntryJson() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("createjournalentries", Boolean.FALSE);
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy");
        map.put("date",
                simple.format(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant())));
        String provisioningEntryCreateJson = new Gson().toJson(map);
        return provisioningEntryCreateJson;
    }

    public static String createProvisioningEntryJsonWithJournalsEnabled() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("createjournalentries", Boolean.TRUE);
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy");
        map.put("date",
                simple.format(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant())));
        String provisioningEntryCreateJson = new Gson().toJson(map);
        return provisioningEntryCreateJson;
    }

    private static ArrayList<HashMap<String, Integer>> addLoanProducts(ArrayList<Integer> loanProducts) {
        ArrayList<HashMap<String, Integer>> list = new ArrayList<>();
        for (int i = 0; i < loanProducts.size(); i++) {
            HashMap<String, Integer> map = new HashMap<>();
            map.put("id", loanProducts.get(i));
            list.add(map);
        }
        return list;
    }

    public static ArrayList<HashMap<String, Object>> addProvisioningCategories(ArrayList categories, Account liability, Account expense) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        int minStart = 0;
        int maxStart = 30;

        for (int i = 0; i < categories.size(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            HashMap category = (HashMap) categories.get(i);
            map.put("categoryId", category.get("id"));
            map.put("categoryName", category.get("categoryName"));
            map.put("minAge", (i * 30) + 1);
            if (i == categories.size() - 1) {
                map.put("maxAge", 90000);
            } else {
                map.put("maxAge", (i + 1) * 30);
            }
            map.put("provisioningPercentage", Float.valueOf((float) ((i + 1) * 5.5)));
            map.put("liabilityAccount", liability.getAccountID());
            map.put("expenseAccount", expense.getAccountID());
            list.add(map);
        }
        return list;
    }
}
