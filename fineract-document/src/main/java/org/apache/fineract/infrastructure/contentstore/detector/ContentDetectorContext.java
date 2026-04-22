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
package org.apache.fineract.infrastructure.contentstore.detector;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class ContentDetectorContext {

    private InputStream inputStream;
    @Builder.Default
    private boolean inputStreamEnabled = false;
    private String fileName;
    private String mimeType;
    private String extension;
    private String format;

    public ContentDetectorContext clone(String mimeType, String extension, String format) {
        return new ContentDetectorContext(this.inputStream, this.inputStreamEnabled, this.fileName, mimeType, extension, format);
    }

    public ContentDetectorContext clone(InputStream inputStream, String mimeType, String extension, String format) {
        return new ContentDetectorContext(inputStream, this.inputStreamEnabled, this.fileName, mimeType, extension, format);
    }
}
