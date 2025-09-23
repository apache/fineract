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
package org.apache.fineract.portfolio.loanaccount.guarantor.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.core.UriInfo;
import java.io.File;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.fineract.validation.constraints.Locale;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

@SuperBuilder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadTemplateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.upload.template.file.required}")
    private InputStream uploadedInputStream;

    private File uploadedFile;

    @NotNull(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.upload.template.fileDetail.required}")
    private FormDataContentDisposition fileDetail;

    private UriInfo uriInfo;

    private FormDataBodyPart bodyPart;

    @NotBlank(message = "{org.apache.fineract.portfolio.loanaccount.guarantor.locale.notBlank}")
    @Size(max = 50, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.locale.size}")
    @Locale
    private String locale;

    @Size(max = 20, message = "{org.apache.fineract.portfolio.loanaccount.guarantor.date.format.size}")
    private String dateFormat;

    public static UploadTemplateRequest fromParameters(InputStream uploadedInputStream, FormDataContentDisposition fileDetail,
            String locale, String dateFormat) {
        return UploadTemplateRequest.builder().uploadedInputStream(uploadedInputStream).fileDetail(fileDetail).locale(locale)
                .dateFormat(dateFormat).build();
    }
}
