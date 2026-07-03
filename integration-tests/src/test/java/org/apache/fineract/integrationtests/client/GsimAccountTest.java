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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@code /groups/{groupId}/gsimaccounts} (FINERACT-2671).
 */
public class GsimAccountTest extends IntegrationTest {

    @Test
    public void retrieveGsimAccountsReturns404WhenParentGsimIdNotExisting() {
        // groupId is irrelevant on this code path: with parentGSIMId present, the lookup is by gsim.id only
        CallFailedRuntimeException e = assertThrows(CallFailedRuntimeException.class,
                () -> ok(fineractClient().groups.retrieveGsimAccountsGroup(1L, null, 999999L)));

        assertEquals(404, e.getResponse().code());
        assertTrue(e.getMessage().contains("error.msg.gsim.account.id.invalid"));
    }
}
