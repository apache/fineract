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
package org.apache.fineract.client.util;

import feign.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import org.apache.fineract.client.feign.services.DocumentsApiFixed;
import org.apache.fineract.client.feign.services.ImagesApi;

/**
 * Convenience utilities for file handling in Feign API calls.
 *
 * Provides helper methods for: - Media type detection from file extensions - File name extraction from HTTP response
 * headers - Content type probing
 *
 * Used in conjunction with {@link DocumentsApiFixed} and {@link ImagesApi} which use Data URL format for file uploads
 * (base64-encoded strings with data URI scheme).
 */
public final class FeignParts {

    private FeignParts() {}

    /**
     * Determine the media type based on file extension.
     *
     * @param fileName
     *            the name of the file
     * @return the media type string, or null if not recognized
     */
    public static String mediaType(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dotPos = fileName.lastIndexOf('.');
        if (dotPos == -1) {
            return null;
        }
        String ext = fileName.substring(dotPos + 1).toLowerCase();

        switch (ext) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "tif":
            case "tiff":
                return "image/tiff";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "doc":
                return "application/msword";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xls":
                return "application/vnd.ms-excel";
            case "odt":
                return "application/vnd.oasis.opendocument.text";
            case "ods":
                return "application/vnd.oasis.opendocument.spreadsheet";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * Extract the file name from the Content-Disposition header of a response.
     *
     * @param response
     *            the HTTP response
     * @return Optional containing the file name if present
     */
    public static Optional<String> fileName(Response response) {
        if (response.headers() == null) {
            return Optional.empty();
        }

        java.util.Collection<String> contentDispositionHeaders = response.headers().get("Content-Disposition");
        if (contentDispositionHeaders == null || contentDispositionHeaders.isEmpty()) {
            return Optional.empty();
        }

        String contentDisposition = contentDispositionHeaders.iterator().next();
        if (contentDisposition == null) {
            return Optional.empty();
        }

        int i = contentDisposition.indexOf("; filename=\"");
        if (i == -1) {
            return Optional.empty();
        }
        return Optional.of(contentDisposition.substring(i + "; filename=\"".length(), contentDisposition.length() - 1));
    }

    /**
     * Probe the content type of a file using Files.probeContentType.
     *
     * @param file
     *            the file to probe
     * @return the content type, or application/octet-stream as fallback
     * @throws IOException
     *             if an I/O error occurs
     */
    public static String probeContentType(File file) throws IOException {
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = mediaType(file.getName());
        }
        return contentType != null ? contentType : "application/octet-stream";
    }
}
