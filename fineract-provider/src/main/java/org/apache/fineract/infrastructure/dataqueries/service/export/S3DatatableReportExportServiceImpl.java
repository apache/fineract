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
package org.apache.fineract.infrastructure.dataqueries.service.export;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableExportTargetParameter;
import org.apache.fineract.infrastructure.dataqueries.service.ReadReportingService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

@RequiredArgsConstructor
public class S3DatatableReportExportServiceImpl implements DatatableReportExportService {

    public static final int AWS_S3_MAXIMUM_KEY_LENGTH = 1024;
    private final ReadReportingService readExtraDataAndReportingService;

    private final ConfigurationDomainService configurationDomainService;
    private final S3Client s3Client;

    private final FineractProperties properties;

    @Override
    public ResponseHolder export(String reportName, MultivaluedMap<String, String> queryParams, Map<String, String> reportParams,
            boolean isSelfServiceUserReport, String parameterTypeValue) {
        try {
            StreamingOutput output = this.readExtraDataAndReportingService.retrieveReportCSV(reportName, parameterTypeValue, reportParams,
                    isSelfServiceUserReport);
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                output.write(byteArrayOutputStream);
                String folder = configurationDomainService.retrieveReportExportS3FolderName();
                String filePath = DatatableExportUtil.generateS3DatatableExportFileName(AWS_S3_MAXIMUM_KEY_LENGTH, folder, "csv",
                        reportName, reportParams);
                s3Client.putObject(
                        builder -> builder.bucket(properties.getReport().getExport().getS3().getBucketName()).key(filePath).build(),
                        RequestBody.fromBytes(byteArrayOutputStream.toByteArray()));
                return new ResponseHolder(Response.Status.NO_CONTENT);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while exporting to S3", e);
        }
    }

    @Override
    public boolean supports(DatatableExportTargetParameter exportType) {
        return DatatableExportTargetParameter.S3 == exportType;
    }
}
