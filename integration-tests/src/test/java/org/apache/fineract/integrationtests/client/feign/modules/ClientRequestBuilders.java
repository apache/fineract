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
package org.apache.fineract.integrationtests.client.feign.modules;

import org.apache.fineract.client.models.PostClientsClientIdRequest;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PutClientsClientIdRequest;
import org.apache.fineract.integrationtests.common.Utils;

public final class ClientRequestBuilders {

    private ClientRequestBuilders() {}

    public static PostClientsRequest createPendingClient(String submittedOnDate) {
        return new PostClientsRequest()//
                .officeId(1L)//
                .legalFormId(1L)//
                .firstname(Utils.randomFirstNameGenerator())//
                .lastname(Utils.randomLastNameGenerator())//
                .externalId(Utils.randomStringGenerator("EXT_", 7))//
                .active(false)//
                .submittedOnDate(submittedOnDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
    }

    public static PostClientsRequest createActivePersonClient(String activationDate) {
        return new PostClientsRequest()//
                .officeId(1L)//
                .legalFormId(1L)//
                .firstname(Utils.randomFirstNameGenerator())//
                .lastname(Utils.randomLastNameGenerator())//
                .active(true)//
                .activationDate(activationDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
    }

    public static PostClientsClientIdRequest activateClient(String activationDate) {
        return new PostClientsClientIdRequest()//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .activationDate(activationDate);
    }

    public static PostClientsClientIdRequest closeClient(Long closureReasonId, String closureDate) {
        return new PostClientsClientIdRequest()//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .closureReasonId(closureReasonId)//
                .closureDate(closureDate);
    }

    public static PostClientsClientIdRequest rejectClient(Long rejectionReasonId, String rejectionDate) {
        return new PostClientsClientIdRequest()//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .rejectionReasonId(rejectionReasonId)//
                .rejectionDate(rejectionDate);
    }

    public static PostClientsClientIdRequest reactivateClient(String reactivationDate) {
        return new PostClientsClientIdRequest()//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .reactivationDate(reactivationDate);
    }

    public static PostClientsClientIdRequest withdrawClient(Long withdrawalReasonId, String withdrawalDate) {
        return new PostClientsClientIdRequest()//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .withdrawalReasonId(withdrawalReasonId)//
                .withdrawalDate(withdrawalDate);
    }

    public static PostClientsClientIdRequest undoRejectClient(String reopenedDate) {
        return new PostClientsClientIdRequest()//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .reopenedDate(reopenedDate);
    }

    public static PostClientsClientIdRequest undoWithdrawnClient(String reopenedDate) {
        return new PostClientsClientIdRequest()//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .reopenedDate(reopenedDate);
    }

    public static PutClientsClientIdRequest updateClientName(String firstname, String lastname) {
        return new PutClientsClientIdRequest()//
                .firstname(firstname)//
                .lastname(lastname);
    }
}
