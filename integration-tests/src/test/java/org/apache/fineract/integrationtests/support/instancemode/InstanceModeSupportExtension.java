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
package org.apache.fineract.integrationtests.support.instancemode;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

public class InstanceModeSupportExtension
        implements BeforeAllCallback, BeforeTestExecutionCallback, AfterAllCallback, AfterTestExecutionCallback {

    private static final Namespace INSTANCE_MODE_NAMESPACE = Namespace.create(InstanceModeSupportExtension.class);
    private static final String AUTH_KEY = "AUTH_KEY";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Utils.initializeRESTAssured();
        resetInstanceMode(context);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        Utils.initializeRESTAssured();
        resetInstanceMode(context);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        context.getTestMethod().ifPresent(m -> {
            ConfigureInstanceMode annotation = m.getAnnotation(ConfigureInstanceMode.class);
            if (annotation != null) {
                Utils.initializeRESTAssured();
                boolean readEnabled = annotation.readEnabled();
                boolean writeEnabled = annotation.writeEnabled();
                boolean batchWorkerEnabled = annotation.batchWorkerEnabled();
                boolean batchManagerEnabled = annotation.batchManagerEnabled();
                changeInstanceMode(context, readEnabled, writeEnabled, batchWorkerEnabled, batchManagerEnabled);
            }
        });
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        context.getTestMethod().ifPresent(m -> {
            ConfigureInstanceMode annotation = m.getAnnotation(ConfigureInstanceMode.class);
            if (annotation != null) {
                Utils.initializeRESTAssured();
                resetInstanceMode(context);
            }
        });
    }

    private void resetInstanceMode(ExtensionContext context) {
        changeInstanceMode(context, true, true, true, true);
    }

    private void changeInstanceMode(ExtensionContext extensionContext, boolean readEnabled, boolean writeEnabled,
            boolean batchWorkerEnabled, boolean batchManagerEnabled) {
        Store store = extensionContext.getStore(INSTANCE_MODE_NAMESPACE);
        String authKey = store.getOrComputeIfAbsent(AUTH_KEY, k -> Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(),
                String.class);
        RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        requestSpec.header("Authorization", "Basic " + authKey);

        InstanceModeHelper.changeMode(requestSpec, responseSpec, readEnabled, writeEnabled, batchWorkerEnabled, batchManagerEnabled);
    }
}
