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
package org.apache.fineract.integrationtests.common.savings;

import com.google.gson.Gson;
import java.util.HashMap;
import lombok.Builder;
import lombok.Getter;
import org.apache.fineract.integrationtests.common.CommonConstants;

@Builder
@Getter
public class SavingsTransactionData {

    private String locale;
    private String dateFormat;
    private String transactionDate;
    private String transactionAmount;
    private Long paymentTypeId;
    private String withdrawnOnDate;
    private String note;
    private Boolean isBulk;
    private Boolean lienAllowed;
    private String reasonForBlock;

    public String getJson() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("locale", locale == null ? CommonConstants.LOCALE : locale);
        map.put("dateFormat", dateFormat == null ? CommonConstants.DATE_FORMAT : dateFormat);
        map.put("transactionDate", transactionDate);
        map.put("transactionAmount", transactionAmount);
        map.put("paymentTypeId", paymentTypeId);
        map.put("withdrawnOnDate", withdrawnOnDate);
        map.put("note", note);
        map.put("isBulk", isBulk);
        map.put("lienAllowed", lienAllowed);
        map.put("reasonForBlock", reasonForBlock);
        return new Gson().toJson(map);
    }

}
