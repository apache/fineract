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
package org.apache.fineract.infrastructure.crypt.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.crypt.data.PublicKeyData;
import org.apache.fineract.infrastructure.crypt.service.CryptographyReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author manoj
 */
@Path("crypt")
@Component
@Scope("singleton")
@Api(tags = {"Cryptography"})
@SwaggerDefinition(tags = {
        @Tag(name = "Cryptography", description = "Provides RSA public keys for various modules(types)")
})
public class CryptographyApiResource {

    private final ToApiJsonSerializer<PublicKeyData> toApiJsonSerializer;
    private final CryptographyReadPlatformService cryptographyReadPlatformService;

    @Autowired
    public CryptographyApiResource(ToApiJsonSerializer<PublicKeyData> toApiJsonSerializer,
                                   CryptographyReadPlatformService cryptographyReadPlatformService) {
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.cryptographyReadPlatformService = cryptographyReadPlatformService;
    }

    @GET
    @Path("publickey/{type}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Provides RSA public key for {type}", notes = "Key is provided with a version ")
    @ApiResponses({@ApiResponse(code = 200, message = "RSA public key")})
    public String getRsaPublicKey(@PathParam("type") @ApiParam(value = "type") final String type) {
        return this.toApiJsonSerializer.serialize(this.cryptographyReadPlatformService.getPublicRsaKey(type));
    }

}
