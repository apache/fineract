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
package org.apache.fineract.infrastructure.codes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.CodeConstants;
import org.apache.fineract.infrastructure.codes.data.CountryData;
import org.apache.fineract.infrastructure.codes.service.CountryService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/country")
@Component
@Scope("singleton")
@Tag(name = "Country", description = "Country Code and Country Values: Country Code represent a specific Country and translated specific to Language/Locale")
@RequiredArgsConstructor
public class CountryCodeApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("countryCode", "countryName"));
    private final String resourceNameForPermissions = "Country";
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<CountryData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final CountryService countryService;

    @GET
    @Path("{locale}/{countryCode}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Country Name by Country Code and Locale. ", description = "Returns the Country Name.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "fetches the countryList specific to language") })
    public String retrieveCountry(@PathParam("locale") @Parameter(description = "locale") String locale,
            @PathParam("countryCode") @Parameter(description = "countryCode") final String countryCode) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        if (!locale.equals(CodeConstants.FRENCH_LOCALE)) { // Set English as default (Requested locale translation
                                                           // unavailable).
            locale = CodeConstants.ENGLISH_LOCALE;
        }

        final Map<Locale, Map<String, String>> countryList = this.countryService.getCountryMapping();
        Locale language = new Locale(locale);
        Map<String, String> country = countryList.get(language);
        String countryName = country.get(countryCode);
        CountryData countryData = CountryData.instance(countryCode, countryName);

        return this.toApiJsonSerializer.serialize(countryData);
    }

    @GET
    @Path("{locale}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Country List", description = "Returns the list of Country List.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "fetches the countryList specific to language") })
    public String retrieveCountryList(@Context final UriInfo uriInfo,
            @PathParam("locale") @Parameter(description = "locale") String locale) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        if (!locale.equals(CodeConstants.FRENCH_LOCALE)) { // Set English as default (Requested locale translation
                                                           // unavailable).
            locale = CodeConstants.ENGLISH_LOCALE;
        }

        final Map<Locale, Collection<CountryData>> countryList = this.countryService.getCountriesList();

        Locale language = new Locale(locale);
        Collection<CountryData> countryData = countryList.get(language);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, countryData, RESPONSE_DATA_PARAMETERS);
    }

}
