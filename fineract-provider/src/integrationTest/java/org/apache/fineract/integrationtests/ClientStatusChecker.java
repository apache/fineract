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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientStatusChecker {
    private final static Logger LOG = LoggerFactory.getLogger(ClientStatusChecker.class);
    public static void verifyClientIsActive(final HashMap<String, Object> clientStatusHashMap) {
        assertEquals((int) clientStatusHashMap.get("id"), 300);
    }

    public static void verifyClientClosed(final HashMap<String, Object> clientStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING CLIENT IS CLOSED ------------------------------------");
        assertEquals((int) clientStatusHashMap.get("id"), 600);
        LOG.info("Client Status:" + clientStatusHashMap + "\n");
    }

    public static void verifyClientPending(final HashMap<String, Object> clientStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING CLIENT IS PENDING ------------------------------------");
        assertEquals((int) clientStatusHashMap.get("id"), 100);
        LOG.info("Client Status:" + clientStatusHashMap + "\n");
    }

    public static void verifyClientRejected(final HashMap<String, Object> clientStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING CLIENT IS REJECTED ------------------------------------");
        assertEquals((int) clientStatusHashMap.get("id"), 700);
        LOG.info("Client Status:" + clientStatusHashMap + "\n");
    }

    public static void verifyClientActiavted(final HashMap<String, Object> clientStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING CLIENT IS ACTIVATED ------------------------------------");
        assertEquals((int) clientStatusHashMap.get("id"), 300);
        LOG.info("Client Status:" + clientStatusHashMap + "\n");
    }

    public static void verifyClientWithdrawn(final HashMap<String, Object> clientStatusHashMap) {
        LOG.info("\n-------------------------------------- VERIFYING CLIENT IS WITHDRAWN ------------------------------------");
        assertEquals((int) clientStatusHashMap.get("id"), 800);
        LOG.info("Client Status:" + clientStatusHashMap + "\n");
    }

}
