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
package org.apache.fineract.infrastructure.core.domain;

public class Base64EncodedImage {

    private final String base64EncodedString;
    private final String fileExtension;

    public Base64EncodedImage(final String base64EncodedString, final String fileExtension) {
        this.base64EncodedString = base64EncodedString;
        this.fileExtension = fileExtension;
    }

    public String getBase64EncodedString() {
        return this.base64EncodedString;
    }

    public String getFileExtension() {
        return this.fileExtension;
    }
}