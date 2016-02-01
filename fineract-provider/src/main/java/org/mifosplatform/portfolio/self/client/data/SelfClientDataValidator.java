/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.self.client.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.exception.UnsupportedParameterException;
import org.springframework.stereotype.Component;

@Component
public class SelfClientDataValidator {

	private static final Set<String> allowedChargesAssociationParameters = new HashSet<>(
			Arrays.asList("transactions"));

	public void validateClientCharges(final UriInfo uriInfo) {
		List<String> unsupportedParams = new ArrayList<>();

		Set<String> associationParameters = ApiParameterHelper
				.extractAssociationsForResponseIfProvided(uriInfo
						.getQueryParameters());
		if (!associationParameters.isEmpty()) {
			associationParameters
					.removeAll(allowedChargesAssociationParameters);
			if (!associationParameters.isEmpty()) {
				unsupportedParams.addAll(associationParameters);
			}
		}

		if (uriInfo.getQueryParameters().getFirst("exclude") != null) {
			unsupportedParams.add("exclude");
		}

		if (unsupportedParams.size() > 0) {
			throw new UnsupportedParameterException(unsupportedParams);
		}
	}

	public void validateRetrieveOne(final UriInfo uriInfo) {
		List<String> unsupportedParams = new ArrayList<>();

		final boolean templateRequest = ApiParameterHelper.template(uriInfo
				.getQueryParameters());
		if (templateRequest) {
			unsupportedParams.add("template");
		}

		if (unsupportedParams.size() > 0) {
			throw new UnsupportedParameterException(unsupportedParams);
		}

	}

}
