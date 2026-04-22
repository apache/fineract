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
package org.apache.fineract.client.feign;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.HttpEntity;

/**
 * Custom multipart encoder for Fineract Feign clients. Uses Apache HttpClient's MultipartEntityBuilder to properly
 * construct multipart/form-data requests.
 */
public class FineractMultipartEncoder implements Encoder {

    private final Encoder delegate;

    public FineractMultipartEncoder(Encoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (object instanceof MultipartData) {
            encodeMultipart((MultipartData) object, template);
        } else if (object instanceof File) {
            encodeFileAsMultipart((File) object, template);
        } else if (object instanceof String && template.headers().containsKey("Content-Type")) {
            String contentType = template.headers().get("Content-Type").iterator().next();
            if (contentType.startsWith("text/html") || contentType.startsWith("text/plain")) {
                byte[] bodyBytes = ((String) object).getBytes(StandardCharsets.UTF_8);
                template.body(bodyBytes, StandardCharsets.UTF_8);
            } else {
                delegate.encode(object, bodyType, template);
            }
        } else {
            delegate.encode(object, bodyType, template);
        }
    }

    private void encodeFileAsMultipart(File file, RequestTemplate template) throws EncodeException {
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            String contentType = detectContentType(file);
            MultipartData multipartData = new MultipartData().addFile("file", file.getName(), fileData, contentType);
            encodeMultipart(multipartData, template);
        } catch (IOException e) {
            throw new EncodeException("Failed to encode File as multipart: " + file, e);
        }
    }

    private String detectContentType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    private void encodeMultipart(MultipartData data, RequestTemplate template) throws EncodeException {
        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(org.apache.hc.client5.http.entity.mime.HttpMultipartMode.STRICT);

            for (MultipartData.Part part : data.getParts()) {
                if (part.getFileData() != null) {
                    org.apache.hc.core5.http.ContentType ct = org.apache.hc.core5.http.ContentType.create(part.getContentType());
                    builder.addBinaryBody(part.getName(), part.getFileData(), ct, part.getFileName());
                } else if (part.getTextValue() != null) {
                    builder.addTextBody(part.getName(), part.getTextValue(), org.apache.hc.core5.http.ContentType.TEXT_PLAIN);
                }
            }

            HttpEntity entity = builder.build();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            entity.writeTo(outputStream);
            byte[] body = outputStream.toByteArray();

            template.body(body, null);
            template.removeHeader("Content-Type");
            String contentTypeValue = entity.getContentType();
            String cleanContentType = contentTypeValue;
            if (contentTypeValue.contains(";")) {
                int firstSemicolon = contentTypeValue.indexOf(';');
                String mainType = contentTypeValue.substring(0, firstSemicolon).trim();
                String paramsSection = contentTypeValue.substring(firstSemicolon + 1);

                String boundary = null;
                int boundaryIndex = paramsSection.indexOf("boundary=");
                if (boundaryIndex != -1) {
                    int boundaryStart = boundaryIndex;
                    int boundaryEnd = paramsSection.indexOf(';', boundaryStart);
                    if (boundaryEnd == -1) {
                        boundary = paramsSection.substring(boundaryStart).trim();
                    } else {
                        boundary = paramsSection.substring(boundaryStart, boundaryEnd).trim();
                    }
                }

                if (boundary != null) {
                    cleanContentType = mainType + "; " + boundary;
                }
            }
            template.header("Content-Type", cleanContentType);
        } catch (IOException e) {
            throw new EncodeException("Failed to encode multipart request", e);
        }
    }

    public static class MultipartData {

        private final java.util.List<Part> parts = new java.util.ArrayList<>();

        public MultipartData addFile(String name, String fileName, byte[] data, String contentType) {
            parts.add(new Part(name, fileName, data, contentType));
            return this;
        }

        public MultipartData addText(String name, String value) {
            parts.add(new Part(name, value));
            return this;
        }

        public java.util.List<Part> getParts() {
            return parts;
        }

        public static class Part {

            private final String name;
            private final String fileName;
            private final byte[] fileData;
            private final String contentType;
            private final String textValue;

            public Part(String name, String fileName, byte[] fileData, String contentType) {
                this.name = name;
                this.fileName = fileName;
                this.fileData = fileData;
                this.contentType = contentType;
                this.textValue = null;
            }

            public Part(String name, String textValue) {
                this.name = name;
                this.textValue = textValue;
                this.fileName = null;
                this.fileData = null;
                this.contentType = null;
            }

            public String getName() {
                return name;
            }

            public String getFileName() {
                return fileName;
            }

            public byte[] getFileData() {
                return fileData;
            }

            public String getContentType() {
                return contentType;
            }

            public String getTextValue() {
                return textValue;
            }
        }
    }
}
