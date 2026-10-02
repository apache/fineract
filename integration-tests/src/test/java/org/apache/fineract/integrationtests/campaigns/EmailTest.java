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
package org.apache.fineract.integrationtests.campaigns;

import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.EmailData;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostEmailRequest;
import org.apache.fineract.client.models.PutEmailRequest;
import org.apache.fineract.client.models.StaffCreateRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignEmailHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.FeignTestConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EmailTest extends FeignIntegrationTest {

    private static final String STAFF_JOINING_DATE = "20 September 2011";

    private FeignEmailHelper emailHelper;
    private FeignClientHelper clientHelper;
    private FeignStaffHelper staffHelper;

    @BeforeEach
    public void setup() {
        emailHelper = new FeignEmailHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        staffHelper = new FeignStaffHelper(fineractClient());
    }

    @Test
    public void testEmailCreateRetrieveUpdateDeleteLifecycle() {
        // Arrange: client must have an emailAddress, since EmailMessageAssembler
        // derives the recipient address from it (no address => data integrity exception).
        PostClientsResponse client = clientHelper.createClient(
                ClientRequestBuilders.defaultClient().emailAddress(Utils.randomStringGenerator("email_", 6) + "@example.com"));

        String initialSubject = Utils.randomStringGenerator("Subject_", 10);
        String initialMessage = Utils.randomStringGenerator("Message_", 20);

        PostEmailRequest createRequest = new PostEmailRequest().clientId(client.getClientId()).emailSubject(initialSubject)
                .emailMessage(initialMessage).locale(FeignTestConstants.LOCALE);

        // Act: CREATE
        Long emailId = emailHelper.createEmail(createRequest).getResourceId();

        // Assert: RETRIEVE after create
        EmailData created = emailHelper.retrieveEmail(emailId);
        assertThat(created.getId()).isEqualTo(emailId);
        assertThat(created.getClientId()).isEqualTo(client.getClientId());
        assertThat(created.getEmailSubject()).isEqualTo(initialSubject);
        assertThat(created.getEmailMessage()).isEqualTo(initialMessage);

        // Act: UPDATE (only emailMessage is a supported update param)
        String updatedMessage = Utils.randomStringGenerator("UpdatedMessage_", 20);

        CommandProcessingResult updateResponse = emailHelper.updateEmail(emailId, new PutEmailRequest().emailMessage(updatedMessage));
        assertThat(updateResponse.getResourceId()).isEqualTo(emailId);
        assertThat(updateResponse.getChanges().get("emailMessage")).isEqualTo(updatedMessage);

        // Assert: RETRIEVE after update
        EmailData updated = emailHelper.retrieveEmail(emailId);
        assertThat(updated.getEmailMessage()).isEqualTo(updatedMessage);
        // subject is untouched by update, since UPDATE_REQUEST_DATA_PARAMETERS only allows emailMessage
        assertThat(updated.getEmailSubject()).isEqualTo(initialSubject);

        // Act: DELETE
        Long deletedResourceId = emailHelper.deleteEmail(emailId).getResourceId();
        assertThat(deletedResourceId).isEqualTo(emailId);

        // Assert: RETRIEVE after delete should 404
        CallFailedRuntimeException notFound = emailHelper.retrieveEmailExpectingError(emailId);
        assertThat(notFound.getStatus()).isEqualTo(404);
    }

    @Test
    public void testEmailCreateWithStaffIdOnlyDoesNotThrow() {
        // Arrange: staff must have an emailAddress, since EmailMessageAssembler
        // derives the recipient address from it (no address => data integrity exception,
        // which the platform maps to a 403 -- Postgres enforces the NOT NULL constraint
        // on email_address strictly, unlike MySQL/MariaDB in non-strict mode).
        StaffCreateRequest staffRequest = new StaffCreateRequest().joiningDate(STAFF_JOINING_DATE)
                .dateFormat(FeignTestConstants.DATETIME_PATTERN).locale(FeignTestConstants.LOCALE)
                .officeId(FeignOfficeHelper.HEAD_OFFICE_ID).firstname(Utils.uniqueRandomStringGenerator("staff_", 5))
                .lastname(Utils.uniqueRandomStringGenerator("Doe_", 4)).isLoanOfficer(true)
                .emailAddress(Utils.randomStringGenerator("staff_email_", 6) + "@example.com");

        Long staffId = staffHelper.createStaff(staffRequest).getResourceId();

        PostEmailRequest createRequest = new PostEmailRequest().staffId(staffId).emailSubject(Utils.randomStringGenerator("Subject_", 10))
                .emailMessage(Utils.randomStringGenerator("Message_", 20)).locale(FeignTestConstants.LOCALE);

        Long emailId = emailHelper.createEmail(createRequest).getResourceId();

        assertThat(emailHelper.retrieveEmail(emailId).getStaffId()).isEqualTo(staffId);
    }

    @Test
    public void testEmailCreateWithoutClientOrStaffIdFails() {
        PostEmailRequest createRequest = new PostEmailRequest().emailSubject(Utils.randomStringGenerator("Subject_", 10))
                .emailMessage(Utils.randomStringGenerator("Message_", 20)).locale(FeignTestConstants.LOCALE);

        CallFailedRuntimeException exception = emailHelper.createEmailExpectingError(createRequest);
        assertThat(exception.getStatus()).isEqualTo(400);
    }
}
