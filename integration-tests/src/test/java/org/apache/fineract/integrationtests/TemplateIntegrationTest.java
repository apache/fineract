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
package org.apache.fineract.integrationtests;

import org.apache.fineract.client.models.TemplateCreateRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TemplateIntegrationTest extends FeignIntegrationTest {

    @Disabled
    @Test
    public void test() {

        final int entriesBeforeTest = ok(() -> fineractClient().templates().retrieveAllTemplates((Integer) null, (Integer) null)).size();

        final Long id = ok(
                () -> fineractClient().templates().createTemplate(new TemplateCreateRequest().name("foo").text("Hello {{template}}")))
                .getResourceId();

        final String name = ok(() -> fineractClient().templates().retrieveOneTemplate(id)).getName();

        Assertions.assertTrue(name.equals("foo"));

        ok(() -> fineractClient().templates().deleteTemplate(id));

        final int entriesAfterTest = ok(() -> fineractClient().templates().retrieveAllTemplates((Integer) null, (Integer) null)).size();

        Assertions.assertEquals(entriesBeforeTest, entriesAfterTest);
    }
}
