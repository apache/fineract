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

import com.google.common.base.Splitter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.CommandPipeline;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.command.CurrencyCreateCommand;
import org.apache.fineract.organisation.monetary.command.CurrencyUpdateCommand;
import org.apache.fineract.organisation.monetary.data.CurrencyConfigurationData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.data.CurrencyUpdateRequest;
import org.apache.fineract.organisation.monetary.data.CurrencyUpdateResponse;
import org.apache.fineract.organisation.monetary.exception.InvalidCurrencyException;
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

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a new currency", description = "Adds a new currency to the platform with the given code, name, symbol, decimal places, in multiples of and name code.")
    public CurrencyData createCurrencies(CurrencyData request) {
        final CurrencyCreateCommand command = new CurrencyCreateCommand();
        Set<Integer> allowedDecimalPlaceValues = Set.of(0, 1, 2, 3, 4, 5);

        // Case where the currency code is not an alphabet == 3
        if (!request.getCode().matches("^[A-Z]{3}$")) {
            final String errorMessage = "Currency Code should be non-null, 3 characters long, non-numeric and uppercase.";
            final String errorArgs = request.getCode();
            throw new InvalidCurrencyException("currency", "code", errorMessage, errorArgs);
        }

        // Case where the currency code does not match the currency Name Code
        // e.g. AAA != currency.BBB
        final List<String> nameCodeParts = Splitter.on('.').splitToList(request.getNameCode().trim());
        if (nameCodeParts.size() < 2 || !request.getCode().trim().equals(nameCodeParts.get(1).trim())) {
            final String errorMessage = "Currency Code does not match NameCode currency suffix.";
            final String errorArgsCode = request.getCode();
            final String errorArgsNameCode = request.getNameCode();
            throw new InvalidCurrencyException("code", "name.code", errorMessage, errorArgsCode, errorArgsNameCode);
        }

        // Check if the decimal places are within 0,1,2,3
        if (!allowedDecimalPlaceValues.contains(request.getDecimalPlaces())) {
            final String errorMessage = "Decimal Places allowed are inclusive of values in the range 0 - 5.";
            final String errorArgs = String.valueOf(request.getDecimalPlaces());
            throw new InvalidCurrencyException("decimal", "places", errorMessage, errorArgs);
        }

        // Check if inMultiplesOf is between 0 and 1
        if (request.getInMultiplesOf() != null && !(request.getInMultiplesOf() >= 0 && request.getInMultiplesOf() <= 1000)) {
            final String errorMessage = "In Multiples of (sub-units) allowed are inclusive of values in the range 0 - 1000.";
            final String errorArgs = String.valueOf(request.getInMultiplesOf());
            throw new InvalidCurrencyException("sub", "unit", errorMessage, errorArgs);
        }

        // Check if this is a duplicate request. Query Database
        // if present throw exception.
        Boolean isExistingCurrency = readPlatformService.checkExistingCurrencyCode(request.getCode());

        if (isExistingCurrency) {
            final String errorMessage = "Duplicate Request. Request cannot be accepted as the currency is already present in the system.";
            final String errorArgs = String.valueOf(request.getCode());
            throw new InvalidCurrencyException("existing", "currency.code", errorMessage, errorArgs);
        }

        command.setId(UUID.randomUUID());
        command.setCreatedAt(DateUtils.getAuditOffsetDateTime());
        command.setPayload(request);
        final Supplier<CurrencyData> response = commandPipeline.send(command);

        return response.get();
    }
}
