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
import feign.Response;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdHocRequest;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.TemplateCreateRequest;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;

@RequiredArgsConstructor
public class TemplatesAndAdHocQueriesStepDef extends AbstractStepDef {

    private static final String AD_HOC_QUERY_ID = "templatesAndAdHocQueriesAdHocQueryId";
    private static final String TEMPLATE_ID = "templatesAndAdHocQueriesTemplateId";
    private static final String VALID_AD_HOC_SQL = "SELECT id FROM m_client WHERE 1 = 0";

    /**
     * Calls whose generated client method is not referenced by other step classes; sent through a minimal interface.
     */
    @Headers({ "Accept: application/json", "Content-Type: application/json" })
    public interface RawCallsApi {

        @RequestLine("PUT /v1/adhocquery/{adHocId}")
        CommandProcessingResult updateAdHocQuery(@Param("adHocId") Long adHocId, AdHocRequest request);

        @RequestLine("POST /v1/templates/{templateId}")
        @Headers({ "Accept: text/html", "Content-Type: application/json" })
        Response mergeTemplate(@Param("templateId") Long templateId, Map<String, Object> values);
    }

    private final FineractFeignClient fineractClient;

    @When("Admin creates a template with text {string}")
    public void createTemplate(final String text) {
        final TemplateCreateRequest request = new TemplateCreateRequest().name(Utils.randomStringGenerator("TEMPLATE_", 8)).type(0)
                .entity(0).text(text);
        final Long templateId = ok(() -> fineractClient.templates().createTemplate(request)).getResourceId();
        testContext().set(TEMPLATE_ID, templateId);
    }

    @Then("Merging the last created template with value {string} succeeds")
    public void mergeSucceeds(final String value) {
        final Long templateId = testContext().get(TEMPLATE_ID);
        final Response response = ok(() -> rawCalls().mergeTemplate(templateId, Map.of("name", value)));
        response.close();
    }

    @Then("Merging the last created template with value {string} is rejected")
    public void mergeIsRejected(final String value) {
        final Long templateId = testContext().get(TEMPLATE_ID);
        final CallFailedRuntimeException exception = fail(() -> rawCalls().mergeTemplate(templateId, Map.of("name", value)));
        assertThat(exception.getStatus()).isGreaterThanOrEqualTo(400);
    }

    // ---- ad hoc queries ------------------------------------------------------------------------------------------

    @When("Admin creates an ad hoc query")
    public void createAdHocQuery() {
        final AdHocRequest request = adHocRequest(VALID_AD_HOC_SQL);
        final Long id = ok(() -> fineractClient.adhocQuery().createAdHocQuery(request)).getResourceId();
        testContext().set(AD_HOC_QUERY_ID, id);
    }

    @Then("Updating the last created ad hoc query with SQL {string} is rejected")
    public void adHocUpdateIsRejected(final String sql) {
        final Long id = testContext().get(AD_HOC_QUERY_ID);
        final CallFailedRuntimeException exception = fail(() -> rawCalls().updateAdHocQuery(id, adHocRequest(sql)));
        assertClientError(exception);
    }

    @Then("Updating the last created ad hoc query with SQL {string} succeeds")
    public void adHocUpdateSucceeds(final String sql) {
        final Long id = testContext().get(AD_HOC_QUERY_ID);
        ok(() -> rawCalls().updateAdHocQuery(id, adHocRequest(sql)));
    }

    private static AdHocRequest adHocRequest(final String sql) {
        return new AdHocRequest().name(Utils.randomStringGenerator("ADHOC_", 8)).query(sql).tableName("m_client").tableFields("id")
                .isActive(false);
    }

    private RawCallsApi rawCalls() {
        return fineractClient.create(RawCallsApi.class);
    }

    private static void assertClientError(final CallFailedRuntimeException exception) {
        assertThat(exception.getStatus()).isBetween(400, 499);
    }
}
