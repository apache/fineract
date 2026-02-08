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

import java.io.FilterInputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SizeContentProcessor implements ContentProcessor {

    private static final String SIZE_PREFIX = "size.";

    public static final String SIZE_RESULT_VALUE = SIZE_PREFIX + "result.value";

    @Override
    public ContentProcessorContext process(ContentProcessorContext ctx) {
        return ctx.clone(new ByteCountingInputStream(ctx));
    }

    private static final class ByteCountingInputStream extends FilterInputStream {

        private final ContentProcessorContext ctx;
        private long byteCount = 0;

        ByteCountingInputStream(ContentProcessorContext ctx) {
            super(ctx.getInputStream());
            this.ctx = ctx;
        }

        @Override
        public int read() throws IOException {
            int data = in.read();

            if (data != -1) {
                byteCount++;
            }

            return data;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int count = in.read(b, off, len);

            if (count != -1) {
                byteCount += count;
            }

            return count;
        }

        @Override
        public void close() throws IOException {
            ctx.setResult(SIZE_RESULT_VALUE, byteCount);

            super.close();
        }
    }
}
