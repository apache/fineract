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
package org.apache.fineract.client.feign.services;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import feign.Response;
import java.io.File;
import java.util.Map;

/**
 * Client API (Feign) for /images.
 *
 * This class is entirely hand-written, inspired by DocumentsApiFixed, and from /images methods which currently end up
 * in DefaultApi (see <a href="https://issues.apache.org/jira/browse/FINERACT-1222">FINERACT-1222</a>), but fixed for
 * bugs in the code generation (see <a href="https://issues.apache.org/jira/browse/FINERACT-1227">FINERACT-1227</a>).
 *
 * Note: For image uploads, use {@link #prepareFileUpload(File)} to prepare file data as a Data URL.
 */
public interface ImagesApi {

    @RequestLine("POST /v1/{entityType}/{entityId}/images")
    @Headers("Content-Type: text/html")
    Response create(@Param("entityType") String entityType, @Param("entityId") Long entityId, String dataUrl);

    @RequestLine("GET /v1/{entityType}/{entityId}/images")
    Response get(@Param("entityType") String entityType, @Param("entityId") Long entityId, @QueryMap Map<String, Object> queryParams);

    @RequestLine("PUT /v1/{entityType}/{entityId}/images")
    @Headers("Content-Type: text/html")
    Response update(@Param("entityType") String entityType, @Param("entityId") Long entityId, String dataUrl);

    @RequestLine("DELETE /v1/{entityType}/{entityId}/images")
    Response delete(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    static String prepareFileUpload(File file) {
        try {
            byte[] fileData = java.nio.file.Files.readAllBytes(file.toPath());
            String contentType = detectMediaType(file.getName());
            String dataUrlPrefix = getDataUrlPrefix(contentType);
            String base64Data = java.util.Base64.getEncoder().encodeToString(fileData);
            return dataUrlPrefix + base64Data;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to prepare file for upload: " + file, e);
        }
    }

    private static String getDataUrlPrefix(String contentType) {
        return "data:" + contentType + ";base64,";
    }

    private static String detectMediaType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        int dotPos = fileName.lastIndexOf('.');
        if (dotPos == -1) {
            return "application/octet-stream";
        }
        String ext = fileName.substring(dotPos + 1).toLowerCase();

        switch (ext) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "tif":
            case "tiff":
                return "image/tiff";
            case "pdf":
                return "application/pdf";
            default:
                return "application/octet-stream";
        }
    }
}
