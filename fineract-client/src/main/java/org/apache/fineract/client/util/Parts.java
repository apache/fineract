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

import java.io.File;
import java.util.Optional;
import okhttp3.MediaType;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import org.apache.fineract.client.services.DocumentsApiFixed;
import org.apache.fineract.client.services.ImagesApi;
import retrofit2.Response;

/**
 * Convenience Factory for {@link Part} (including {@link RequestBody}).
 *
 * {@link Part} is the argument of operations for binary uploads like
 * {@link DocumentsApiFixed#createDocument(String, Long, Part, String, String)},
 * {@link DocumentsApiFixed#updateDocument(String, Long, Long, Part, String, String)} and
 * {@link ImagesApi#create(String, Long, Part)} and {@link ImagesApi#update(String, Long, Part)}.
 *
 * @author Michael Vorburger.ch
 */
public final class Parts {

    private Parts() {}

    public static Part fromFile(File file) {
        RequestBody rb = RequestBody.create(file, mediaType(file.getName()));
        return Part.createFormData("file", file.getName(), rb);
    }

    public static Part fromBytes(String fileName, byte[] bytes) {
        RequestBody rb = RequestBody.create(bytes, mediaType(fileName));
        return Part.createFormData("file", fileName, rb);
    }

    // package local, for unit testing
    // TODO this logic should be on the Server, not have to be done by the client...
    static MediaType mediaType(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dotPos = fileName.lastIndexOf('.');
        if (dotPos == -1) {
            return null;
        }
        String ext = fileName.substring(dotPos + 1);
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
        switch (ext) {
            case "jpg":
            case "jpeg":
                return MediaType.get("image/jpeg");
            case "png":
                return MediaType.get("image/png");
            case "tif":
            case "tiff":
                return MediaType.get("image/tiff");
            case "gif":
                return MediaType.get("image/gif");
            case "pdf":
                return MediaType.get("application/pdf");
            case "docx":
                return MediaType.get("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "doc":
                return MediaType.get("application/msword");
            case "xlsx":
                return MediaType.get("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "xls":
                return MediaType.get("application/vnd.ms-excel");
            case "odt":
                return MediaType.get("application/vnd.oasis.opendocument.text");
            case "ods":
                return MediaType.get("application/vnd.oasis.opendocument.spreadsheet");
            case "txt":
                return MediaType.get("text/plain");
            default:
                return null;
        }
    }

    public static Optional<String> fileName(Response<?> response) {
        String contentDisposition = response.headers().get("Content-Disposition");
        if (contentDisposition == null) {
            return Optional.empty();
        }
        int i = contentDisposition.indexOf("; filename=\"");
        if (i == -1) {
            return Optional.empty();
        }
        return Optional.of(contentDisposition.substring(i + "; filename=\"".length(), contentDisposition.length() - 1));
    }
}
