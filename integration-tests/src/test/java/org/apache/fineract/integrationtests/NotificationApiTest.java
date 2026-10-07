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
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetNotification;
import org.apache.fineract.client.models.GetNotificationsResponse;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignNotificationHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
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
    private FeignNotificationHelper notificationHelper;
    private FeignNotificationHelper newUserNotificationHelper;

    @BeforeEach
    public void setUp() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        notificationHelper = new FeignNotificationHelper(FineractFeignClientHelper.getFineractFeignClient());

        GetOfficesResponse headOffice = OfficeHelper.getHeadOffice();
        String username = Utils.uniqueRandomStringGenerator("NotificationUser", 4);
        String password = Utils.randomStringGenerator("A1b2c3d4e5f$", 1);

        PostUsersRequest createUserRequest = new PostUsersRequest().username(username).firstname(Utils.randomFirstNameGenerator())
                .lastname(Utils.randomLastNameGenerator()).email("whatever@mifos.org").password(password).repeatPassword(password)
                .sendPasswordToEmail(false).roles(List.of(SUPER_USER_ROLE_ID)).officeId(headOffice.getId());

        PostUsersResponse userCreationResponse = UserHelper.createUser(createUserRequest);
        Assertions.assertNotNull(userCreationResponse.getResourceId());

        FineractFeignClient newUserClient = FineractFeignClientHelper.createNewFineractFeignClient(username, password);
        newUserNotificationHelper = new FeignNotificationHelper(newUserClient);
    }

    @Test
    public void testNotificationRetrievalWorksWhenNoNotificationsAreAvailable() {
        // given
        // when
        GetNotificationsResponse response = notificationHelper.getNotifications();
        // then
        Assertions.assertNotNull(response);
    }

    @Test
    public void testNotificationRetrievalWorksWhenOneNotificationIsAvailable() {
        // given (still using RestAssured-based ClientHelper - to be migrated separately)
        PostClientsRequest clientRequest = ClientHelper.defaultClientCreationRequest();
        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, clientRequest);
        Assertions.assertNotNull(clientId);

        // when
        newUserNotificationHelper.waitUntilNotificationsAreAvailable();
        GetNotificationsResponse response = newUserNotificationHelper.getNotifications();
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

    @Test
    public void testNotificationReadStatusFiltering() {
        // given
        PostClientsRequest clientRequest = ClientHelper.defaultClientCreationRequest();
        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, clientRequest);
        Assertions.assertNotNull(clientId);

        newUserNotificationHelper.waitUntilNotificationsAreAvailable();

        // verify the new notification is unread
        GetNotificationsResponse unreadResponse = newUserNotificationHelper.getUnreadNotifications();
        Assertions.assertNotNull(unreadResponse);
        Assertions.assertEquals(1, unreadResponse.getPageItems().size());
        Assertions.assertFalse(unreadResponse.getPageItems().get(0).getIsRead());

        // mark the notification as read
        newUserNotificationHelper.markNotificationsAsRead();

        // verify it is removed from the unread result
        GetNotificationsResponse unreadAfterUpdate = newUserNotificationHelper.getUnreadNotifications();
        Assertions.assertNotNull(unreadAfterUpdate);
        Assertions.assertTrue(unreadAfterUpdate.getPageItems().isEmpty());

        GetNotificationsResponse allNotifications = newUserNotificationHelper.getNotifications();
        Assertions.assertNotNull(allNotifications);
        Assertions.assertEquals(1, allNotifications.getPageItems().size());

        GetNotification notification = allNotifications.getPageItems().get(0);
        Assertions.assertTrue(notification.getIsRead());
        Assertions.assertEquals(clientId.longValue(), notification.getObjectId());
    }
}
