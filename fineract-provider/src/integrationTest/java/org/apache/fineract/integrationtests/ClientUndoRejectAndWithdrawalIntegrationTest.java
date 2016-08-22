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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.InvalidClientStateTransitionException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused" })
public class ClientUndoRejectAndWithdrawalIntegrationTest {

	private static final String CREATE_CLIENT_URL = "/fineract-provider/api/v1/clients?" + Utils.TENANT_IDENTIFIER;
	public static final String DATE_FORMAT = "dd MMMM yyyy";
	private final String submittedOnDate = "submittedOnDate";
	private final String officeId = "officeId";
	private ResponseSpecification responseSpec;
	private RequestSpecification requestSpec;
	private ClientHelper clientHelper;

	@Before
	public void setup() {
		Utils.initializeRESTAssured();
		this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
		this.requestSpec.header("Authorization",
				"Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
		this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
	}

	@Test
	public void clientUndoRejectIntegrationTest() {

		// CREATE CLIENT
		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
		final Integer clientId = ClientHelper.createClientPending(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(clientId);

		// GET CLIENT STATUS
		HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec,
				String.valueOf(clientId));

		ClientStatusChecker.verifyClientPending(status);

		status = this.clientHelper.rejectClient(clientId);
		ClientStatusChecker.verifyClientRejected(status);

		status = this.clientHelper.undoReject(clientId);
		ClientStatusChecker.verifyClientPending(status);

	}

	@Test
	public void testClientUndoRejectWithDateBeforeRejectDate() {
		final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
		final ClientHelper validationErrorHelper = new ClientHelper(this.requestSpec, errorResponse);

		// CREATE CLIENT
		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
		final Integer clientId = ClientHelper.createClientPending(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(clientId);

		// GET CLIENT STATUS
		HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec,
				String.valueOf(clientId));

		ClientStatusChecker.verifyClientPending(status);

		status = this.clientHelper.rejectClient(clientId);
		ClientStatusChecker.verifyClientRejected(status);

		ArrayList<HashMap> clientErrorData = (ArrayList<HashMap>) validationErrorHelper.undoRejectedclient(clientId,
				CommonConstants.RESPONSE_ERROR, ClientHelper.CREATED_DATE);
		assertEquals("error.msg.client.reopened.date.cannot.before.client.rejected.date",
				((HashMap<String, Object>) clientErrorData.get(0)).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

		status = this.clientHelper.undoReject(clientId);
		ClientStatusChecker.verifyClientPending(status);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testClientUndoRejectWithoutReject() {
		final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
		final ClientHelper validationErrorHelper = new ClientHelper(this.requestSpec, errorResponse);

		// CREATE CLIENT
		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
		final Integer clientId = ClientHelper.createClientPending(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(clientId);

		// GET CLIENT STATUS
		HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec,
				String.valueOf(clientId));
		ClientStatusChecker.verifyClientPending(status);

		DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
		Calendar todaysDate = Calendar.getInstance();
		final String undoRejectDate = dateFormat.format(todaysDate.getTime());

		ArrayList<HashMap> clientErrorData = (ArrayList<HashMap>) validationErrorHelper.undoRejectedclient(clientId,
				CommonConstants.RESPONSE_ERROR, undoRejectDate);
		assertEquals("error.msg.client.undorejection.on.nonrejected.account",
				((HashMap<String, Object>) clientErrorData.get(0)).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

		status = this.clientHelper.getClientStatus(requestSpec, responseSpec, String.valueOf(clientId));
		ClientStatusChecker.verifyClientPending(status);

	}

	@Test
	public void testClientUndoRejectWithFutureDate() {

		final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
		final ClientHelper validationErrorHelper = new ClientHelper(this.requestSpec, errorResponse);

		// CREATE CLIENT
		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
		final Integer clientId = ClientHelper.createClientPending(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(clientId);

		// GET CLIENT STATUS
		HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec,
				String.valueOf(clientId));

		ClientStatusChecker.verifyClientPending(status);

		status = this.clientHelper.rejectClient(clientId);
		ClientStatusChecker.verifyClientRejected(status);
		DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
		Calendar todaysDate = Calendar.getInstance();
		todaysDate.add(Calendar.DATE, 1);
		final String undoRejectDate = dateFormat.format(todaysDate.getTime());
		ArrayList<HashMap> clientErrorData = (ArrayList<HashMap>) validationErrorHelper.undoWithdrawclient(clientId,
				CommonConstants.RESPONSE_ERROR, undoRejectDate);
		assertEquals("validation.msg.client.reopenedDate.is.greater.than.date",
				((HashMap<String, Object>) clientErrorData.get(0)).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

		status = this.clientHelper.undoReject(clientId);
		ClientStatusChecker.verifyClientPending(status);

	}

	@Test
	public void clientUndoWithDrawnIntegrationTest() {

		// CREATE CLIENT
		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
		final Integer clientId = ClientHelper.createClientPending(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(clientId);

		// GET CLIENT STATUS
		HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec,
				String.valueOf(clientId));

		ClientStatusChecker.verifyClientPending(status);

		status = this.clientHelper.withdrawClient(clientId);
		ClientStatusChecker.verifyClientWithdrawn(status);

		status = this.clientHelper.undoWithdrawn(clientId);
		ClientStatusChecker.verifyClientPending(status);

	}

	@Test
	public void testClientUndoWithDrawnWithDateBeforeWithdrawal() {

		final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
		final ClientHelper validationErrorHelper = new ClientHelper(this.requestSpec, errorResponse);

		// CREATE CLIENT
		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
		final Integer clientId = ClientHelper.createClientPending(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(clientId);

		// GET CLIENT STATUS
		HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec,
				String.valueOf(clientId));

		ClientStatusChecker.verifyClientPending(status);

		status = this.clientHelper.withdrawClient(clientId);
		ClientStatusChecker.verifyClientWithdrawn(status);

		ArrayList<HashMap> clientErrorData = (ArrayList<HashMap>) validationErrorHelper.undoWithdrawclient(clientId,
				CommonConstants.RESPONSE_ERROR, ClientHelper.CREATED_DATE);
		assertEquals("error.msg.client.reopened.date.cannot.before.client.withdrawal.date",
				((HashMap<String, Object>) clientErrorData.get(0)).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

		status = this.clientHelper.undoWithdrawn(clientId);
		ClientStatusChecker.verifyClientPending(status);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testClientUndoWithDrawnWithoutWithdrawal() {
		final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(403).build();
		final ClientHelper validationErrorHelper = new ClientHelper(this.requestSpec, errorResponse);
		// CREATE CLIENT
		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
		final Integer clientId = ClientHelper.createClientPending(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(clientId);

		// GET CLIENT STATUS
		HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec,
				String.valueOf(clientId));

		DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
		Calendar todaysDate = Calendar.getInstance();
		final String undoWithdrawDate = dateFormat.format(todaysDate.getTime());

		ArrayList<HashMap> clientErrorData = (ArrayList<HashMap>) validationErrorHelper.undoWithdrawclient(clientId,
				CommonConstants.RESPONSE_ERROR, undoWithdrawDate);
		assertEquals("error.msg.client.undoWithdrawal.on.nonwithdrawal.account",
				((HashMap<String, Object>) clientErrorData.get(0)).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

		status = this.clientHelper.getClientStatus(requestSpec, responseSpec, String.valueOf(clientId));
		ClientStatusChecker.verifyClientPending(status);

	}

	@Test
	public void testClientUndoWithDrawnWithFutureDate() {

		final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
		final ClientHelper validationErrorHelper = new ClientHelper(this.requestSpec, errorResponse);

		// CREATE CLIENT
		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
		final Integer clientId = ClientHelper.createClientPending(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(clientId);

		// GET CLIENT STATUS
		HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec,
				String.valueOf(clientId));

		ClientStatusChecker.verifyClientPending(status);

		status = this.clientHelper.withdrawClient(clientId);
		ClientStatusChecker.verifyClientWithdrawn(status);
		DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
		Calendar todaysDate = Calendar.getInstance();
		todaysDate.add(Calendar.DATE, 1);
		final String undoWithdrawDate = dateFormat.format(todaysDate.getTime());
		ArrayList<HashMap> clientErrorData = (ArrayList<HashMap>) validationErrorHelper.undoWithdrawclient(clientId,
				CommonConstants.RESPONSE_ERROR, undoWithdrawDate);
		assertEquals("validation.msg.client.reopenedDate.is.greater.than.date",
				((HashMap<String, Object>) clientErrorData.get(0)).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

		status = this.clientHelper.undoWithdrawn(clientId);
		ClientStatusChecker.verifyClientPending(status);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testValidateReopenedDate() {
		final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
		final ClientHelper validationErrorHelper = new ClientHelper(this.requestSpec, errorResponse);

		// CREATE CLIENT
		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
		final Integer clientId = ClientHelper.createClientPending(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(clientId);
		// GET CLIENT STATUS
		HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec,
				String.valueOf(clientId));
		ClientStatusChecker.verifyClientPending(status);

		status = this.clientHelper.withdrawClient(clientId);
		ClientStatusChecker.verifyClientWithdrawn(status);
		status = this.clientHelper.undoWithdrawn(clientId);
		ClientStatusChecker.verifyClientPending(status);
		ArrayList<HashMap> clientErrorData = (ArrayList<HashMap>) validationErrorHelper.activateClient(clientId,
				CommonConstants.RESPONSE_ERROR);
		assertEquals("error.msg.clients.submittedOnDate.after.reopened.date",
				((HashMap<String, Object>) clientErrorData.get(0)).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

	}

	@Test
	public void testReopenedDate() {
		final ResponseSpecification errorResponse = new ResponseSpecBuilder().expectStatusCode(400).build();
		final ClientHelper validationErrorHelper = new ClientHelper(this.requestSpec, errorResponse);

		// CREATE CLIENT
		this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
		final Integer clientId = ClientHelper.createClientPending(this.requestSpec, this.responseSpec);
		Assert.assertNotNull(clientId);
		// GET CLIENT STATUS
		HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec,
				String.valueOf(clientId));
		ClientStatusChecker.verifyClientPending(status);

		status = this.clientHelper.withdrawClient(clientId);
		ClientStatusChecker.verifyClientWithdrawn(status);
		status = this.clientHelper.undoWithdrawn(clientId);
		ClientStatusChecker.verifyClientPending(status);
		status = this.clientHelper.activateClientWithDiffDateOption(clientId, ClientHelper.CREATED_DATE_PLUS_TWO);

	}

	private static String randomIDGenerator(final String prefix, final int lenOfRandomSuffix) {
		return Utils.randomStringGenerator(prefix, lenOfRandomSuffix, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}

}
