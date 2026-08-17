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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostRolesRequest;
import org.apache.fineract.client.models.PostRolesResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsRequest;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;

public final class FeignUserHelper {

    private static final String SIMPLE_USER_PASSWORD = "QwE!5rTy#9uP0";
    private static final String REPAYMENT_LOAN_PERMISSION = "REPAYMENT_LOAN";
    private static final String READ_LOAN_PERMISSION = "READ_LOAN";

    private static FineractFeignClient simpleUserWithoutBypassPermissionClient;

    private FeignUserHelper() {}

    /**
     * Lazily creates a simple user whose role lacks the loan-checker bypass permission (but can read and repay loans
     * and manage loan reschedules) and returns a Feign client authenticated as that user. Mirrors
     * {@code UserHelper.getSimpleUserWithoutBypassPermission}. The user and client are created once per JVM and reused.
     */
    public static FineractFeignClient getSimpleUserWithoutBypassPermissionClient() {
        if (simpleUserWithoutBypassPermissionClient == null) {
            String username = Utils.uniqueRandomStringGenerator("NonByPassUser", 4);
            createSimpleUser(username);
            simpleUserWithoutBypassPermissionClient = FineractFeignClientHelper.createNewFineractFeignClient(username,
                    SIMPLE_USER_PASSWORD);
        }
        return simpleUserWithoutBypassPermissionClient;
    }

    private static void createSimpleUser(String username) {
        FineractFeignClient adminClient = FineractFeignClientHelper.getFineractFeignClient();
        GetOfficesResponse headOffice = OfficeHelper.getHeadOffice();

        PostRolesResponse roleResponse = ok(
                () -> adminClient.roles().createRole(new PostRolesRequest().name(Utils.uniqueRandomStringGenerator("Role_Name_", 5))
                        .description(Utils.randomStringGenerator("Role_Description_", 10))));
        Long roleId = roleResponse.getResourceId();

        Map<String, Boolean> permissions = Map.of(REPAYMENT_LOAN_PERMISSION, true, READ_LOAN_PERMISSION, true, "READ_RESCHEDULELOAN", true,
                "CREATE_RESCHEDULELOAN", true, "REJECT_RESCHEDULELOAN", true, "APPROVE_RESCHEDULELOAN", true);
        ok(() -> adminClient.roles().updateRolePermissions(roleId, new PutRolesRoleIdPermissionsRequest().permissions(permissions)));

        PostUsersResponse userResponse = ok(() -> adminClient.users()
                .createUser(new PostUsersRequest().username(username).firstname(Utils.randomFirstNameGenerator())
                        .lastname(Utils.randomLastNameGenerator()).email("whatever@mifos.org").password(SIMPLE_USER_PASSWORD)
                        .repeatPassword(SIMPLE_USER_PASSWORD).sendPasswordToEmail(false).roles(List.of(roleId))
                        .officeId(headOffice.getId())));
        if (userResponse.getResourceId() == null) {
            throw new IllegalStateException("Failed to create non-bypass user " + username);
        }
    }
}
