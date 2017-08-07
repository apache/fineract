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
package org.apache.fineract.infrastructure.bulkimport.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.bulkimport.data.BulkImportEvent;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.data.ImportData;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocument;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.apache.fineract.infrastructure.bulkimport.importhandler.ImportHandlerUtils;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.domain.Document;
import org.apache.fineract.infrastructure.documentmanagement.domain.DocumentRepository;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformService;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl.DOCUMENT_MANAGEMENT_ENTITY;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.sun.jersey.core.header.FormDataContentDisposition;

@Service
public class BulkImportWorkbookServiceImpl implements BulkImportWorkbookService {

    private final ApplicationContext applicationContext;
    private final PlatformSecurityContext securityContext;
    private final DocumentWritePlatformService documentWritePlatformService;
    private final DocumentRepository documentRepository;
    private final ImportDocumentRepository importDocumentRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BulkImportWorkbookServiceImpl(final ApplicationContext applicationContext,
    		final PlatformSecurityContext securityContext,
    		final DocumentWritePlatformService documentWritePlatformService,
    		final DocumentRepository documentRepository,
    		final ImportDocumentRepository importDocumentRepository,
    		final RoutingDataSource dataSource) {
        this.applicationContext = applicationContext;
        this.securityContext = securityContext;
        this.documentWritePlatformService = documentWritePlatformService;
        this.documentRepository = documentRepository;
        this.importDocumentRepository = importDocumentRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Long importWorkbook(final String entity, final InputStream inputStream,
    		final FormDataContentDisposition fileDetail, final String locale, final String dateFormat) {
        if (entity != null && inputStream != null && fileDetail != null&&locale!=null&&dateFormat!=null) {
            try {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(inputStream, baos);
                final byte[] bytes = baos.toByteArray();
                InputStream clonedInputStream = new ByteArrayInputStream(bytes);
                InputStream clonedInputStreamWorkbook = new ByteArrayInputStream(bytes);
                final Tika tika = new Tika();
                final TikaInputStream tikaInputStream = TikaInputStream.get(clonedInputStream);
                final String fileType = tika.detect(tikaInputStream);
                if (!fileType.contains("msoffice")) {
                    throw new GeneralPlatformDomainRuleException("error.msg.invalid.file.extension",
                            "Uploaded file extension is not recognized.");
                }

                Workbook workbook = new HSSFWorkbook(clonedInputStreamWorkbook);
                final GlobalEntityType entityType;
                int primaryColumn;
                if (entity.trim().equalsIgnoreCase(GlobalEntityType.OFFICES.toString())) {
                		entityType = GlobalEntityType.OFFICES;
                		primaryColumn = 0;
                } else {
                    throw new GeneralPlatformDomainRuleException("error.msg.unable.to.find.resource",
                            "Unable to find requested resource");
                }
                return publishEvent(primaryColumn, fileDetail, clonedInputStreamWorkbook, entityType,
                		workbook, locale, dateFormat);


            } catch (IOException ex) {
                throw new GeneralPlatformDomainRuleException("error.msg.io.exception", "IO exception occured with "
                		+ fileDetail.getFileName() + " " + ex.getMessage());
            }
        } else {
            throw new GeneralPlatformDomainRuleException("error.msg.entityType.null",
                    "One or more of the given parameters null or not found");
        }
    }

    private Long publishEvent(final Integer primaryColumn,
    		final FormDataContentDisposition fileDetail, final InputStream clonedInputStreamWorkbook,
    		final GlobalEntityType entityType, final Workbook workbook,
    		final String locale, final String dateFormat) {

    		final String fileName = fileDetail.getFileName();

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        //create a document containing excel data
        final Long documentId = this.documentWritePlatformService.createInternalDocument(
        		DOCUMENT_MANAGEMENT_ENTITY.IMPORT.name(),
        		this.securityContext.authenticatedUser().getId(), null, clonedInputStreamWorkbook,
        		URLConnection.guessContentTypeFromName(fileName), fileName, null, fileName);

        //Gets the newly created Document instance
        final Document document = this.documentRepository.findOne(documentId);

        //Create Table m_import_document and push default data.
        final ImportDocument importDocument = ImportDocument.instance(document,
        		DateUtils.getLocalDateTimeOfTenant(), entityType.getValue(),
        		this.securityContext.authenticatedUser(),
        		ImportHandlerUtils.getNumberOfRows(workbook.getSheetAt(0),
        				primaryColumn));
        this.importDocumentRepository.saveAndFlush(importDocument);

        // Upon instance creation  push excel data,and updates endtime,error count and success count to m_import_document table
        BulkImportEvent event = BulkImportEvent.instance(ThreadLocalContextUtil.getTenant()
        		.getTenantIdentifier(), workbook, importDocument.getId(), locale, dateFormat);
        applicationContext.publishEvent(event);
        return importDocument.getId();
    }

    public Collection<ImportData> getImports(GlobalEntityType type) {
    		this.securityContext.authenticatedUser();

        final ImportMapper rm = new ImportMapper();
        final String sql = "select " + rm.schema() + " order by i.id desc";

        return this.jdbcTemplate.query(sql, rm, new Object[] {type.getValue()});
    }


    private static final class ImportMapper implements RowMapper<ImportData> {

        public String schema() {
        		final StringBuilder sql = new StringBuilder();
        		sql.append("i.id as id, i.document_id as documentId, d.name as name, i.import_time as importTime, i.end_time as endTime, ")
        		.append("i.completed as completed, i.total_records as totalRecords, i.success_count as successCount, ")
			.append(	"i.failure_count as failureCount, i.createdby_id as createdBy ")
			.append("from m_import_document i inner join m_document d on i.document_id=d.id ")
			.append("where i.entity_type= ? ");
        		return sql.toString();
        }

        @Override
        public ImportData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long documentId = rs.getLong("documentId");
            final String name = rs.getString("name");
            final LocalDate importTime = JdbcSupport.getLocalDate(rs, "importTime");
            final LocalDate endTime = JdbcSupport.getLocalDate(rs, "endTime");
            final Boolean completed = rs.getBoolean("completed");
            final Integer totalRecords = JdbcSupport.getInteger(rs, "totalRecords");
            final Integer successCount = JdbcSupport.getInteger(rs, "successCount");
            final Integer failureCount = JdbcSupport.getInteger(rs, "failureCount");
            final Long createdBy = rs.getLong("createdBy");

            return ImportData.instance(id, documentId, importTime, endTime, completed,
            		name, createdBy, totalRecords, successCount, failureCount);
        }
    }


    @Override
    public DocumentData getOutputTemplateLocation(String importDocumentId) {
        this.securityContext.authenticatedUser();
        final ImportTemplateLocationMapper importTemplateLocationMapper=new ImportTemplateLocationMapper();
        final String sql = "select " + importTemplateLocationMapper.schema();

        return this.jdbcTemplate.queryForObject(sql, importTemplateLocationMapper, new Object[] {importDocumentId});
    }

    private static final class ImportTemplateLocationMapper implements RowMapper<DocumentData> {
        public String schema() {
            final StringBuilder sql = new StringBuilder();
            sql.append("d.location ")
                    .append("from m_import_document i inner join m_document d on i.document_id=d.id ")
                    .append("where i.id= ? ");
            return sql.toString();
        }
        @Override
        public DocumentData mapRow (ResultSet rs,int rowNum) throws SQLException {
            final String location = rs.getString("location");
            return new DocumentData(null,null,null,null,null,
                    null,null,null,location,null);
        }

    }
}