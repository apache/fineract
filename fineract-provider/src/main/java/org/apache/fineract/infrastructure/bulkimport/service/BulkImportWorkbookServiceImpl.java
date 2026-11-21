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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.bulkimport.data.BulkImportEvent;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.data.ImportData;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocument;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.apache.fineract.infrastructure.bulkimport.mapping.ImportDocumentMapper;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentCreateRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentCreateResponse;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BulkImportWorkbookServiceImpl implements BulkImportWorkbookService {

    static final int MAX_FILE_NAME_LENGTH = 250;
    private static final Set<String> ACCEPTED_EXTENSIONS = Set.of("xls", "xlsx");
    private static final String FILE_PARAMETER = "file";

    private final TransactionBoundApplicationEventPublisher eventPublisher;
    private final PlatformSecurityContext securityContext;
    private final ImportDocumentRepository importDocumentRepository;
    private final DocumentWritePlatformService writePlatformService;
    private final JdbcTemplate jdbcTemplate;
    private final ContentPipe pipe;
    private final ImportDocumentMapper mapper;

    @Override
    public Long importWorkbook(String entity, InputStream inputStream, FormDataContentDisposition fileDetail, final String locale,
            final String dateFormat) {
        try {
            if (entity != null && inputStream != null && fileDetail != null && locale != null && dateFormat != null) {
                validateFileName(fileDetail.getFileName());
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                inputStream.transferTo(baos);
                final byte[] bytes = baos.toByteArray();
                InputStream clonedInputStream = new ByteArrayInputStream(bytes);
                final BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
                final Tika tika = new Tika();
                final TikaInputStream tikaInputStream = TikaInputStream.get(bis);
                final String fileType = tika.detect(tikaInputStream);
                if (!fileType.contains("msoffice") && !fileType.contains("application/vnd.ms-excel")
                        && !fileType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                    throw unreadableWorkbook(null);
                }
                Workbook workbook = readWorkbook(clonedInputStream);
                final GlobalEntityType entityType = BulkImportTemplateLayout.findEntityType(entity).orElse(null);
                if (entityType == null) {
                    workbook.close();
                    throw new GeneralPlatformDomainRuleException("error.msg.unable.to.find.resource", "Unable to find requested resource");
                }
                final BulkImportTemplateLayout layout = BulkImportTemplateLayout.of(entityType);
                try {
                    layout.validate(workbook);
                } catch (PlatformApiDataValidationException e) {
                    workbook.close();
                    throw e;
                }
                return publishEvent(layout, fileDetail, fileType, entityType, workbook, locale, dateFormat);
            }
            throw new GeneralPlatformDomainRuleException("error.msg.null", "One or more of the given parameters not found");
        } catch (IOException e) {
            log.error("Problem occurred in importWorkbook function", e);
            throw new GeneralPlatformDomainRuleException("error.msg.io.exception",
                    "IO exception occurred with " + fileDetail.getFileName() + " " + e.getMessage(), e);

        }
    }

    private static void validateFileName(final String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw invalidFile("error.msg.bulk.import.file.name.missing", "File name is required");
        }
        if (fileName.length() > MAX_FILE_NAME_LENGTH) {
            throw invalidFile("error.msg.bulk.import.file.name.too.long",
                    "File name is too long (max " + MAX_FILE_NAME_LENGTH + " characters)");
        }
        final String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isBlank(extension)) {
            throw invalidFile("error.msg.bulk.import.file.extension.missing", "File has no extension - only .xls and .xlsx are accepted");
        }
        if (!ACCEPTED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw invalidFile("error.msg.bulk.import.file.extension.invalid",
                    "File extension ." + extension + " is not supported - only .xls and .xlsx are accepted");
        }
    }

    private static Workbook readWorkbook(final InputStream inputStream) {
        try {
            return WorkbookFactory.create(inputStream);
        } catch (IOException | RuntimeException e) {
            log.warn("Uploaded bulk import file could not be read as a workbook", e);
            throw unreadableWorkbook(e);
        }
    }

    private static PlatformApiDataValidationException unreadableWorkbook(final Throwable cause) {
        return new PlatformApiDataValidationException("error.msg.bulk.import.file.unreadable",
                "File could not be read as an Excel workbook", FILE_PARAMETER, cause);
    }

    private static PlatformApiDataValidationException invalidFile(final String messageCode, final String message) {
        return new PlatformApiDataValidationException(messageCode, message, FILE_PARAMETER);
    }

    private DocumentCreateResponse createDocument(final Workbook workbook, final FormDataContentDisposition fileDetail,
            final String fileType) {
        final var pipedInputStream = pipe.pipe(output -> {
            workbook.write(output);
        });

        // TODO: does it really make sense to use the current user's ID?
        // TODO: wouldn't it be better to use "entityType.name()" as entity name?
        return writePlatformService
                .createDocument(DocumentCreateRequest.builder().entityId(this.securityContext.authenticatedUser().getId())
                        .entityType("IMPORT").name(fileDetail.getFileName()).description(fileDetail.getFileName())
                        .fileName(fileDetail.getFileName()).size(fileDetail.getSize()).type(fileType).stream(pipedInputStream).build());
    }

    private Long publishEvent(final BulkImportTemplateLayout layout, final FormDataContentDisposition fileDetail, final String fileType,
            final GlobalEntityType entityType, final Workbook workbook, final String locale, final String dateFormat) {

        final var importDocument = ImportDocument.instance(null, DateUtils.getLocalDateTimeOfTenant(), entityType.getValue(),
                securityContext.authenticatedUser(), layout.countRecords(workbook));

        final var response = createDocument(workbook, fileDetail, fileType);

        importDocument.setDocumentId(response.getResourceId());

        // TODO: should we mark as "not yet processed by import handler"?
        importDocumentRepository.saveAndFlush(importDocument);

        final var event = new BulkImportEvent(this, workbook, fileDetail.getFileName(), fileType, importDocument, locale, dateFormat,
                ThreadLocalContextUtil.getContext(), this.securityContext.authenticatedUser().getId());

        eventPublisher.publishEvent(event);

        return importDocument.getId();
    }

    @Override
    public Collection<ImportData> getImports(GlobalEntityType type) {
        this.securityContext.authenticatedUser();

        final ImportMapper rm = new ImportMapper();
        final String sql = "select " + rm.schema() + " order by i.id desc";

        return this.jdbcTemplate.query(sql, rm, type.getValue());
    }

    @Override
    public ImportData getImport(Long id) {
        return importDocumentRepository.findById(id).map(mapper::map).orElse(null);
    }

    private static final class ImportMapper implements RowMapper<ImportData> {

        private static final String IMPORT_SCHEMA = """
                i.id as id, i.document_id as documentId, d.name as name, i.import_time as importTime, i.end_time as endTime,
                i.completed as completed, i.total_records as totalRecords, i.success_count as successCount,
                i.failure_count as failureCount, i.createdby_id as createdBy
                from m_import_document i inner join m_document d on i.document_id=d.id where i.entity_type= ?\s""";

        private String schema() {
            return IMPORT_SCHEMA;
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

            return ImportData.instance(id, documentId, importTime, endTime, completed, name, createdBy, totalRecords, successCount,
                    failureCount);
        }
    }
}
