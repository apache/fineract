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
package org.apache.fineract.portfolio.self.loanaccount.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public class SelfLoansDataValidator {
	private static final Set<String> allowedAssociationParameters = new HashSet<>(
			Arrays.asList("repaymentSchedule", "futureSchedule",
					"originalSchedule", "transactions", "charges",
					"guarantors", "collateral", "linkedAccount",
					"multiDisburseDetails"));
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public SelfLoansDataValidator(final FromJsonHelper fromApiJsonHelper){
		this.fromApiJsonHelper = fromApiJsonHelper;
	}
	
	public void validateRetrieveLoan(final UriInfo uriInfo) {
		List<String> unsupportedParams = new ArrayList<>();

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

	public void validateRetrieveTransaction(UriInfo uriInfo) {
		List<String> unsupportedParams = new ArrayList<>();

		validateTemplate(uriInfo, unsupportedParams);

		throwExceptionIfReqd(unsupportedParams);

	}
	
	public HashMap<String, Object> validateLoanApplication(final String json){
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String loanTypeParameterName = "loanType";
        final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(loanTypeParameterName, element);
        baseDataValidator.reset().parameter(loanTypeParameterName).value(loanTypeStr).notNull().equals("individual");

        final String clientIdParameterName = "clientId";
        final String clientId = this.fromApiJsonHelper.extractStringNamed(clientIdParameterName, element);
        baseDataValidator.reset().parameter(clientIdParameterName).value(clientId).notNull().longGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        
        HashMap<String, Object> retAttr = new HashMap<>();
        retAttr.put("clientId", Long.parseLong(clientId));
        
        return retAttr;

	}

	public HashMap<String, Object> validateModifyLoanApplication(final String json) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String loanTypeParameterName = "loanType";
        if(this.fromApiJsonHelper.parameterExists(loanTypeParameterName, element)){
            final String loanTypeStr = this.fromApiJsonHelper.extractStringNamed(loanTypeParameterName, element);
            baseDataValidator.reset().parameter(loanTypeParameterName).value(loanTypeStr).notNull().equals("individual");
        }

        final String clientIdParameterName = "clientId";
        String clientId = null;
        if(this.fromApiJsonHelper.parameterExists(clientIdParameterName, element)){
            clientId = this.fromApiJsonHelper.extractStringNamed(clientIdParameterName, element);
            baseDataValidator.reset().parameter(clientIdParameterName).value(clientId).notNull().longGreaterThanZero();
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        
        HashMap<String, Object> retAttr = new HashMap<>();
        if(clientId != null){
            retAttr.put("clientId", Long.parseLong(clientId));
        }
        
        return retAttr;
	}

	private void throwExceptionIfReqd(List<String> unsupportedParams) {
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
