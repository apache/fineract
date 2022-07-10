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
package org.apache.fineract.integrationtests.common.commands;

import com.google.gson.Gson;
import com.linecorp.armeria.internal.shaded.guava.reflect.TypeToken;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.lang.reflect.Type;
import java.util.ArrayList;
import org.apache.fineract.client.models.GetMakerCheckerResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.common.Utils;

public class MakercheckersHelper {

    private static final Gson GSON = new JSON().getGson();

    private static final String MAKERCHECKER_URL = "/fineract-provider/api/v1/makercheckers?" + Utils.TENANT_IDENTIFIER;

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public MakercheckersHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public ArrayList<GetMakerCheckerResponse> getMakerCheckerList() {
        final String response = Utils.performServerGet(this.requestSpec, this.responseSpec, MAKERCHECKER_URL);
        Type makerCheckerList = new TypeToken<ArrayList<GetMakerCheckerResponse>>() {}.getType();
        return GSON.fromJson(response, makerCheckerList);
    }

}
