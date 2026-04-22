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
package org.apache.fineract.organisation.staff.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.File;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffUploadRequest implements Serializable {
    // TODO: prefixing attributes with "upload" when we are already in a class named "XXXUploadXXX" is inconvenient; I'd
    // just name it "data"; we don't have to replicate the data types in the attribute names

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(type = "string", format = "binary")
    @FormDataParam("file")
    private InputStream uploadedInputStream;

    @Schema(implementation = File.class, hidden = true)
    @FormDataParam("file")
    private File file;

    @Schema(implementation = FormDataContentDisposition.class, hidden = true)
    @FormDataParam("file")
    private FormDataContentDisposition fileDetail;

    @Schema(name = "locale")
    @FormDataParam("locale")
    private String locale;

    @Schema(name = "dateFormat")
    @FormDataParam("dateFormat")
    private String dateFormat;
}
