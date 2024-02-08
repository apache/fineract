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

import java.io.InputStream;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.stereotype.Component;

/**
 * Validator for uploaded files.
 *
 * @author Michael Vorburger.ch
 */
@Component
public class FileUploadValidator {

    public void validate(Long contentLength, InputStream inputStream, FormDataContentDisposition fileDetails, FormDataBodyPart bodyPart) {
        new DataValidatorBuilder().resource("fileUpload").reset().parameter("Content-Length").value(contentLength).notBlank()
                .integerGreaterThanNumber(0).reset().parameter("InputStream").value(inputStream).notNull().reset()
                .parameter("FormDataContentDisposition").value(fileDetails).notNull().reset().parameter("FormDataBodyPart").value(bodyPart)
                .notNull().throwValidationErrors();
    }
}
