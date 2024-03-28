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
package org.apache.fineract.test.api;

import org.apache.fineract.client.services.ClientApi;
import org.apache.fineract.client.services.CodeValuesApi;
import org.apache.fineract.client.services.CodesApi;
import org.apache.fineract.client.util.FineractClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfiguration {

    @Autowired
    private FineractClient fineractClient;

    @Bean
    public ClientApi clientApi() {
        return fineractClient.createService(ClientApi.class);
    }

    @Bean
    public CodesApi codesApi() {
        return fineractClient.createService(CodesApi.class);
    }

    @Bean
    public CodeValuesApi codeValuesApi() {
        return fineractClient.createService(CodeValuesApi.class);
    }
}
