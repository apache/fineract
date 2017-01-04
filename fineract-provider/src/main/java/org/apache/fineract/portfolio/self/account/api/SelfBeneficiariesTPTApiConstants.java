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
package org.apache.fineract.portfolio.self.account.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface SelfBeneficiariesTPTApiConstants {

	public static final String BENEFICIARY_ENTITY_NAME = "SSBENEFICIARYTPT";
	public static final String RESOURCE_NAME = "beneficiary";
	public static final String LOCALE = "locale";
	public static final String NAME_PARAM_NAME = "name";
	public static final String OFFICE_NAME_PARAM_NAME = "officeName";
	public static final String ACCOUNT_TYPE_PARAM_NAME = "accountType";
	public static final String ACCOUNT_NUMBER_PARAM_NAME = "accountNumber";
	public static final String TRANSFER_LIMIT_PARAM_NAME = "transferLimit";

	public static final String ID_PARAM_NAME = "id";
	public static final String CLIENT_NAME_PARAM_NAME = "clientName";
	public static final String ACCOUNT_TYPE_OPTIONS_PARAM_NAME = "accountTypeOptions";

	public static final Set<String> CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(LOCALE, NAME_PARAM_NAME, OFFICE_NAME_PARAM_NAME,
					ACCOUNT_NUMBER_PARAM_NAME, ACCOUNT_TYPE_PARAM_NAME,
					TRANSFER_LIMIT_PARAM_NAME));

	public static final Set<String> UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(NAME_PARAM_NAME, TRANSFER_LIMIT_PARAM_NAME));

	public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(NAME_PARAM_NAME, OFFICE_NAME_PARAM_NAME,
					ACCOUNT_NUMBER_PARAM_NAME, ACCOUNT_TYPE_PARAM_NAME,
					TRANSFER_LIMIT_PARAM_NAME, ID_PARAM_NAME,
					CLIENT_NAME_PARAM_NAME, ACCOUNT_TYPE_OPTIONS_PARAM_NAME));
}
