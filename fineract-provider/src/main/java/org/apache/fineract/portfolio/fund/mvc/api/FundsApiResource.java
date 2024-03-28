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
package org.apache.fineract.portfolio.fund.mvc.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.mvc.domain.CommandTypeWrapper;
import org.apache.fineract.commands.mvc.service.CommandTypeWrapperBuilder;
import org.apache.fineract.commands.mvc.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.core.api.mvc.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.mvc.ProfileMvc;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.mvc.data.FundResponse;
import org.apache.fineract.portfolio.fund.mvc.data.PostFundsRequest;
import org.apache.fineract.portfolio.fund.mvc.data.PutFundsRequest;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ProfileMvc
@RestController("mvcFundsApiResource")
@RequestMapping("/v1/funds")
@Tag(name = "Funds")
@RequiredArgsConstructor
public class FundsApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "FUND";

    private final PlatformSecurityContext context;
    private final FundReadPlatformService readPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ObjectMapper mapper;

    @GetMapping
    @Operation(summary = "Retrieve Funds", description = "Returns the list of funds.\n" + "\n" + "Example Requests:\n" + "\n" + "funds")
    public List<FundResponse> retrieveFunds() {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final Collection<FundData> funds = readPlatformService.retrieveAllFunds();
        apiRequestParameterHelper.process();
        return funds.stream().map(fundData -> new FundResponse(fundData.getId(), fundData.getName(), fundData.getExternalId())).toList();
    }

    @PostMapping
    @Operation(summary = "Create a Fund", description = "Creates a Fund")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FundsApiResourceSwagger.PostFundsResponse.class))) })
    public CommandProcessingResult createFund(@Valid @RequestBody final PostFundsRequest apiRequestBody) {
        final String withJson = getJsonRequest(apiRequestBody);

        final CommandWrapper commandJsonRequest = new CommandWrapperBuilder().createFund().withJson(withJson).build();

        final CommandTypeWrapper<PostFundsRequest> commandRequest = new CommandTypeWrapperBuilder<PostFundsRequest>() //
                .createFund() //
                .withRequest(apiRequestBody) //
                .withJsonCommand(commandJsonRequest) //
                .build(); //

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    @GetMapping("/{fundId}")
    @Operation(summary = "Retrieve a Fund", description = "Returns the details of a Fund.\n" + "\n" + "Example Requests:\n" + "\n"
            + "funds/1")
    public FundResponse retrieveFund(@PathVariable @Parameter(description = "fundId") final Long fundId) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final FundData fund = readPlatformService.retrieveFund(fundId);
        apiRequestParameterHelper.process();
        return new FundResponse(fund.getId(), fund.getName(), fund.getExternalId());
    }

    @PutMapping("/{fundId}")
    @Operation(summary = "Update a Fund", description = "Updates the details of a fund.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = FundsApiResourceSwagger.PutFundsFundIdResponse.class))) })
    public CommandProcessingResult updateFund(@PathVariable @Parameter(description = "fundId") final Long fundId,
            @Valid @RequestBody final PutFundsRequest apiRequestBody) {
        final String withJson = getJsonRequest(apiRequestBody);

        final CommandWrapper commandJsonRequest = new CommandWrapperBuilder().updateFund(fundId).withJson(withJson).build();

        final CommandTypeWrapper<PutFundsRequest> commandRequest = new CommandTypeWrapperBuilder<PutFundsRequest>() //
                .updateFund(fundId) //
                .withRequest(apiRequestBody) //
                .withJsonCommand(commandJsonRequest) //
                .build(); //

        return commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    // TODO: Remove after MVC migration
    private String getJsonRequest(Object apiRequestBody) {
        final String withJson;
        try {
            withJson = mapper.writeValueAsString(apiRequestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return withJson;
    }
}
