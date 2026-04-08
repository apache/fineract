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
import static org.apache.fineract.infrastructure.contentstore.processor.DataUrlDecoderContentProcessor.DATA_URL_DECODE_RESULT_CONTENT_TYPE;
import static org.apache.fineract.infrastructure.contentstore.processor.DataUrlEncoderContentProcessor.DATA_URL_ENCODE_PARAM_CONTENT_TYPE;
import static org.apache.fineract.infrastructure.contentstore.processor.DataUrlEncoderContentProcessor.DATA_URL_ENCODE_PARAM_ENCODING;
import static org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor.IMAGE_RESIZE_PARAM_FORMAT;
import static org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor.IMAGE_RESIZE_PARAM_MAX_HEIGHT;
import static org.apache.fineract.infrastructure.contentstore.processor.ImageResizeContentProcessor.IMAGE_RESIZE_PARAM_MAX_WIDTH;
import static org.apache.fineract.infrastructure.contentstore.processor.SizeContentProcessor.SIZE_RESULT_VALUE;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.fineract.infrastructure.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
class ContentProcessorTest {

    @Autowired
    private Base64EncoderContentProcessor base64EncoderContentProcessor;

    @Autowired
    private Base64DecoderContentProcessor base64DecoderContentProcessor;

    @Autowired
    private DataUrlEncoderContentProcessor dataUrlEncoderContentProcessor;

    @Autowired
    private DataUrlDecoderContentProcessor dataUrlDecoderContentProcessor;

    @Autowired
    private ImageResizeContentProcessor imageResizeContentProcessor;

    @Autowired
    private SizeContentProcessor sizeContentProcessor;

    static final byte[] DATA_BASE64 = """
            iVBORw0KGgoAAAANSUhEUgAAANwAAADcCAYAAAAbWs+BAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJ
            bWFnZVJlYWR5ccllPAAAAodJREFUeNrs3NFtg0AQRVGPRRNWyoCeUhM9LWWkjUw6iIQyCRvmnAKQ
            F3S1/nqRmQ/gbzy9AhAcCA4QHAgOEBwIDgQHCA4EBwgOBAeCAwQHggMEB4IDBAeCg/tbvAJuJaJ2
            pCcz3HDgLyUgOBAcCA4QHAgOEBwIDhAcCA4EBwgOBAcIDgQHggMEB4IDTmm3aRKxlW5erOva6v2N
            sUft96jdIMndDQcIDgQHggMEB4IDBAeCA8EBggPBAYIDwQGCA8GB4ADBgeAAwcHFpt806bZBMvvv
            27b32g2SzOgUnBsOBAeCAwQHggMEB4IDwQGCA8EBggPBgeAAwYHgAMGB4ADBgeCggcjM2gcWb5BU
            m30zpNt5j+Mofd4Y+9QbKW44EBwIDhAcCA4QHAgOBAcIDgQHCA4EB4IDBAeCAwQHggMEB4KDBpZu
            B67e0Oi2GdJtE8YNB4IDBAeCA8EBggPBAYIDwQGCA8GB4ADBgeAAwYHgQHCA4EBwwGmRmbUPjLfa
            Bz5erT6IzZCfGWMPNxwgOBAcCA4QHAgOEBwIDgQHCA4EBwgOBAeC8wpAcCA4QHAgOEBwcKl/sGlS
            7eWr31jmsGkCCA4EB4IDBAeCAwQHggPBAYIDwQGCA8EBggPBgeAAwYHgAMHBtco3Tcp/4PQbKdV6
            ba7MvkHihgPBAYIDwYHgAMGB4ADBgeAAwYHgQHCA4EBwgOBAcCA4QHAgOOC06TdNyg8cz+IDd9sg
            +QjZuOFAcIDgQHAgOEBwIDhAcCA4EBwgOBAcIDgQHCA4EBwIDhAcCA74xtLtwJmfNjlww4HgAMGB
            4ADBgeBAcIDgQHCA4EBwgOBAcCA4QHAgOEBwIDgQHPB7vgQYAPVcRHV5+lfgAAAAAElFTkSuQmCC""".getBytes(UTF_8);

