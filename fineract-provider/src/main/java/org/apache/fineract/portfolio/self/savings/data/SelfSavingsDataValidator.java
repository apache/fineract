/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.self.savings.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.mifosplatform.portfolio.savings.SavingsApiConstants;
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
