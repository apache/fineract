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
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.integrationtests.ConfigProperties;

public final class FineractFeignClientHelper {

    private static final FineractFeignClient DEFAULT_FINERACT_FEIGN_CLIENT = createNewFineractFeignClient(ConfigProperties.Backend.USERNAME,
            ConfigProperties.Backend.PASSWORD);

    private FineractFeignClientHelper() {}

    public static FineractFeignClient getFineractFeignClient() {
        return DEFAULT_FINERACT_FEIGN_CLIENT;
    }

    public static FineractFeignClient createNewFineractFeignClient(String username, String password) {
        return createNewFineractFeignClient(username, password, Function.identity()::apply);
    }

    public static FineractFeignClient createNewFineractFeignClient(String username, String password, boolean debugEnabled) {
        return createNewFineractFeignClient(username, password, builder -> builder.debug(debugEnabled));
    }

    public static FineractFeignClient createNewFineractFeignClient(String username, String password,
            Consumer<FineractFeignClient.Builder> customizer) {
        String url = System.getProperty("fineract.it.url", buildURI());
        FineractFeignClient.Builder builder = FineractFeignClient.builder().baseUrl(url).credentials(username, password)
                .disableSslVerification(true);
        customizer.accept(builder);
        return builder.build();
    }

    private static String buildURI() {
        return ConfigProperties.Backend.PROTOCOL + "://" + ConfigProperties.Backend.HOST + ":" + ConfigProperties.Backend.PORT
                + "/fineract-provider/api";
    }
}
