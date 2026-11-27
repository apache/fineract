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
package org.apache.fineract.test.stepdef.common;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.ExternalServicesPropertiesData;
import org.apache.fineract.client.models.HookCreateRequest;
import org.apache.fineract.client.models.HookEventData;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;

@RequiredArgsConstructor
public class ExternalServicesAndHooksStepDef extends AbstractStepDef {

    private final FineractFeignClient fineractClient;

    @When("Admin sets the S3 external service properties s3_access_key to {string} and s3_secret_key to {string}")
    public void setS3Properties(final String accessKey, final String secretKey) {
        // The generated request model only covers the SMTP properties, so the properties are sent as a plain map.
        final ExternalServiceUpdateApi api = fineractClient.create(ExternalServiceUpdateApi.class);
        ok(() -> api.updateProperties("S3", Map.of("s3_access_key", accessKey, "s3_secret_key", secretKey)));
    }

    @Then("The S3 external service properties are returned without the values {string} and {string}")
    public void s3PropertiesAreMasked(final String accessKey, final String secretKey) {
        final List<ExternalServicesPropertiesData> properties = ok(
                () -> fineractClient.externalServices().retrieveExternalServicesConfiguration("S3"));
        assertThat(properties).extracting(ExternalServicesPropertiesData::getName).contains("s3_access_key", "s3_secret_key");
        assertMasked(properties, "s3_access_key", accessKey);
        assertMasked(properties, "s3_secret_key", secretKey);
    }

    /**
     * The seeded S3 key properties have no value, so clearing them restores the initial state.
     */
    @After("@ExternalServicePropertiesRestore")
    public void restoreS3Properties() {
        final ExternalServiceUpdateApi api = fineractClient.create(ExternalServiceUpdateApi.class);
        ok(() -> api.updateProperties("S3", Map.of("s3_access_key", "", "s3_secret_key", "")));
    }

    private static void assertMasked(final List<ExternalServicesPropertiesData> properties, final String name, final String clearValue) {
        assertThat(properties).filteredOn(p -> name.equals(p.getName())).extracting(ExternalServicesPropertiesData::getValue)
                .singleElement().asString().isNotEqualTo(clearValue).contains("*");
    }

    @Headers({ "Accept: application/json", "Content-Type: application/json" })
    public interface ExternalServiceUpdateApi {

        @RequestLine("PUT /v1/externalservice/{serviceName}")
        CommandProcessingResult updateProperties(@Param("serviceName") String serviceName, Map<String, String> properties);
    }

    // ---- hooks ---------------------------------------------------------------------------------------------------

    @Then("Creating a hook with payload URL {string} is rejected with a validation error")
    public void hookCreationIsRejected(final String payloadUrl) {
        final HookCreateRequest request = new HookCreateRequest() //
                .name("Web") //
                .displayName(Utils.randomStringGenerator("Hook_DisplayName_", 5)) //
                .isActive(true) //
                .config(Map.of("Content Type", "json", "Payload URL", payloadUrl)) //
                .events(List.of(new HookEventData().actionName("CREATE").entityName("OFFICE")));
        final CallFailedRuntimeException exception = fail(() -> fineractClient.hooks().createHook(request));
        assertThat(exception.getStatus()).isEqualTo(400);
        assertThat(exception.getResponseBody()).contains("url.invalid");
    }
}
