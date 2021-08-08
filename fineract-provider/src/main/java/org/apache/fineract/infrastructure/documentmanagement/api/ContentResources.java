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
package org.apache.fineract.infrastructure.documentmanagement.api;

import com.google.common.io.ByteSource;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities common to file upload/download resources.
 *
 * @author Michael Vorburger.ch
 */
final class ContentResources {

    private static final Logger LOG = LoggerFactory.getLogger(ContentResources.class);

    private ContentResources() {}

    static Response fileDataToResponse(FileData fileData, String fileName, String dispositionType) {
        ResponseBuilder response;
        try {
            ByteSource byteSource = fileData.getByteSource();
            // TODO Where is this InputStream closed?! It needs to be AFTER it's read by JAX-RS.. how to do that?
            InputStream is = byteSource.openBufferedStream();
            response = Response.ok(is);
            response.header("Content-Disposition", dispositionType + "; filename=\"" + fileName + "\"");
            response.header("Content-Length", byteSource.sizeIfKnown().or(-1L));
            response.header("Content-Type", fileData.contentType());
        } catch (IOException e) {
            LOG.error("resizedImage.getByteSource().openBufferedStream() failed", e);
            response = Response.serverError();
        }
        return response.build();
    }

    static Response fileDataToResponse(FileData fileData, String dispositionType) {
        return fileDataToResponse(fileData, fileData.name(), dispositionType);
    }
}
