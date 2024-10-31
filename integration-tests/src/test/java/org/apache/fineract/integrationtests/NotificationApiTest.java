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

import static org.apache.fineract.integrationtests.useradministration.roles.RolesHelper.SUPER_USER_ROLE_ID;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import org.apache.fineract.client.models.GetNotification;
import org.apache.fineract.client.models.GetNotificationsResponse;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.NotificationHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NotificationApiTest {

    public static final int SUPER_USER_ID = 1;
    public static final String CLIENT_OBJECT_TYPE = "client";
    public static final String CREATED_ACTION_TYPE = "created";
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    private RequestSpecification newUserRequestSpec;
    private ResponseSpecification newUserResponseSpec;

    @BeforeEach
    public void setUp() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        GetOfficesResponse headOffice = OfficeHelper.getHeadOffice(requestSpec, responseSpec);
        String username = Utils.uniqueRandomStringGenerator("NotificationUser", 4);
        String password = Utils.randomStringGenerator("A1b2c3d4e5f$", 1); // prefix is to conform with the password
                                                                          // rules
        PostUsersRequest createUserRequest = new PostUsersRequest().username(username)
                .firstname(Utils.randomStringGenerator("NotificationFN", 4)).lastname(Utils.randomStringGenerator("NotificationLN", 4))
                .email("whatever@mifos.org").password(password).repeatPassword(password).sendPasswordToEmail(false)
                .roles(List.of(SUPER_USER_ROLE_ID)).officeId(headOffice.getId());

        PostUsersResponse userCreationResponse = UserHelper.createUser(requestSpec, responseSpec, createUserRequest);
        Assertions.assertNotNull(userCreationResponse.getResourceId());

        newUserRequestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        newUserRequestSpec.header("Authorization",
                "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(username, password));
        newUserResponseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

    }

    @Test
    public void testNotificationRetrievalWorksWhenNoNotificationsAreAvailable() {
        // given
        // when
        GetNotificationsResponse response = NotificationHelper.getNotifications(requestSpec, responseSpec);
        // then
        Assertions.assertNotNull(response);
    }

    @Test
    public void testNotificationRetrievalWorksWhenOneNotificationIsAvailable() {
        // given
        PostClientsRequest clientRequest = ClientHelper.defaultClientCreationRequest();
        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, clientRequest);
        Assertions.assertNotNull(clientId);

        // when
        NotificationHelper.waitUntilNotificationsAreAvailable(newUserRequestSpec, newUserResponseSpec);
        GetNotificationsResponse response = NotificationHelper.getNotifications(newUserRequestSpec, newUserResponseSpec);
        // then
        Assertions.assertNotNull(response);
        List<GetNotification> pageItems = response.getPageItems();
        Assertions.assertEquals(1, pageItems.size());
        GetNotification firstNotification = pageItems.get(0);
        Assertions.assertEquals(SUPER_USER_ID, firstNotification.getActorId());
        Assertions.assertEquals(false, firstNotification.getIsRead());
        Assertions.assertEquals(CREATED_ACTION_TYPE, firstNotification.getAction());
        Assertions.assertEquals(clientId.longValue(), firstNotification.getObjectId());
        Assertions.assertEquals(CLIENT_OBJECT_TYPE, firstNotification.getObjectType());
    }
}
