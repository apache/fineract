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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.api.AccountTransfersApiResource;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.self.account.data.SelfAccountTemplateData;
import org.apache.fineract.portfolio.self.account.data.SelfAccountTransferData;
import org.apache.fineract.portfolio.self.account.data.SelfAccountTransferDataValidator;
import org.apache.fineract.portfolio.self.account.exception.BeneficiaryTransferLimitExceededException;
import org.apache.fineract.portfolio.self.account.exception.DailyTPTTransactionAmountLimitExceededException;
import org.apache.fineract.portfolio.self.account.service.SelfAccountTransferReadService;
import org.apache.fineract.portfolio.self.account.service.SelfBeneficiariesTPTReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Component;

@Path("/v1/self/accounttransfers")
@Component
@Tag(name = "Self Account transfer", description = "")
@RequiredArgsConstructor
public class SelfAccountTransferApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SelfAccountTransferData> toApiJsonSerializer;
    private final AccountTransfersApiResource accountTransfersApiResource;
    private final SelfAccountTransferReadService selfAccountTransferReadService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SelfAccountTransferDataValidator dataValidator;
    private final SelfBeneficiariesTPTReadPlatformService tptBeneficiaryReadPlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final AccountTransfersReadPlatformService accountTransfersReadPlatformService;

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Account Transfer Template", description = "Returns list of loan/savings accounts that can be used for account transfer\n"
            + "\n" + "\n" + "Example Requests:\n" + "\n" + "self/accounttransfers/template\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SelfAccountTransferApiResourceSwagger.GetAccountTransferTemplateResponse.class)))) })
    public String template(@DefaultValue("") @QueryParam("type") @Parameter(name = "type") final String type,
            @Context final UriInfo uriInfo) {

        AppUser user = this.context.authenticatedUser();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        Collection<SelfAccountTemplateData> selfTemplateData = this.selfAccountTransferReadService.retrieveSelfAccountTemplateData(user);

        if (type.equals("tpt")) {
            Collection<SelfAccountTemplateData> tptTemplateData = this.tptBeneficiaryReadPlatformService
                    .retrieveTPTSelfAccountTemplateData(user);
            return this.toApiJsonSerializer.serialize(settings, new SelfAccountTransferData(selfTemplateData, tptTemplateData));
        }

        return this.toApiJsonSerializer.serialize(settings, new SelfAccountTransferData(selfTemplateData, selfTemplateData));
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create new Transfer", description = "Ability to create new transfer of monetary funds from one account to another.\n"
            + "\n" + "\n" + "Example Requests:\n" + "\n" + " self/accounttransfers/\n")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SelfAccountTransferApiResourceSwagger.PostNewTransferResponse.class)))) })
    public String create(@DefaultValue("") @QueryParam("type") @Parameter(name = "type") final String type,
            final String apiRequestBodyAsJson) {
        Map<String, Object> params = this.dataValidator.validateCreate(type, apiRequestBodyAsJson);
        if (type.equals("tpt")) {
            checkForLimits(params);
        }
        return this.accountTransfersApiResource.create(apiRequestBodyAsJson);
    }

    private void checkForLimits(Map<String, Object> params) {
        SelfAccountTemplateData fromAccount = (SelfAccountTemplateData) params.get("fromAccount");
        SelfAccountTemplateData toAccount = (SelfAccountTemplateData) params.get("toAccount");
        LocalDate transactionDate = (LocalDate) params.get("transactionDate");
        BigDecimal transactionAmount = (BigDecimal) params.get("transactionAmount");

        AppUser user = this.context.authenticatedUser();
        Long transferLimit = this.tptBeneficiaryReadPlatformService.getTransferLimit(user.getId(), toAccount.getAccountId(),
                toAccount.getAccountType());
        if (transferLimit != null && transferLimit > 0) {
            if (transactionAmount.compareTo(new BigDecimal(transferLimit)) > 0) {
                throw new BeneficiaryTransferLimitExceededException();
            }
        }

        if (this.configurationDomainService.isDailyTPTLimitEnabled()) {
            Long dailyTPTLimit = this.configurationDomainService.getDailyTPTLimit();
            if (dailyTPTLimit != null && dailyTPTLimit > 0) {
                BigDecimal dailyTPTLimitBD = new BigDecimal(dailyTPTLimit);
                BigDecimal totTransactionAmount = this.accountTransfersReadPlatformService
                        .getTotalTransactionAmount(fromAccount.getAccountId(), fromAccount.getAccountType(), transactionDate);
                if (totTransactionAmount != null && totTransactionAmount.compareTo(BigDecimal.ZERO) > 0) {
                    if (dailyTPTLimitBD.compareTo(totTransactionAmount) <= 0
                            || dailyTPTLimitBD.compareTo(totTransactionAmount.add(transactionAmount)) < 0) {
                        throw new DailyTPTTransactionAmountLimitExceededException(fromAccount.getAccountId(), fromAccount.getAccountType());
                    }
                }
            }
        }
    }

}
