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
package org.apache.fineract.util;

import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public final class StreamResponseUtil {

    public static String DISPOSITION_TYPE_ATTACHMENT = "attachment";
    public static String DISPOSITION_TYPE_INLINE = "inline";

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private StreamResponseUtil() {}

    public static Response ok(final StreamResponseData content) {
        final var stream = new StreamingOutput() {

            @Override
            public void write(OutputStream out) throws IOException {
                IOUtils.copy(content.getStream(), out);
            }
        };

        if (StringUtils.isEmpty(content.getDispositionType())) {
            return Response.ok(stream, content.getType()).build();
        } else {
            return Response.ok(stream, content.getType()).header(HttpHeaders.CONTENT_DISPOSITION,
                    String.format("%s; filename=\"%s\"", content.getDispositionType(), content.getFileName())).build();
        }
    }

    public static Future<?> ok(final AsyncResponse asyncResponse, final StreamResponseData content) {
        return executor.submit(() -> {
            if (StringUtils.isEmpty(content.getDispositionType())) {
                asyncResponse.resume(Response.ok(content.getStream(), content.getType()).build());
            } else {
                asyncResponse.resume(Response.ok(content.getStream(), content.getType()).header(HttpHeaders.CONTENT_DISPOSITION,
                        String.format("%s; filename=\"%s\"", content.getDispositionType(), content.getFileName())).build());
            }
        });
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class StreamResponseData {

        private InputStream stream;
        private String type;
        private String fileName;
        private String dispositionType;
        private Long size;
    }
}
