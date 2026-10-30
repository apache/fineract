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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import feign.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.FineractMultipartEncoder.MultipartData;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

public class FeignBulkImportHelper {

    private static final String WORKBOOK_CONTENT_TYPE = "application/vnd.ms-excel";

    private final FineractFeignClient fineractClient;

    public FeignBulkImportHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Workbook downloadTemplate(String entityPath, Map<String, Object> queryParams) {
        try (Response response = fineractClient.bulkImportTemplates().downloadTemplate(entityPath, queryParams)) {
            return new HSSFWorkbook(new ByteArrayInputStream(readBody(response)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Long uploadTemplate(String entityPath, Map<String, Object> queryParams, Workbook workbook, String fileName, String locale,
            String dateFormat) {
        MultipartData multipartData = new MultipartData().addFile("file", fileName, toBytes(workbook), WORKBOOK_CONTENT_TYPE)
                .addText("locale", locale).addText("dateFormat", dateFormat);
        ApiResponse<Long> response = ok(
                () -> fineractClient.bulkImportTemplates().uploadTemplateWithHttpInfo(entityPath, queryParams, multipartData));
        if (response.getStatusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.getStatusCode() + " from " + entityPath + "/uploadtemplate");
        }
        return response.getData();
    }

    public byte[] downloadOutputTemplate(Long importDocumentId) {
        try (Response response = fineractClient.bulkImportTemplates().downloadOutputTemplate(importDocumentId)) {
            return readBody(response);
        }
    }

    private static byte[] readBody(Response response) {
        try (InputStream body = response.body().asInputStream()) {
            byte[] bytes = body.readAllBytes();
            if (response.status() != 200) {
                throw new IllegalStateException("HTTP " + response.status() + " from " + response.request().url() + ": "
                        + new String(bytes, StandardCharsets.UTF_8));
            }
            return bytes;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] toBytes(Workbook workbook) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
