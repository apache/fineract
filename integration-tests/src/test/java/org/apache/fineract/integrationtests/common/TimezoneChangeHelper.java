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
package org.apache.fineract.integrationtests.common;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.util.JSON;

@SuppressWarnings({ "unused", "rawtypes" })
@Slf4j
@RequiredArgsConstructor
public class TimezoneChangeHelper {

    private static final Gson GSON = new JSON().getGson();
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public static void updateTimeZone(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String tz) {
        final String BUSINESS_DATE_UPDATE_URL = "/fineract-provider/api/v1/timezone?" + Utils.TENANT_IDENTIFIER;
        log.info("---------------------------------UPDATE BUSINESS DATE---------------------------------------------");
        Utils.performServerPut(requestSpec, responseSpec, BUSINESS_DATE_UPDATE_URL, updateTimeZoneBody(tz));
    }

    private static String updateTimeZoneBody(String tz) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("timeZone", tz);
        log.info("map :  {}", map);
        return new Gson().toJson(map);
    }

}
