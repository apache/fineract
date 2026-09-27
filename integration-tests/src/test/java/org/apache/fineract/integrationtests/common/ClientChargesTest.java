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
package org.apache.fineract.integrationtests.common;

import java.math.BigDecimal;
import org.apache.fineract.client.models.PostClientsClientIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostClientsClientIdChargesRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * IntegrationTest for ClientCharges.
 *
 */
/**
 * @author lenovo
 *
 */
public class ClientChargesTest extends FeignIntegrationTest {

    private static final int BAD_REQUEST = 400;
    private static final int FORBIDDEN = 403;
    private static final double CHARGE_AMOUNT = 100.0;
    private static final BigDecimal CLIENT_CHARGE_AMOUNT = BigDecimal.valueOf(200);
    private static final String CHARGE_DUE_DATE = "29 October 2011";

    private FeignClientHelper clientHelper;
    private FeignChargesHelper chargesHelper;

    @BeforeAll
    public void setup() {
        clientHelper = new FeignClientHelper(fineractClient());
        chargesHelper = new FeignChargesHelper(fineractClient());
    }

    @Test
    public void clientChargeTest() {

        // Creates clientCharge
        final Long chargeId = chargesHelper.createClientSpecifiedDueDateCharge(CHARGE_AMOUNT).getResourceId();
        Assertions.assertNotNull(chargeId);

        // creates client with activation date
        final Long clientId = clientHelper.createClient("01 October 2011");
        Assertions.assertNotNull(clientId);

        /**
         * create a charge for loan and try to associate to client created in the above lines.it will be an invalid
         * scenario the reason is client is not allowed to have only client charge.
         *
         */
        final Long loanChargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(CHARGE_AMOUNT).getResourceId();
        Assertions.assertNotNull(loanChargeId);
        Assertions.assertEquals(FORBIDDEN, chargesHelper.addClientChargeExpectingError(clientId, clientCharge(loanChargeId)).getStatus());

        /**
         * associates a clientCharge to a client and pay client charge for 10 USD--success scenario
         **/
        final Long clientChargeId = chargesHelper.addClientCharge(clientId, clientCharge(chargeId)).getResourceId();
        Assertions.assertNotNull(clientChargeId);
        final Long clientChargePaidTransactionId = chargesHelper
                .payClientCharge(clientId, clientChargeId, ChargeRequestBuilders.payClientCharge("25 AUGUST 2015", 10)).getTransactionId();
        Assertions.assertNotNull(clientChargePaidTransactionId);
        isValidOutstandingAmount(clientId, clientChargeId, BigDecimal.valueOf(190));

        /**
         * Revert the paid client charge transaction by passing the clientChargePaidTransactionId and ensure the same is
         * reverted.
         */
        final Long undoTrxnId = clientHelper.undoClientTransaction(clientId, clientChargePaidTransactionId).getResourceId();
        Assertions.assertNotNull(undoTrxnId);
        isReversedTransaction(clientId, undoTrxnId);
        /**
         * Now pay client charge for 20 USD and ensure the outstanding amount is updated properly
         */
        final String futureDate = Utils.getLocalDateOfTenant().plusDays(2).format(Utils.dateFormatter);
        assertPaymentRejected(clientId, clientChargeId, ChargeRequestBuilders.payClientCharge(futureDate, 20));

        // waived off the outstanding client charge
        final Long waiveOffClientChargeTransactionId = chargesHelper
                .waiveClientCharge(clientId, clientChargeId, ChargeRequestBuilders.waiveClientCharge(100)).getTransactionId();
        Assertions.assertNotNull(waiveOffClientChargeTransactionId);

        /**
         * Revert the waived off client charge transaction by passing the waiveOffClientChargeTransactionId and ensured
         * the transaction is reversed.
         */
        final Long undoWaiveTrxnId = clientHelper.undoClientTransaction(clientId, waiveOffClientChargeTransactionId).getResourceId();
        Assertions.assertNotNull(undoWaiveTrxnId);
        isReversedTransaction(clientId, undoWaiveTrxnId);
        /**
         * pay client charge before client activation date and ensured its a failure test case
         */

        assertPaymentRejected(clientId, clientChargeId, ChargeRequestBuilders.payClientCharge("30 September 2011", 20));
        /**
         * pay client charge more than outstanding amount amount and ensured its a failure test case
         */
        assertPaymentRejected(clientId, clientChargeId, ChargeRequestBuilders.payClientCharge("25 AUGUST 2015", 300));
        /**
         * pay client charge for 10 USD and ensure outstanding amount is updated properly
         */
        final Long chargePaidResponseId = chargesHelper
                .payClientCharge(clientId, clientChargeId, ChargeRequestBuilders.payClientCharge("25 AUGUST 2015", 100)).getTransactionId();
        Assertions.assertNotNull(chargePaidResponseId);

        isValidOutstandingAmount(clientId, clientChargeId, BigDecimal.valueOf(100));

    }

    private static PostClientsClientIdChargesRequest clientCharge(Long chargeId) {
        return ChargeRequestBuilders.clientCharge(chargeId, CHARGE_DUE_DATE, CLIENT_CHARGE_AMOUNT);
    }

    private void assertPaymentRejected(Long clientId, Long clientChargeId, PostClientsClientIdChargesChargeIdRequest request) {
        Assertions.assertEquals(BAD_REQUEST, chargesHelper.payClientChargeExpectingError(clientId, clientChargeId, request).getStatus());
    }

    /**
     * It checks whether the client charge transaction is reversed or not.
     *
     * @param clientId
     * @param transactionId
     */
    private void isReversedTransaction(Long clientId, Long transactionId) {
        Assertions.assertTrue(clientHelper.getClientTransaction(clientId, transactionId).getReversed());
    }

    /**
     * Check whether the outStandingAmount is equal to expected Amount or not after paying or after waiving off the
     * client charge.
     *
     * @param clientId
     * @param clientChargeId
     * @param expectedAmount
     */
    private void isValidOutstandingAmount(Long clientId, Long clientChargeId, BigDecimal expectedAmount) {
        BigDecimal outstandingAmount = chargesHelper.getClientCharge(clientId, clientChargeId).getAmountOutstanding();
        Assertions.assertEquals(0, expectedAmount.compareTo(outstandingAmount),
                () -> "Expected outstanding amount " + expectedAmount + " but was " + outstandingAmount);
    }

}
