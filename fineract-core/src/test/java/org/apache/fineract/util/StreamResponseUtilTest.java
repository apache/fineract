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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class StreamResponseUtilTest {

    @Test
    void okReturnsResponseWithoutContentDispositionWhenDispositionTypeEmpty() {

        InputStream stream = new ByteArrayInputStream("test".getBytes(UTF_8));

        StreamResponseUtil.StreamResponseData data = StreamResponseUtil.StreamResponseData.builder().stream(stream).type("text/plain")
                .fileName("file.txt").build();

        Response response = StreamResponseUtil.ok(data);

        assertEquals("text/plain", response.getMediaType().toString());
        assertNull(response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    void okReturnsResponseWithContentDispositionWhenDispositionTypePresent() {

        InputStream stream = new ByteArrayInputStream("test".getBytes(UTF_8));

        StreamResponseUtil.StreamResponseData data = StreamResponseUtil.StreamResponseData.builder().stream(stream).type("text/plain")
                .fileName("file.txt").dispositionType(StreamResponseUtil.DISPOSITION_TYPE_ATTACHMENT).build();

        Response response = StreamResponseUtil.ok(data);

        assertEquals("text/plain", response.getMediaType().toString());

        String header = response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION);

        assertNotNull(header);
        assertTrue(header.contains(StreamResponseUtil.DISPOSITION_TYPE_ATTACHMENT));
        assertTrue(header.contains("file.txt"));
    }

    @Test
    void okAsyncResponseWithoutDisposition() throws Exception {

        AsyncResponse asyncResponse = Mockito.mock(AsyncResponse.class);

        InputStream stream = new ByteArrayInputStream("test".getBytes(UTF_8));

        StreamResponseUtil.StreamResponseData data = StreamResponseUtil.StreamResponseData.builder().stream(stream).type("text/plain")
                .build();

        Future<?> future = StreamResponseUtil.ok(asyncResponse, data);
        future.get();

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);

        Mockito.verify(asyncResponse).resume(captor.capture());

        Response response = captor.getValue();

        assertEquals("text/plain", response.getMediaType().toString());
    }

    @Test
    void okAsyncResponseWithDisposition() throws Exception {

        AsyncResponse asyncResponse = Mockito.mock(AsyncResponse.class);

        InputStream stream = new ByteArrayInputStream("test".getBytes(UTF_8));

        StreamResponseUtil.StreamResponseData data = StreamResponseUtil.StreamResponseData.builder().stream(stream).type("text/plain")
                .fileName("file.txt").dispositionType(StreamResponseUtil.DISPOSITION_TYPE_INLINE).build();

        Future<?> future = StreamResponseUtil.ok(asyncResponse, data);
        future.get();

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);

        Mockito.verify(asyncResponse).resume(captor.capture());

        Response response = captor.getValue();

        assertEquals("text/plain", response.getMediaType().toString());
    }

    @Test
    void okHeaderContainsCorrectFilename() {

        InputStream stream = new ByteArrayInputStream("hello".getBytes(UTF_8));

        StreamResponseUtil.StreamResponseData data = StreamResponseUtil.StreamResponseData.builder().stream(stream).type("text/plain")
                .fileName("example.txt").dispositionType(StreamResponseUtil.DISPOSITION_TYPE_ATTACHMENT).build();

        Response response = StreamResponseUtil.ok(data);

        String header = response.getHeaderString(HttpHeaders.CONTENT_DISPOSITION);

        assertTrue(header.contains("attachment"));
        assertTrue(header.contains("example.txt"));
    }

    @Test
    void okStreamingOutputWritesStreamData() throws Exception {

        byte[] content = "stream-content".getBytes(UTF_8);
        InputStream stream = new ByteArrayInputStream(content);

        StreamResponseUtil.StreamResponseData data = StreamResponseUtil.StreamResponseData.builder().stream(stream).type("text/plain")
                .build();

        Response response = StreamResponseUtil.ok(data);

        StreamingOutput streamingOutput = (StreamingOutput) response.getEntity();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamingOutput.write(out);

        assertArrayEquals(content, out.toByteArray());
    }

}
