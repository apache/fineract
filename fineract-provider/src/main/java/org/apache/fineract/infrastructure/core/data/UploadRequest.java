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
package org.apache.fineract.infrastructure.core.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.File;
import java.io.InputStream;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

public class UploadRequest {

    @Schema(type = "string", format = "binary")
    @FormDataParam("file")
    private InputStream uploadedInputStream;

    @Schema(implementation = File.class, hidden = true)
    @FormDataParam("file")
    private File uploadedFile;

    @Schema(implementation = FormDataContentDisposition.class, hidden = true)
    @FormDataParam("file")
    private FormDataContentDisposition fileDetail;

    @Schema(implementation = UriInfo.class, hidden = true)
    @FormDataParam("file")
    private UriInfo uriInfo;

    @Schema(implementation = UriInfo.class, hidden = true)
    @FormDataParam("file")
    private FormDataBodyPart bodyPart;

    @Schema(name = "locale", type = "string", accessMode = Schema.AccessMode.READ_WRITE)
    @FormDataParam("locale")
    private String locale;

    @Schema(name = "dateFormat", type = "string", accessMode = Schema.AccessMode.READ_WRITE)
    @FormDataParam("dateFormat")
    private String dateFormat;

    public InputStream getUploadedInputStream() {
        return uploadedInputStream;
    }

    public void setUploadedInputStream(InputStream uploadedInputStream) {
        this.uploadedInputStream = uploadedInputStream;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}
