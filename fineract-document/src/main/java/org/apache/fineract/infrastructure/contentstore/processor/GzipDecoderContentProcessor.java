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
package org.apache.fineract.infrastructure.contentstore.processor;

import static java.util.Objects.requireNonNullElse;

import java.util.zip.GZIPInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GzipDecoderContentProcessor implements ContentProcessor {

    private static final String GZIP_DECODE_PREFIX = "gzip.decode.";

    public static final String GZIP_DECODE_PARAM_BUFFER_SIZE = GZIP_DECODE_PREFIX + "buffer-size";

    private final ContentPipe pipe;
    private final FineractProperties properties;

    @Override
    public ContentProcessorContext process(final ContentProcessorContext ctx) {
        final Integer bufferSize = ctx.getParameter(GZIP_DECODE_PARAM_BUFFER_SIZE,
                requireNonNullElse(properties.getContent().getDefaultBufferSize(), 8192));

        final var pipedInputStream = pipe.pipe(ctx.getInputStream(), (in, out) -> {
            pipe.write(new GZIPInputStream(in), out, new byte[bufferSize]);
        });

        return ctx.clone(pipedInputStream);
    }
}
