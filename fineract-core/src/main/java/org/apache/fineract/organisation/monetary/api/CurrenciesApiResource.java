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
package org.apache.fineract.organisation.monetary.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.command.CurrencyUpdateCommand;
import org.apache.fineract.organisation.monetary.data.CurrencyConfigurationData;
import org.apache.fineract.organisation.monetary.data.CurrencyUpdateRequest;
import org.apache.fineract.organisation.monetary.data.CurrencyUpdateResponse;
import org.apache.fineract.organisation.monetary.service.OrganisationCurrencyReadPlatformService;
import org.springframework.stereotype.Component;

@Path("/v1/currencies")
@Component
@Tag(name = "Currency", description = "Application related configuration around viewing/updating the currencies permitted for use within the MFI.")
@RequiredArgsConstructor
public class CurrenciesApiResource {

    private final OrganisationCurrencyReadPlatformService readPlatformService;
    private final CommandPipeline commandPipeline;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Currency Configuration", description = """
            Returns the list of currencies permitted for use AND the list of currencies not selected (but available for selection).

            Example Requests:

            currencies
            currencies?fields=selectedCurrencyOptions
            """)
    public CurrencyConfigurationData retrieveCurrencies() {
        return readPlatformService.retrieveCurrencyConfiguration();
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Currency Configuration", description = "Updates the list of currencies permitted for use.")
    public CurrencyUpdateResponse updateCurrencies(CurrencyUpdateRequest request) {
        final var command = new CurrencyUpdateCommand();

        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setPayload(request);

        final Supplier<CurrencyUpdateResponse> response = commandPipeline.send(command);

        return response.get();
    }
}
