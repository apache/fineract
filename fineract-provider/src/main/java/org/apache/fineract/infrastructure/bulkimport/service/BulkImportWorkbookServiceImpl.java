/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE multipartFile
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this multipartFile
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this multipartFile except in compliance
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
package org.apache.fineract.infrastructure.bulkimport.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.ImportFormatType;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.client.ClientEntityImportHandler;
import org.apache.fineract.infrastructure.bulkimport.importhandler.client.ClientPersonImportHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;
import com.sun.jersey.core.header.FormDataContentDisposition;

@Service
public class BulkImportWorkbookServiceImpl implements BulkImportWorkbookService {
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public BulkImportWorkbookServiceImpl(final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @Override
    public Response importWorkbook(String entityType , InputStream inputStream, FormDataContentDisposition fileDetail) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, baos);
            final byte[] bytes = baos.toByteArray();
            InputStream clonedInputStream = new ByteArrayInputStream(bytes);
            InputStream clonedInputStreamWorkbook =new ByteArrayInputStream(bytes);
            final Tika tika = new Tika();
            final TikaInputStream tikaInputStream = TikaInputStream.get(clonedInputStream);
            final String fileType = tika.detect(tikaInputStream);
            final String fileExtension = Files.getFileExtension(fileDetail.getFileName()).toLowerCase();
            ImportFormatType format = ImportFormatType.of(fileExtension);
            if (format.name().equalsIgnoreCase(fileType)) {
                throw new GeneralPlatformDomainRuleException("error.msg.invalid.file.extension",
                        "Uploaded file extension is not recognized.");
            }

            Workbook workbook = new HSSFWorkbook(clonedInputStreamWorkbook);
            ImportHandler importHandler=null;
            if (entityType.trim().equalsIgnoreCase(ClientApiConstants.CLIENT_PERSON_RESOURCE_NAME)) {
                importHandler = new ClientPersonImportHandler(workbook);
            }else if(entityType.trim().equalsIgnoreCase(ClientApiConstants.CLIENT_ENTITY_RESOURCE_NAME)){
                importHandler =new ClientEntityImportHandler(workbook);
            } else {
                throw new GeneralPlatformDomainRuleException("error.msg.unable.to.find.resource",
                        "Unable to find requested resource");
            }
            importHandler.readExcelFile();
            importHandler.Upload(commandsSourceWritePlatformService);
        }catch (IOException ex){
            throw new GeneralPlatformDomainRuleException("error.msg.io.exception","IO exception occured with "+fileDetail.getFileName()+" "+ex.getMessage());
        }
        return Response.ok(fileDetail.getFileName()+" uploaded successfully").build();
    }

}