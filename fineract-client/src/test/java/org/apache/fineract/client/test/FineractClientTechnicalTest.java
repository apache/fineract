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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.client.util.FineractClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests a few technical aspect of the Fineract SDK REST Client.
 *
 * @author Michael Vorburger.ch
 */
public class FineractClientTechnicalTest {

    @Test
    @Disabled // TODO remove Ignore once https://issues.apache.org/jira/browse/FINERACT-1221 is fixed
    void testInvalidOperations() {
        FineractClient.Builder builder = FineractClient.builder().baseURL("http://test/").tenant("default").basicAuth("mifos", "password");
        builder.getRetrofitBuilder().validateEagerly(true); // see FINERACT-1221
        builder.build();
    }

    @Test
    void testFineractClientBuilder() {
        assertThrows(IllegalStateException.class, () -> {
            FineractClient.builder().build();
        });
        assertThrows(IllegalStateException.class, () -> {
            FineractClient.builder().baseURL("https://server/").build();
        });
        assertThrows(IllegalStateException.class, () -> {
            FineractClient.builder().baseURL("https://server/").tenant("default").build();
        });
    }
}
