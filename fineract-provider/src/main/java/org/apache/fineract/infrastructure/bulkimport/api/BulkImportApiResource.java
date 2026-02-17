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
package org.apache.fineract.infrastructure.bulkimport.api;

import static org.apache.fineract.util.StreamResponseUtil.DISPOSITION_TYPE_ATTACHMENT;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.data.ImportData;
import org.apache.fineract.infrastructure.bulkimport.exceptions.ImportTypeNotFoundException;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.apache.fineract.util.StreamResponseUtil;
import org.springframework.stereotype.Component;

@Path("/v1/imports")
@Component
@Tag(name = "Bulk Import", description = "")
@RequiredArgsConstructor
public class BulkImportApiResource {

    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final DocumentReadPlatformService documentReadPlatformService;
    private final DefaultToApiJsonSerializer<ImportData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveImportDocuments(@Context final UriInfo uriInfo, @QueryParam("entityType") final String entityType) {
        Collection<ImportData> importData = new ArrayList<>();

        if (entityType.equals(GlobalEntityType.CLIENT.getCode())) {
            final var importForClientEntity = this.bulkImportWorkbookService.getImports(GlobalEntityType.CLIENTS_ENTITY);
            final var importForClientPerson = this.bulkImportWorkbookService.getImports(GlobalEntityType.CLIENTS_PERSON);

            if (importForClientEntity != null) {
                importData.addAll(importForClientEntity);
            }

            if (importForClientPerson != null) {
                importData.addAll(importForClientPerson);
            }
        } else {
            final GlobalEntityType type = GlobalEntityType.fromCode(entityType);

            if (type == null) {
                throw new ImportTypeNotFoundException(entityType);
            }

            importData = this.bulkImportWorkbookService.getImports(type);
        }
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, importData);
    }

    @GET
    @Path("getOutputTemplateLocation")
    public String retriveOutputTemplateLocation(@QueryParam("importDocumentId") final Long importDocumentId) {
        final var imporData = bulkImportWorkbookService.getImport(importDocumentId);

        return Optional.ofNullable(imporData)
                .flatMap(importData -> Optional.ofNullable(documentReadPlatformService.retrieveDocument(imporData.getDocumentId())))
                .map(DocumentData::getLocation).orElse(null);
    }

    @GET
    @Path("downloadOutputTemplate")
    @Produces("application/vnd.ms-excel")
    public Response getOutputTemplate(@QueryParam("importDocumentId") final Long importDocumentId) {
        // TODO: can we avoid 2 calls by introducing a new function? clean code more important now
        final var docs = documentReadPlatformService.retrieveAllDocuments("IMPORT", importDocumentId);

        final var docData = docs.stream().findFirst();

        final var d = docData.orElseThrow(() -> new DocumentNotFoundException("IMPORT", importDocumentId, -1L));
        final var content = documentReadPlatformService.retrieveDocumentContent(d.getParentEntityType(), d.getParentEntityId(), d.getId());
        final var streamResponseData = StreamResponseUtil.StreamResponseData.builder().type(content.getContentType())
                .fileName(content.getFileName()).stream(content.getStream()).dispositionType(DISPOSITION_TYPE_ATTACHMENT).build();

        return StreamResponseUtil.ok(streamResponseData);
    }
}
