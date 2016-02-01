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
package org.apache.fineract.portfolio.self.savings.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.springframework.stereotype.Component;

@Component
public class SelfSavingsDataValidator {

	private static final Set<String> allowedAssociationParameters = new HashSet<>(
			Arrays.asList(SavingsApiConstants.transactions,
					SavingsApiConstants.charges));

	public void validateRetrieveSavings(final UriInfo uriInfo) {
		List<String> unsupportedParams = new ArrayList<>();

		validateTemplate(uriInfo, unsupportedParams);

		Set<String> associationParameters = ApiParameterHelper
				.extractAssociationsForResponseIfProvided(uriInfo
						.getQueryParameters());
		if (!associationParameters.isEmpty()) {
			associationParameters.removeAll(allowedAssociationParameters);
			if (!associationParameters.isEmpty()) {
				unsupportedParams.addAll(associationParameters);
			}
		}

		if (uriInfo.getQueryParameters().getFirst("exclude") != null) {
			unsupportedParams.add("exclude");
		}

		throwExceptionIfReqd(unsupportedParams);
	}

	public void validateRetrieveSavingsTransaction(final UriInfo uriInfo) {
		List<String> unsupportedParams = new ArrayList<>();

		validateTemplate(uriInfo, unsupportedParams);

		throwExceptionIfReqd(unsupportedParams);
	}

	private void throwExceptionIfReqd(final List<String> unsupportedParams) {
		if (unsupportedParams.size() > 0) {
			throw new UnsupportedParameterException(unsupportedParams);
		}
	}

	private void validateTemplate(final UriInfo uriInfo,
			List<String> unsupportedParams) {
		final boolean templateRequest = ApiParameterHelper.template(uriInfo
				.getQueryParameters());
		if (templateRequest) {
			unsupportedParams.add("template");
		}
	}

}
