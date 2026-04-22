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

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataUrlDecoderContentProcessor implements ContentProcessor {

    private static final String DATA_URL_PREFIX = "dataurl.decode.";

    public static final String DATA_URL_DECODE_RESULT_CONTENT_TYPE = DATA_URL_PREFIX + "result.content-type";
    public static final String DATA_URL_DECODE_RESULT_ENCODING = DATA_URL_PREFIX + "result.encoding";
    public static final String DATA_URL_DECODE_PARAM_BUFFER_SIZE = DATA_URL_PREFIX + "param.buffer-size";

    private final ContentPipe pipe;
    private final FineractProperties properties;

    @Override
    public ContentProcessorContext process(final ContentProcessorContext ctx) {
        final Integer bufferSize = ctx.getParameter(DATA_URL_DECODE_PARAM_BUFFER_SIZE,
                requireNonNullElse(properties.getContent().getDefaultBufferSize(), 8192));

        final var pipedInputStream = pipe.pipe(ctx.getInputStream(), (in, out) -> {
            var dataUrlIn = new DataUrlInputStream(in);

            ctx.setResult(DATA_URL_DECODE_RESULT_CONTENT_TYPE, dataUrlIn.getContentType());
            ctx.setResult(DATA_URL_DECODE_RESULT_ENCODING, dataUrlIn.getEncoding());

            pipe.write(dataUrlIn, out, new byte[bufferSize]);
        });

        return ctx.clone(pipedInputStream);
    }

    private static class DataUrlInputStream extends FilterInputStream {

        private static final String DEFAULT_MEDIA_TYPE = "text/plain;charset=US-ASCII";
        private static final String ENCODING_BASE64 = "base64";
        private static final String ENCODING_URL = "urlencoded";
        private static final String META_PART = "data:";
        private static final char COMMA_CHAR = ',';
        private static final char SEMICOLON_CHAR = ';';

        private volatile String contentType;
        private volatile String encoding;

        DataUrlInputStream(InputStream in) {
            super(in);
        }

        private void readHeaderProcessed() throws IOException {
            if (StringUtils.isNotEmpty(contentType)) {
                return;
            }

            final var headerBuffer = new ByteArrayOutputStream(128);

            int b;
            while ((b = in.read()) != -1) {
                if (b == COMMA_CHAR) {
                    // stop reading header
                    break;
                }
                headerBuffer.write(b);
            }

            var header = headerBuffer.toString(StandardCharsets.UTF_8);

            parseHeader(header);
        }

        private void parseHeader(String header) {
            if (!header.startsWith(META_PART)) {
                this.contentType = DEFAULT_MEDIA_TYPE;
                this.encoding = ENCODING_URL;
                return;
            }

            String metaPart = header.substring(5); // Remove "data:"

            if (metaPart.endsWith(SEMICOLON_CHAR + ENCODING_BASE64)) {
                this.encoding = ENCODING_BASE64;
                // remove ";base64"
                metaPart = metaPart.substring(0, metaPart.length() - ENCODING_BASE64.length() - 1);
            } else {
                this.encoding = ENCODING_URL;
            }

            if (metaPart.isEmpty()) {
                this.contentType = DEFAULT_MEDIA_TYPE;
            } else {
                this.contentType = metaPart;
            }
        }

        String getContentType() throws IOException {
            readHeaderProcessed();

            return contentType;
        }

        String getEncoding() throws IOException {
            readHeaderProcessed();

            return encoding;
        }

        @Override
        public int read() throws IOException {
            readHeaderProcessed();

            return in.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            readHeaderProcessed();

            return in.read(b, off, len);
        }
    }
}
