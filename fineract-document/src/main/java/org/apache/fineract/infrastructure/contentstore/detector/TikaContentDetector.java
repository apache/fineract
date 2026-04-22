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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.contentstore.exception.ContentDetectorException;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public final class TikaContentDetector implements ContentDetector {

    private final Tika tika = new Tika();

    @Override
    public ContentDetectorContext detect(ContentDetectorContext ctx) {
        if (ctx.getInputStream() != null && !ctx.isInputStreamEnabled()) {
            log.warn(
                    "Input stream provided, but not explicitly enabled for detection. This operation is potentially making the input stream unusable, especially with HTTP multipart streams. Input stream will be ignored!");
        }

        try {
            if (ctx.getInputStream() != null && ctx.isInputStreamEnabled()) {
                final var stream = TikaInputStream.get(ctx.getInputStream());
                final var metadata = new Metadata();
                metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, ctx.getFileName());

                final var mimeType = tika.detect(stream, metadata);
                final var extension = getExtension(mimeType);

                stream.reset();

                // note: original stream should be still usable if "mark" is supported
                return ctx.clone(mimeType, extension, extension.substring(1));
            } else if (StringUtils.isNotEmpty(ctx.getFileName())) {
                final var mimeType = tika.detect(ctx.getFileName());
                final var extension = getExtension(mimeType);

                return ctx.clone(mimeType, extension, extension.substring(1));
            }
        } catch (Exception e) {
            throw new ContentDetectorException(e);
        }

        throw new ContentDetectorException(
                new IllegalArgumentException("Could not run detection, because required arguments were missing."));
    }

    private String getExtension(String mimeType) {
        try {
            return Optional.ofNullable(MimeTypes.getDefaultMimeTypes().forName(mimeType)).orElseGet(() -> {
                try {
                    return MimeTypes.getDefaultMimeTypes().forName(MimeTypes.OCTET_STREAM);
                } catch (MimeTypeException e) {
                    throw new ContentDetectorException(e);
                }
            }).getExtension();
        } catch (Exception e) {
            throw new ContentDetectorException(e);
        }
    }
}
