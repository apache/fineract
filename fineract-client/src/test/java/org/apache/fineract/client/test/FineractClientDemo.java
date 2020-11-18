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
package org.apache.fineract.client.test;

import java.util.List;
import org.apache.fineract.client.models.RetrieveOneResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.client.util.FineractClient;

/**
 * Demo code which is included in the fineract-doc/src/docs/en/05_client.adoc.
 *
 * This is not a real running integration test - those are in
 * integration-tests/src/test/java/org/apache/fineract/integrationtests/client.
 *
 * @author Michael Vorburger.ch
 */
public class FineractClientDemo {

    void demoClient() {
        // tag::documentation[]
        FineractClient fineract = FineractClient.builder().baseURL("https://demo.fineract.dev/fineract-provider/api/v1/").tenant("default")
                .basicAuth("mifos", "password").build();
        List<RetrieveOneResponse> staff = Calls.ok(fineract.staff.retrieveAll16(1L, true, false, "ACTIVE"));
        String name = staff.get(0).getDisplayName();
        // end::documentation[]
    }

}
