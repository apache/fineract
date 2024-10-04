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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.fineract.client.util.FineractClient;
import org.junit.jupiter.api.Test;

/**
 * Tests a few technical aspect of the Fineract SDK REST Client.
 *
 * @author Michael Vorburger.ch
 */
@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class FineractClientTechnicalTest {

    @Test
    void testInvalidOperations() {
        FineractClient.Builder builder = FineractClient.builder().baseURL("http://test/").tenant("default").basicAuth("mifos", "password");
        builder.getRetrofitBuilder().validateEagerly(true);
        builder.build();
    }

    @Test
    void testFineractClientBuilder() {
        assertThrows(IllegalStateException.class, () -> {
            FineractClient.builder().build();
        });
    }
}
