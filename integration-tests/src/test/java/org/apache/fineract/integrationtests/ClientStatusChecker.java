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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientStatusChecker {

    private static final Logger LOG = LoggerFactory.getLogger(ClientStatusChecker.class);

    private ClientStatusChecker() {

    }

    public static void verifyClientIsActive(final HashMap<String, Object> clientStatusHashMap) {
        assertEquals(300, (int) clientStatusHashMap.get("id"));
    }

    public static void verifyClientClosed(final HashMap<String, Object> clientStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING CLIENT IS CLOSED ------------------------------------");
        assertEquals(600, (int) clientStatusHashMap.get("id"));
        LOG.info("Client Status: {} \n", clientStatusHashMap);
    }

    public static void verifyClientPending(final HashMap<String, Object> clientStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING CLIENT IS PENDING ------------------------------------");
        assertEquals(100, (int) clientStatusHashMap.get("id"));
        LOG.info("Client Status: {} \n", clientStatusHashMap);
    }

    public static void verifyClientRejected(final HashMap<String, Object> clientStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING CLIENT IS REJECTED ------------------------------------");
        assertEquals(700, (int) clientStatusHashMap.get("id"));
        LOG.info("Client Status: {} \n", clientStatusHashMap);
    }

    public static void verifyClientActiavted(final HashMap<String, Object> clientStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING CLIENT IS ACTIVATED ------------------------------------");
        assertEquals(300, (int) clientStatusHashMap.get("id"));
        LOG.info("Client Status: {} \n", clientStatusHashMap);
    }

    public static void verifyClientWithdrawn(final HashMap<String, Object> clientStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING CLIENT IS WITHDRAWN ------------------------------------");
        assertEquals(800, (int) clientStatusHashMap.get("id"));
        LOG.info("Client Status: {} \n", clientStatusHashMap);
    }

}
