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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNullElse;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataUrlEncoderContentProcessor implements ContentProcessor {

    private static final String DATA_URL_ENCODE_PREFIX = "dataurl.encode.";

    public static final String DATA_URL_ENCODE_PARAM_BUFFER_SIZE = DATA_URL_ENCODE_PREFIX + "param.buffer-size";
    public static final String DATA_URL_ENCODE_PARAM_CONTENT_TYPE = DATA_URL_ENCODE_PREFIX + "param.content-type";
    public static final String DATA_URL_ENCODE_PARAM_ENCODING = DATA_URL_ENCODE_PREFIX + "param.encoding";

    private final ContentPipe pipe;
    private final FineractProperties properties;

    @Override
    public ContentProcessorContext process(final ContentProcessorContext ctx) {
        final Integer bufferSize = ctx.getParameter(DATA_URL_ENCODE_PARAM_BUFFER_SIZE,
                requireNonNullElse(properties.getContent().getDefaultBufferSize(), 8192));
        final String contentType = ctx.getParameter(DATA_URL_ENCODE_PARAM_CONTENT_TYPE, "text/plain");
        final String encoding = ctx.getParameter(DATA_URL_ENCODE_PARAM_ENCODING, "charset=US-ASCII");

        final var pipedInputStream = pipe.pipe(ctx.getInputStream(), (in, out) -> {
            pipe.write(new DataUrlEncoderInputStream(in, contentType, encoding), out, new byte[bufferSize]);
        });

        return ctx.clone(pipedInputStream);
    }

    private static class DataUrlEncoderInputStream extends FilterInputStream {

        private static final String META_PART = "data:";
        private static final char COMMA_CHAR = ',';
        private static final char SEMICOLON_CHAR = ';';

        private final byte[] prefix;
        private int prefixIndex = 0;

        DataUrlEncoderInputStream(InputStream in, String contentType, String encoding) {
            super(in);
            this.prefix = (META_PART + contentType + SEMICOLON_CHAR + encoding + COMMA_CHAR).getBytes(UTF_8);
        }

        private boolean isPrefixConsumed() {
            return prefixIndex >= prefix.length;
        }

        @Override
        public int read() throws IOException {
            if (!isPrefixConsumed()) {
                // return the next byte of the prefix
                // we need to cast to int and mask with 0xFF to ensure positive value (0-255)
                return prefix[prefixIndex++] & 0xFF;
            }

            // served the prefix, now the real stream starts
            return in.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            // optimization: do not call super.read(b, off, len) as that might call
            // the single-byte read() repeatedly, which is slow.

            if (isPrefixConsumed()) {
                return in.read(b, off, len);
            }

            // still have prefix data to write
            int availableInPrefix = prefix.length - prefixIndex;

            // fill the buffer with remaining prefix bytes (or as much as requested)
            int bytesToCopyFromPrefix = Math.min(len, availableInPrefix);

            System.arraycopy(prefix, prefixIndex, b, off, bytesToCopyFromPrefix);
            prefixIndex += bytesToCopyFromPrefix;

            // if we filled the request entirely with the prefix, return immediately
            if (bytesToCopyFromPrefix == len) {
                return len;
            }

            // if we still have space in the buffer, fill the rest from the underlying stream
            int bytesToReadFromStream = len - bytesToCopyFromPrefix;
            int bytesReadFromStream = in.read(b, off + bytesToCopyFromPrefix, bytesToReadFromStream);

            if (bytesReadFromStream == -1) {
                // underlying stream is empty, but we did write the prefix
                return bytesToCopyFromPrefix;
            }

            return bytesToCopyFromPrefix + bytesReadFromStream;
        }

        @Override
        public long skip(long n) throws IOException {
            if (isPrefixConsumed()) {
                return in.skip(n);
            }

            long availableInPrefix = Integer.valueOf(prefix.length - prefixIndex).longValue();
            long skippableInPrefix = Math.min(n, availableInPrefix);

            prefixIndex += Long.valueOf(skippableInPrefix).intValue();
            long remainingToSkip = n - skippableInPrefix;

            if (remainingToSkip > 0) {
                return skippableInPrefix + in.skip(remainingToSkip);
            }

            return skippableInPrefix;
        }

        @Override
        public int available() throws IOException {
            if (isPrefixConsumed()) {
                return in.available();
            }
            // total available is prefix remaining + stream available
            return (prefix.length - prefixIndex) + in.available();
        }
    }
}
