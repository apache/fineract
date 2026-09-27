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

import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.CREATED_DATE;
import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.CREATED_DATE_PLUS_ONE;
import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.CREATED_DATE_PLUS_TWO;
import static org.apache.fineract.integrationtests.client.feign.modules.ClientTestData.DEFAULT_SUBMITTED_ON_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ClientUndoRejectAndWithdrawalIntegrationTest extends FeignIntegrationTest {

    private static final int BAD_REQUEST = 400;
    private static final int FORBIDDEN = 403;

    private FeignClientHelper clientHelper;

    @BeforeAll
    public void setup() {
        clientHelper = new FeignClientHelper(fineractClient());
    }

    @Test
    public void clientUndoRejectIntegrationTest() {

        // CREATE CLIENT
        final Long clientId = createPendingClient();
        assertEquals(clientId, clientHelper.getClient(clientId).getId());

        // GET CLIENT STATUS
        assertClientStatus(clientId, ClientStatus.PENDING);

        clientHelper.rejectClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.REJECTED);

        clientHelper.undoRejectClient(clientId, ClientRequestBuilders.undoRejectClient(CREATED_DATE_PLUS_TWO));
        assertClientStatus(clientId, ClientStatus.PENDING);

    }

    @Test
    public void testClientUndoRejectWithDateBeforeRejectDate() {
        // CREATE CLIENT
        final Long clientId = createPendingClient();
        Assertions.assertNotNull(clientId);

        // GET CLIENT STATUS
        assertClientStatus(clientId, ClientStatus.PENDING);

        clientHelper.rejectClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.REJECTED);

        CallFailedRuntimeException error = clientHelper.undoRejectClientExpectingError(clientId,
                ClientRequestBuilders.undoRejectClient(CREATED_DATE));
        assertError(FORBIDDEN, "error.msg.client.reopened.date.cannot.before.client.rejected.date", error);

        clientHelper.undoRejectClient(clientId, ClientRequestBuilders.undoRejectClient(CREATED_DATE_PLUS_TWO));
        assertClientStatus(clientId, ClientStatus.PENDING);
    }

    @Test
    public void testClientUndoRejectWithoutReject() {
        // CREATE CLIENT
        final Long clientId = createPendingClient();
        Assertions.assertNotNull(clientId);

        // GET CLIENT STATUS
        assertClientStatus(clientId, ClientStatus.PENDING);

        LocalDate todaysDate = Utils.getLocalDateOfTenant();
        final String undoRejectDate = todaysDate.format(Utils.dateFormatter);

        CallFailedRuntimeException error = clientHelper.undoRejectClientExpectingError(clientId,
                ClientRequestBuilders.undoRejectClient(undoRejectDate));
        assertError(FORBIDDEN, "error.msg.client.undorejection.on.nonrejected.account", error);

        assertClientStatus(clientId, ClientStatus.PENDING);

    }

    @Test
    public void testClientUndoRejectWithFutureDate() {

        // CREATE CLIENT
        final Long clientId = createPendingClient();
        Assertions.assertNotNull(clientId);

        // GET CLIENT STATUS
        assertClientStatus(clientId, ClientStatus.PENDING);

        clientHelper.rejectClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.REJECTED);
        LocalDate tomorrowsDate = Utils.getLocalDateOfTenant().plusDays(1);
        final String undoRejectDate = tomorrowsDate.format(Utils.dateFormatter);
        CallFailedRuntimeException error = clientHelper.undoRejectClientExpectingError(clientId,
                ClientRequestBuilders.undoRejectClient(undoRejectDate));
        assertError(BAD_REQUEST, "validation.msg.client.reopenedDate.is.greater.than.date", error);

        clientHelper.undoRejectClient(clientId, ClientRequestBuilders.undoRejectClient(CREATED_DATE_PLUS_TWO));
        assertClientStatus(clientId, ClientStatus.PENDING);

    }

    @Test
    public void clientUndoWithDrawnIntegrationTest() {

        // CREATE CLIENT
        final Long clientId = createPendingClient();
        Assertions.assertNotNull(clientId);

        // GET CLIENT STATUS
        assertClientStatus(clientId, ClientStatus.PENDING);

        clientHelper.withdrawClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.WITHDRAWN);

        clientHelper.undoWithdrawnClient(clientId, ClientRequestBuilders.undoWithdrawnClient(CREATED_DATE_PLUS_TWO));
        assertClientStatus(clientId, ClientStatus.PENDING);

    }

    @Test
    public void testClientUndoWithDrawnWithDateBeforeWithdrawal() {

        // CREATE CLIENT
        final Long clientId = createPendingClient();
        Assertions.assertNotNull(clientId);

        // GET CLIENT STATUS
        assertClientStatus(clientId, ClientStatus.PENDING);

        clientHelper.withdrawClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.WITHDRAWN);

        CallFailedRuntimeException error = clientHelper.undoWithdrawnClientExpectingError(clientId,
                ClientRequestBuilders.undoWithdrawnClient(CREATED_DATE));
        assertError(FORBIDDEN, "error.msg.client.reopened.date.cannot.before.client.withdrawal.date", error);

        clientHelper.undoWithdrawnClient(clientId, ClientRequestBuilders.undoWithdrawnClient(CREATED_DATE_PLUS_TWO));
        assertClientStatus(clientId, ClientStatus.PENDING);

    }

    @Test
    public void testClientUndoWithDrawnWithoutWithdrawal() {
        // CREATE CLIENT
        final Long clientId = createPendingClient();
        Assertions.assertNotNull(clientId);

        LocalDate todaysDate = Utils.getLocalDateOfTenant();
        final String undoWithdrawDate = todaysDate.format(Utils.dateFormatter);

        CallFailedRuntimeException error = clientHelper.undoWithdrawnClientExpectingError(clientId,
                ClientRequestBuilders.undoWithdrawnClient(undoWithdrawDate));
        assertError(FORBIDDEN, "error.msg.client.undoWithdrawal.on.nonwithdrawal.account", error);

        assertClientStatus(clientId, ClientStatus.PENDING);

    }

    @Test
    public void testClientUndoWithDrawnWithFutureDate() {

        // CREATE CLIENT
        final Long clientId = createPendingClient();
        Assertions.assertNotNull(clientId);

        // GET CLIENT STATUS
        assertClientStatus(clientId, ClientStatus.PENDING);

        clientHelper.withdrawClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.WITHDRAWN);
        LocalDate tomorrowsDate = Utils.getLocalDateOfTenant().plusDays(1);
        final String undoWithdrawDate = tomorrowsDate.format(Utils.dateFormatter);
        CallFailedRuntimeException error = clientHelper.undoWithdrawnClientExpectingError(clientId,
                ClientRequestBuilders.undoWithdrawnClient(undoWithdrawDate));
        assertError(BAD_REQUEST, "validation.msg.client.reopenedDate.is.greater.than.date", error);

        clientHelper.undoWithdrawnClient(clientId, ClientRequestBuilders.undoWithdrawnClient(CREATED_DATE_PLUS_TWO));
        assertClientStatus(clientId, ClientStatus.PENDING);
    }

    @Test
    public void testValidateReopenedDate() {
        // CREATE CLIENT
        final Long clientId = createPendingClient();
        Assertions.assertNotNull(clientId);
        // GET CLIENT STATUS
        assertClientStatus(clientId, ClientStatus.PENDING);

        clientHelper.withdrawClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.WITHDRAWN);
        clientHelper.undoWithdrawnClient(clientId, ClientRequestBuilders.undoWithdrawnClient(CREATED_DATE_PLUS_TWO));
        assertClientStatus(clientId, ClientStatus.PENDING);
        CallFailedRuntimeException error = clientHelper.activateClientExpectingError(clientId,
                ClientRequestBuilders.activateClient(CREATED_DATE_PLUS_ONE));
        assertError(BAD_REQUEST, "error.msg.clients.submittedOnDate.after.reopened.date", error);

    }

    @Test
    public void testReopenedDate() {
        // CREATE CLIENT
        final Long clientId = createPendingClient();
        Assertions.assertNotNull(clientId);
        // GET CLIENT STATUS
        assertClientStatus(clientId, ClientStatus.PENDING);

        clientHelper.withdrawClient(clientId, CREATED_DATE_PLUS_ONE);
        assertClientStatus(clientId, ClientStatus.WITHDRAWN);
        clientHelper.undoWithdrawnClient(clientId, ClientRequestBuilders.undoWithdrawnClient(CREATED_DATE_PLUS_TWO));
        assertClientStatus(clientId, ClientStatus.PENDING);
        clientHelper.activateClient(clientId, ClientRequestBuilders.activateClient(CREATED_DATE_PLUS_TWO));

    }

    private Long createPendingClient() {
        return clientHelper.createClientPending(DEFAULT_SUBMITTED_ON_DATE).getClientId();
    }

    private void assertClientStatus(Long clientId, ClientStatus expected) {
        ClientStatusChecker.verifyClientStatus(expected, clientHelper.getClient(clientId));
    }

    private static void assertError(int expectedStatus, String expectedCode, CallFailedRuntimeException error) {
        assertEquals(expectedStatus, error.getStatus());
        assertEquals(expectedCode, FeignErrors.errorGlobalisationCode(error));
    }
}
