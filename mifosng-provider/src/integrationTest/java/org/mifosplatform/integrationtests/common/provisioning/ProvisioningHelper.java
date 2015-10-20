/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.provisioning;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.mifosplatform.integrationtests.common.accounting.Account;

import com.google.gson.Gson;

public class ProvisioningHelper {

    public final static String createProvisioingCriteriaJson(ArrayList<Integer> loanProducts, ArrayList categories, Account liability,
            Account expense) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("loanProducts", addLoanProducts(loanProducts));
        map.put("provisioningcriteria", addProvisioningCategories(categories, liability, expense));
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy");
        String formattedString = simple.format(new Date());
        String criteriaName = "General Provisioning Criteria" + formattedString;
        map.put("criteriaName", criteriaName);
        map.put("locale", "en");
        String provisioningCriteriaCreateJson = new Gson().toJson(map);
        return provisioningCriteriaCreateJson;
    }

    public final static String createProvisioningEntryJson() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("createjournalentries", Boolean.TRUE);
        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy");
        map.put("date", simple.format(new Date()));
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
                map.put("maxAge", i * 60);
            }
            map.put("provisioningPercentage", (i + 1) * 5.5);
            map.put("liabilityAccount", liability.getAccountID());
            map.put("expenseAccount", expense.getAccountID());
            list.add(map);
        }
        return list;
    }
}
