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

import static com.github.romankh3.image.comparison.model.ImageComparisonState.MATCH;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.romankh3.image.comparison.ImageComparison;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.fineract.infrastructure.TestConfiguration;
import org.apache.fineract.infrastructure.contentstore.exception.ContentDetectorException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
class TikaContentDetectorTest {

    @Autowired
    private TikaContentDetector tikaContentDetector;

    @Test
    void detectByFileName() {
        var ctx = tikaContentDetector.detect(ContentDetectorContext.builder().fileName("test.png").build());

        assertEquals("png", ctx.getFormat());
        assertEquals(".png", ctx.getExtension());
        assertEquals("image/png", ctx.getMimeType());

        ctx = tikaContentDetector.detect(ContentDetectorContext.builder().fileName("test.jpg").build());

        assertEquals("jpg", ctx.getFormat());
        assertEquals(".jpg", ctx.getExtension());
        assertEquals("image/jpeg", ctx.getMimeType());
    }

    @Test
    void detectByInputStream() {
        var ctx = tikaContentDetector.detect(
                ContentDetectorContext.builder().inputStream(TikaContentDetectorTest.class.getClassLoader().getResourceAsStream("test.png"))
                        .inputStreamEnabled(true).build());

        assertEquals("png", ctx.getFormat());
        assertEquals(".png", ctx.getExtension());
        assertEquals("image/png", ctx.getMimeType());

        write(ctx); // make sure we can still consume the input stream

        ctx = tikaContentDetector.detect(
                ContentDetectorContext.builder().inputStream(TikaContentDetectorTest.class.getClassLoader().getResourceAsStream("test.jpg"))
                        .inputStreamEnabled(true).build());

        assertEquals("jpg", ctx.getFormat());
        assertEquals(".jpg", ctx.getExtension());
        assertEquals("image/jpeg", ctx.getMimeType());

        write(ctx);
    }

    @Test
    void reuseStream() throws IOException {
        var ctx = tikaContentDetector.detect(
                ContentDetectorContext.builder().inputStream(TikaContentDetectorTest.class.getClassLoader().getResourceAsStream("test.png"))
                        .inputStreamEnabled(true).build());

        // we can run the detection again with the same stream

        ctx = tikaContentDetector
                .detect(ContentDetectorContext.builder().inputStream(ctx.getInputStream()).inputStreamEnabled(true).build());

        assertEquals("image/png", ctx.getMimeType());

        IOUtils.copy(ctx.getInputStream(), new FileOutputStream("build/test.png"));

        try (var expectedIs = TikaContentDetectorTest.class.getClassLoader().getResourceAsStream("test.png");
                var actualIs = new FileInputStream("build/test.png")) {
            requireNonNull(expectedIs);

            var expectedImage = ImageIO.read(expectedIs);
            var actualImage = ImageIO.read(actualIs);

            var result = new ImageComparison(expectedImage, actualImage).setAllowingPercentOfDifferentPixels(0).compareImages();

            log.info("Image diff percentage: {}", result.getDifferencePercent());

            assertEquals(MATCH, result.getImageComparisonState(), "The images should be identical");
        }
    }

    @Test
    void illegalArguments() {
        var exception = assertThrows(ContentDetectorException.class, () -> {
            tikaContentDetector.detect(ContentDetectorContext.builder()
                    .inputStream(TikaContentDetectorTest.class.getClassLoader().getResourceAsStream("test.png")).build());
        });

        assertNotNull(exception);
    }

    private void write(ContentDetectorContext ctx) {
        try (var is = ctx.getInputStream()) {
            IOUtils.copy(is, new FileOutputStream("build/detector" + ctx.getExtension()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
