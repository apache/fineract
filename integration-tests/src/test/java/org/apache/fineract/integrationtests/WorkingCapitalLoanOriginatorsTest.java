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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.LoanOriginatorData;
import org.apache.fineract.client.models.LoanOriginatorsResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansOriginatorData;
import org.apache.fineract.integrationtests.client.feign.helpers.WorkingCapitalLoanOriginatorHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanOriginatorsTest {

    private final WorkingCapitalLoanHelper wcLoanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final WorkingCapitalLoanOriginatorHelper originatorHelper = new WorkingCapitalLoanOriginatorHelper();

    @Test
    public void testCreateWorkingCapitalLoanWithOriginator() {
        final String originatorExternalId = Utils.randomStringGenerator("originator", 20);
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId, "Test Originator");
        final Long productId = createProduct();
        final Long clientId = createClient();

        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withOriginators(List.of(new PostWorkingCapitalLoansOriginatorData().id(originatorId))) //
                .buildSubmitRequest();

        final Long loanId = wcLoanHelper.submit(json);
        assertNotNull(loanId);
        assertTrue(loanId > 0);

        final GetWorkingCapitalLoansLoanIdResponse loanDetails = wcLoanHelper.retrieveById(loanId);
        assertNotNull(loanDetails);
        assertNotNull(loanDetails.getOriginators());
        assertThat(loanDetails.getOriginators()).hasSize(1);
        assertEquals(originatorId.longValue(), loanDetails.getOriginators().get(0).getId());
        assertEquals(originatorExternalId, loanDetails.getOriginators().get(0).getExternalId());

        originatorHelper.detachOriginatorFromWorkingCapitalLoan(loanId, originatorId);
        wcLoanHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testCreateWorkingCapitalLoanWithMultipleOriginators() {
        final String originatorExternalId1 = Utils.randomStringGenerator("originator", 20);
        final String originatorExternalId2 = Utils.randomStringGenerator("originator", 20);
        final Long originatorId1 = originatorHelper.createOriginator(originatorExternalId1, "Originator 1");
        final Long originatorId2 = originatorHelper.createOriginator(originatorExternalId2, "Originator 2");
        final Long productId = createProduct();
        final Long clientId = createClient();

        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withOriginators(List.of(new PostWorkingCapitalLoansOriginatorData().id(originatorId1),
                        new PostWorkingCapitalLoansOriginatorData().id(originatorId2))) //
                .buildSubmitRequest();

        final Long loanId = wcLoanHelper.submit(json);
        assertNotNull(loanId);
        assertTrue(loanId > 0);

        final GetWorkingCapitalLoansLoanIdResponse loanDetails = wcLoanHelper.retrieveById(loanId);
        assertNotNull(loanDetails);
        assertNotNull(loanDetails.getOriginators());
        assertThat(loanDetails.getOriginators()).hasSize(2);

        Set<Long> originatorIds = new HashSet<>();
        Set<String> originatorExternalIds = new HashSet<>();
        loanDetails.getOriginators().forEach(originator -> {
            originatorIds.add(originator.getId());
            originatorExternalIds.add(originator.getExternalId());
        });

        assertThat(originatorIds).containsExactlyInAnyOrder(originatorId1, originatorId2);
        assertThat(originatorExternalIds).containsExactlyInAnyOrder(originatorExternalId1, originatorExternalId2);

        originatorHelper.detachOriginatorFromWorkingCapitalLoan(loanId, originatorId2);
        originatorHelper.detachOriginatorFromWorkingCapitalLoan(loanId, originatorId1);
        wcLoanHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
        originatorHelper.deleteOriginator(originatorId2);
        originatorHelper.deleteOriginator(originatorId1);
    }

    @Test
    public void testCreateWorkingCapitalLoanWithoutOriginatorAndAttachLater() {
        final String originatorExternalId = Utils.randomStringGenerator("originator", 20);
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId, "Test Originator");
        final Long productId = createProduct();
        final Long clientId = createClient();

        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest();

        final Long loanId = wcLoanHelper.submit(json);
        assertNotNull(loanId);
        assertTrue(loanId > 0);

        originatorHelper.attachOriginatorToWorkingCapitalLoan(loanId, originatorId);

        final GetWorkingCapitalLoansLoanIdResponse loanDetails = wcLoanHelper.retrieveById(loanId);
        assertNotNull(loanDetails);
        assertNotNull(loanDetails.getOriginators());
        assertThat(loanDetails.getOriginators()).hasSize(1);
        assertEquals(originatorId.longValue(), loanDetails.getOriginators().get(0).getId());
        assertEquals(originatorExternalId, loanDetails.getOriginators().get(0).getExternalId());

        originatorHelper.detachOriginatorFromWorkingCapitalLoan(loanId, originatorId);
        wcLoanHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testRetrieveWorkingCapitalLoanOriginators() {
        final String originatorExternalId1 = Utils.randomStringGenerator("originator", 20);
        final String originatorExternalId2 = Utils.randomStringGenerator("originator", 20);
        final Long originatorId1 = originatorHelper.createOriginator(originatorExternalId1, "Originator 1");
        final Long originatorId2 = originatorHelper.createOriginator(originatorExternalId2, "Originator 2");
        final Long productId = createProduct();
        final Long clientId = createClient();

        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withOriginators(List.of(new PostWorkingCapitalLoansOriginatorData().id(originatorId1))) //
                .buildSubmitRequest();

        final Long loanId = wcLoanHelper.submit(json);
        assertNotNull(loanId);

        originatorHelper.attachOriginatorToWorkingCapitalLoan(loanId, originatorId2);

        LoanOriginatorsResponse loanOriginatorsResponse = originatorHelper.retrieveOriginatorsByWorkingCapitalLoanId(loanId);
        assertNotNull(loanOriginatorsResponse);
        assertNotNull(loanOriginatorsResponse.getOriginators());
        assertThat(loanOriginatorsResponse.getOriginators()).hasSize(2);

        Set<Long> actualLoanOriginators = loanOriginatorsResponse.getOriginators().stream().map(LoanOriginatorData::getId)
                .collect(Collectors.toSet());
        assertThat(actualLoanOriginators).containsExactlyInAnyOrder(originatorId1, originatorId2);

        originatorHelper.detachOriginatorFromWorkingCapitalLoan(loanId, originatorId1);
        originatorHelper.detachOriginatorFromWorkingCapitalLoan(loanId, originatorId2);
        wcLoanHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
        originatorHelper.deleteOriginator(originatorId2);
        originatorHelper.deleteOriginator(originatorId1);
    }

    @Test
    public void testDetachOriginatorFromWorkingCapitalLoan() {
        final String originatorExternalId = Utils.randomStringGenerator("originator", 20);
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId, "Test Originator");
        final Long productId = createProduct();
        final Long clientId = createClient();

        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .withOriginators(List.of(new PostWorkingCapitalLoansOriginatorData().id(originatorId))) //
                .buildSubmitRequest();

        final Long loanId = wcLoanHelper.submit(json);
        assertNotNull(loanId);

        GetWorkingCapitalLoansLoanIdResponse loanDetails = wcLoanHelper.retrieveById(loanId);
        assertThat(loanDetails.getOriginators()).hasSize(1);

        originatorHelper.detachOriginatorFromWorkingCapitalLoan(loanId, originatorId);

        loanDetails = wcLoanHelper.retrieveById(loanId);
        assertThat(loanDetails.getOriginators()).isEmpty();

        wcLoanHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
        originatorHelper.deleteOriginator(originatorId);
    }

    @Test
    public void testUserWithoutPermissionsCannotAttachOrDetachOriginator() {
        Utils.initializeRESTAssured();
        RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        final Integer roleId = RolesHelper.createRole(requestSpec, responseSpec);
        assertNotNull(roleId);

        final HashMap<String, Boolean> permissions = new HashMap<>();
        permissions.put("READ_WORKINGCAPITALLOAN", true);
        RolesHelper.addPermissionsToRole(requestSpec, responseSpec, roleId, permissions);

        final String username = Utils.uniqueRandomStringGenerator("WCLOriginatorUser", 4);
        final String password = "Str0ngP@sw0rd!";
        final GetOfficesResponse headOffice = OfficeHelper.getHeadOffice();
        final PostUsersRequest createUserRequest = new PostUsersRequest().username(username).firstname(Utils.randomFirstNameGenerator())
                .lastname(Utils.randomLastNameGenerator()).email("wcloriginator@test.org").password(password).repeatPassword(password)
                .sendPasswordToEmail(false).roles(List.of(roleId.longValue())).officeId(headOffice.getId());
        final PostUsersResponse userResponse = UserHelper.createUser(requestSpec, responseSpec, createUserRequest);
        assertNotNull(userResponse.getResourceId());

        final FineractFeignClient userClient = FineractFeignClientHelper.createNewFineractFeignClient(username, password);

        final String originatorExternalId = Utils.randomStringGenerator("originator", 20);
        final Long originatorId = originatorHelper.createOriginator(originatorExternalId, "Permission Test Originator");
        final Long productId = createProduct();
        final Long clientId = createClient();

        final var json = new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(BigDecimal.valueOf(5000)) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(BigDecimal.valueOf(5500)) //
                .buildSubmitRequest();

        final Long loanId = wcLoanHelper.submit(json);
        assertNotNull(loanId);

        CallFailedRuntimeException attachException = FeignCalls
                .failVoid(() -> userClient.workingCapitalLoanOriginators().attachOriginatorToWorkingCapitalLoan(loanId, originatorId));
        assertThat(attachException.getStatus()).isEqualTo(403);

        final HashMap<String, Boolean> attachPermission = new HashMap<>();
        attachPermission.put("ATTACH_WORKING_CAPITAL_LOAN_ORIGINATOR", true);
        RolesHelper.addPermissionsToRole(requestSpec, responseSpec, roleId, attachPermission);

        FeignCalls.ok(() -> userClient.workingCapitalLoanOriginators().attachOriginatorToWorkingCapitalLoan(loanId, originatorId));

        CallFailedRuntimeException detachException = FeignCalls
                .failVoid(() -> userClient.workingCapitalLoanOriginators().detachOriginatorFromWorkingCapitalLoan(loanId, originatorId));
        assertThat(detachException.getStatus()).isEqualTo(403);

        final HashMap<String, Boolean> detachPermission = new HashMap<>();
        detachPermission.put("DETACH_WORKING_CAPITAL_LOAN_ORIGINATOR", true);
        RolesHelper.addPermissionsToRole(requestSpec, responseSpec, roleId, detachPermission);

        FeignCalls.ok(() -> userClient.workingCapitalLoanOriginators().detachOriginatorFromWorkingCapitalLoan(loanId, originatorId));

        wcLoanHelper.deleteById(loanId);
        productHelper.deleteWorkingCapitalLoanProductById(productId);
        originatorHelper.deleteOriginator(originatorId);
    }

    private Long createProduct() {
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withPrincipalAmountMin(BigDecimal.valueOf(1000)) //
                .withPrincipalAmountMax(BigDecimal.valueOf(50000)) //
                .withPrincipalAmountDefault(BigDecimal.valueOf(10000)) //
                .withMinPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_MIN_PERIOD_PAYMENT_RATE_PERCENT) //
                .withMaxPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_MAX_PERIOD_PAYMENT_RATE_PERCENT) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .build()) //
                .getResourceId();
    }

    private Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }
}
