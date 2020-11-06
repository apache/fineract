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
package org.apache.fineract.integrationtests.client;

import java.util.Optional;
import org.apache.fineract.client.models.GetClientsResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for /clients API.
 *
 * @author Michael Vorburger.ch
 */
public class ClientTest extends IntegrationTest {

    @Test
    @Order(1)
    void createOne() {
        assertThat(create()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    void retrieveAnyExisting() {
        assertThat(retrieveFirst()).isPresent();
    }

    // The following are not tests, but helpful utilities for other tests

    public Long getClientId() {
        return retrieveFirst().orElseGet(this::create);
    }

    Long create() {
        // NB officeId(1) always exists (Head Office)
        // TODO rm long cast, see https://issues.apache.org/jira/browse/FINERACT-1230
        // TODO activationDate() why String? https://issues.apache.org/jira/browse/FINERACT-1232
        // TODO why dateFormat and locale required even when no activationDate?!
        // https://issues.apache.org/jira/browse/FINERACT-1233
        return (long) ok(fineract().clients
                .create6(new PostClientsRequest().officeId(1).fullname("TestClient").dateFormat(dateFormat()).locale("en_US")))
                        .getClientId();
    }

    Optional<Long> retrieveFirst() {
        GetClientsResponse clients = ok(
                fineract().clients.retrieveAll21(null, null, null, null, null, null, null, null, 0, 1, null, null, false));
        if (clients.getTotalFilteredRecords() > 0) {
            // TODO rm long cast, see https://issues.apache.org/jira/browse/FINERACT-1230
            return Optional.of((long) clients.getPageItems().get(0).getId());
        }
        return Optional.empty();
    }
}
