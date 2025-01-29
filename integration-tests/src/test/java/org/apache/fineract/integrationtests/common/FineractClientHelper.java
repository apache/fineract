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

import java.util.function.Consumer;
import java.util.function.Function;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.fineract.client.util.FineractClient;
import org.apache.fineract.integrationtests.ConfigProperties;

public final class FineractClientHelper {

    private static final FineractClient DEFAULT_FINERACT_CLIENT = createNewFineractClient(ConfigProperties.Backend.USERNAME,
            ConfigProperties.Backend.PASSWORD);

    private FineractClientHelper() {}

    public static FineractClient getFineractClient() {
        return DEFAULT_FINERACT_CLIENT;
    }

    public static FineractClient createNewFineractClient(String username, String password) {
        return createNewFineractClient(username, password, Function.identity()::apply);
    }

    public static FineractClient createNewFineractClient(String username, String password, Consumer<FineractClient.Builder> customizer) {
        String url = System.getProperty("fineract.it.url", buildURI());
        // insecure(true) should *ONLY* ever be used for https://localhost:8443, NOT in real clients!!
        FineractClient.Builder builder = FineractClient.builder().insecure(true).baseURL(url).tenant(ConfigProperties.Backend.TENANT)
                .basicAuth(username, password).logging(HttpLoggingInterceptor.Level.NONE);
        customizer.accept(builder);
        return builder.build();
    }

    private static String buildURI() {
        return ConfigProperties.Backend.PROTOCOL + "://" + ConfigProperties.Backend.HOST + ":" + ConfigProperties.Backend.PORT
                + "/fineract-provider/api/";
    }
}
