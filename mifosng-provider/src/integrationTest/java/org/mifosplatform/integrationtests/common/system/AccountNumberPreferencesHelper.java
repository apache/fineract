/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.system;

import java.util.ArrayList;
import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class AccountNumberPreferencesHelper {

	private final RequestSpecification requestSpec;

	private final ResponseSpecification responseSpec;

	private static final String ACCOUNT_NUMBER_FORMATS_REQUEST_URL = "/mifosng-provider/api/v1/accountnumberformats";

	public AccountNumberPreferencesHelper(
			final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec) {
		this.requestSpec = requestSpec;
		this.responseSpec = responseSpec;
	}

	public Object createClientAccountNumberPreference(
			ResponseSpecification responseSpec, String jsonAttributeToGetBack) {
		System.out
				.println("---------------------------------CREATING CLIENT ACCOUNT NUMBER PREFERENCE------------------------------------------");

		final String requestJSON = new AccountNumberPreferencesTestBuilder()
				.clientBuild();

		final String URL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "?"
				+ Utils.TENANT_IDENTIFIER;

		return Utils.performServerPost(this.requestSpec, responseSpec, URL,
				requestJSON, jsonAttributeToGetBack);
	}

	public Object createLoanAccountNumberPreference(
			ResponseSpecification responseSpec, String jsonAttributeToGetBack) {
		System.out
				.println("---------------------------------CREATING LOAN ACCOUNT NUMBER PREFERENCE------------------------------------------");

		final String requestJSON = new AccountNumberPreferencesTestBuilder()
				.loanBuild();

		final String URL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "?"
				+ Utils.TENANT_IDENTIFIER;
		return Utils.performServerPost(this.requestSpec, responseSpec, URL,
				requestJSON, jsonAttributeToGetBack);
	}

	public Object createSavingsAccountNumberPreference(
			ResponseSpecification responseSpec, String jsonAttributeToGetBack) {
		System.out
				.println("---------------------------------CREATING SAVINGS ACCOUNT NUMBER PREFERENCE------------------------------------------");

		final String requestJSON = new AccountNumberPreferencesTestBuilder()
				.savingsBuild();

		final String URL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "?"
				+ Utils.TENANT_IDENTIFIER;
		return Utils.performServerPost(this.requestSpec, responseSpec, URL,
				requestJSON, jsonAttributeToGetBack);

	}

	public HashMap<String, Object> createAccountNumberPreferenceWithInvalidData(
			ResponseSpecification responseSpec, String accountType,
			String prefixType, String jsonAttributeToGetBack) {

		final String requestJSON = new AccountNumberPreferencesTestBuilder()
				.invalidDataBuild(accountType, prefixType);

		final String URL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "?"
				+ Utils.TENANT_IDENTIFIER;
		return Utils.performServerPost(this.requestSpec, responseSpec, URL,
				requestJSON, jsonAttributeToGetBack);

	}

	public HashMap<String, Object> updateAccountNumberPreference(
			final Integer accountNumberFormatId, final String prefixType,
			ResponseSpecification responseSpec, String jsonAttributeToGetBack) {

		final String requestJSON = new AccountNumberPreferencesTestBuilder()
				.updatePrefixType(prefixType);

		final String URL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "/"
				+ accountNumberFormatId + "?" + Utils.TENANT_IDENTIFIER;

		return Utils.performServerPut(this.requestSpec, responseSpec, URL,
				requestJSON, jsonAttributeToGetBack);

	}

	public HashMap<String, Object> deleteAccountNumberPreference(
			final Integer accountNumberFormatId,
			ResponseSpecification responseSpec, String jsonAttributeToGetBack) {

		System.out
				.println("---------------------------------DELETING ACCOUNT NUMBER PREFERENCE------------------------------------------");

		final String URL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "/"
				+ accountNumberFormatId + "?" + Utils.TENANT_IDENTIFIER;

		return Utils.performServerDelete(this.requestSpec, responseSpec, URL,
				jsonAttributeToGetBack);
	}

	public Object getAccountNumberPreference(
			final Integer accountNumberFormatId,
			final String jsonAttributeToGetBack) {
		final String URL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "/"
				+ accountNumberFormatId + "?" + Utils.TENANT_IDENTIFIER;

		return Utils.performServerGet(requestSpec, responseSpec, URL,
				jsonAttributeToGetBack);
	}

	public ArrayList<HashMap<String, Object>> getAllAccountNumberPreferences() {
		final String URL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "?"
				+ Utils.TENANT_IDENTIFIER;
		final ArrayList<HashMap<String, Object>> response = Utils
				.performServerGet(requestSpec, responseSpec, URL, "");
		return response;
	}

	public void verifyCreationOfAccountNumberPreferences(
			final Integer clientAccountNumberPreferenceId,
			final Integer loanAccountNumberPreferenceId,
			final Integer savingsAccountNumberPreferenceId,
			ResponseSpecification responseSpec, RequestSpecification requestSpec) {

		final String clientURL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "/"
				+ clientAccountNumberPreferenceId + "?"
				+ Utils.TENANT_IDENTIFIER;

		Utils.performServerGet(requestSpec, responseSpec, clientURL, "id");

		final String loanURL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "/"
				+ loanAccountNumberPreferenceId + "?" + Utils.TENANT_IDENTIFIER;

		Utils.performServerGet(requestSpec, responseSpec, loanURL, "id");

		final String savingsURL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "/"
				+ savingsAccountNumberPreferenceId + "?"
				+ Utils.TENANT_IDENTIFIER;

		Utils.performServerGet(requestSpec, responseSpec, savingsURL, "id");
	}

	public void verifyUpdationOfAccountNumberPreferences(
			final Integer accountNumberPreferenceId,
			ResponseSpecification responseSpec, RequestSpecification requestSpec) {

		final String URL = ACCOUNT_NUMBER_FORMATS_REQUEST_URL + "/"
				+ accountNumberPreferenceId + "?" + Utils.TENANT_IDENTIFIER;
		Utils.performServerGet(requestSpec, responseSpec, URL, "id");

	}
}