    private static final byte[] DATA_URL = """
            data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAANwAAADcCAYAAAAbWs+BAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJ
            bWFnZVJlYWR5ccllPAAAAodJREFUeNrs3NFtg0AQRVGPRRNWyoCeUhM9LWWkjUw6iIQyCRvmnAKQ
            F3S1/nqRmQ/gbzy9AhAcCA4QHAgOEBwIDgQHCA4EBwgOBAeCAwQHggMEB4IDBAeCg/tbvAJuJaJ2
            pCcz3HDgLyUgOBAcCA4QHAgOEBwIDhAcCA4EBwgOBAcIDgQHggMEB4IDTmm3aRKxlW5erOva6v2N
            sUft96jdIMndDQcIDgQHggMEB4IDBAeCA8EBggPBAYIDwQGCA8GB4ADBgeAAwcHFpt806bZBMvvv
            27b32g2SzOgUnBsOBAeCAwQHggMEB4IDwQGCA8EBggPBgeAAwYHgAMGB4ADBgeCggcjM2gcWb5BU
            m30zpNt5j+Mofd4Y+9QbKW44EBwIDhAcCA4QHAgOBAcIDgQHCA4EB4IDBAeCAwQHggMEB4KDBpZu
            B67e0Oi2GdJtE8YNB4IDBAeCA8EBggPBAYIDwQGCA8GB4ADBgeAAwYHgQHCA4EBwwGmRmbUPjLfa
            Bz5erT6IzZCfGWMPNxwgOBAcCA4QHAgOEBwIDgQHCA4EBwgOBAeC8wpAcCA4QHAgOEBwcKl/sGlS
            7eWr31jmsGkCCA4EB4IDBAeCAwQHggPBAYIDwQGCA8EBggPBgeAAwYHgAMHBtco3Tcp/4PQbKdV6
            ba7MvkHihgPBAYIDwYHgAMGB4ADBgeAAwYHgQHCA4EBwgOBAcCA4QHAgOOC06TdNyg8cz+IDd9sg
            +QjZuOFAcIDgQHAgOEBwIDhAcCA4EBwgOBAcIDgQHCA4EBwIDhAcCA74xtLtwJmfNjlww4HgAMGB
            4ADBgeBAcIDgQHCA4EBwgOBAcCA4QHAgOEBwIDgQHPB7vgQYAPVcRHV5+lfgAAAAAElFTkSuQmCC""".getBytes(UTF_8);

    @Test
    void process() {

        final var ctx = sizeContentProcessor.then(base64DecoderContentProcessor).process(new ByteArrayInputStream(DATA_BASE64));

        write(ctx, "process.png");

        String type = ctx.getResult(DATA_URL_DECODE_RESULT_CONTENT_TYPE);
        Long size = ctx.getResult(SIZE_RESULT_VALUE);

        log.info("Result: {} of size  {}", type, size);
    }

    @Test
    void dataUrlDecode() {
        final var ctx = dataUrlDecoderContentProcessor.then(base64DecoderContentProcessor).then(imageResizeContentProcessor)
                .then(sizeContentProcessor).process(new ContentProcessorContext(new ByteArrayInputStream(DATA_URL),
                        Map.of(IMAGE_RESIZE_PARAM_MAX_WIDTH, 110, IMAGE_RESIZE_PARAM_MAX_HEIGHT, 110, IMAGE_RESIZE_PARAM_FORMAT, "png")));

        write(ctx, "data-url-decode.png");

        String type = ctx.getResult(DATA_URL_DECODE_RESULT_CONTENT_TYPE);
        Long size = ctx.getResult(SIZE_RESULT_VALUE);

        log.info("Result: {} of size  {}", type, size);
    }

    @Test
    void dataUrlEncodePng() {
        final var ctx = imageResizeContentProcessor.then(base64EncoderContentProcessor).then(dataUrlEncoderContentProcessor)
                .process(new ContentProcessorContext(ContentProcessorTest.class.getClassLoader().getResourceAsStream("test.png"),
                        Map.of(IMAGE_RESIZE_PARAM_MAX_WIDTH, 110, IMAGE_RESIZE_PARAM_MAX_HEIGHT, 110, IMAGE_RESIZE_PARAM_FORMAT, "png",
                                DATA_URL_ENCODE_PARAM_CONTENT_TYPE, "image/png", DATA_URL_ENCODE_PARAM_ENCODING, "base64")));

        write(ctx, "data-url-encode-png.txt");

        String type = ctx.getResult(DATA_URL_DECODE_RESULT_CONTENT_TYPE);
        Long size = ctx.getResult(SIZE_RESULT_VALUE);

        log.info("Result: {} of size  {}", type, size);
    }

    @Test
    void dataUrlEncodeJpeg() {
        final var ctx = imageResizeContentProcessor.then(base64EncoderContentProcessor).then(dataUrlEncoderContentProcessor)
                .process(new ContentProcessorContext(ContentProcessorTest.class.getClassLoader().getResourceAsStream("test-large.jpg"),
                        Map.of(IMAGE_RESIZE_PARAM_MAX_WIDTH, 300, IMAGE_RESIZE_PARAM_MAX_HEIGHT, 300, IMAGE_RESIZE_PARAM_FORMAT, "jpg",
                                DATA_URL_ENCODE_PARAM_CONTENT_TYPE, "image/png", DATA_URL_ENCODE_PARAM_ENCODING, "base64")));

        write(ctx, "data-url-encode-jpg.txt");

        String type = ctx.getResult(DATA_URL_DECODE_RESULT_CONTENT_TYPE);
        Long size = ctx.getResult(SIZE_RESULT_VALUE);

        log.info("Result: {} of size  {}", type, size);
    }

    @Test
    void base64() {
        final var ctx = base64DecoderContentProcessor.then(sizeContentProcessor).process(new ByteArrayInputStream(DATA_BASE64));

        write(ctx, "base64.png");

        String type = ctx.getResult(DATA_URL_DECODE_RESULT_CONTENT_TYPE);
        Long size = ctx.getResult(SIZE_RESULT_VALUE);

        log.info("Result: {} of size  {}", type, size);
    }

    private void write(ContentProcessorContext ctx, String fileName) {
        try (var is = ctx.getInputStream()) {
            IOUtils.copy(is, new FileOutputStream("build/" + fileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
