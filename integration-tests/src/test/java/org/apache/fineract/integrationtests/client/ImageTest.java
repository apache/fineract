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
package org.apache.fineract.integrationtests.client;

import static com.github.romankh3.image.comparison.model.ImageComparisonState.MATCH;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.romankh3.image.comparison.ImageComparison;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.fineract.client.feign.FineractMultipartEncoder.MultipartData;
import org.apache.fineract.client.util.FeignParts;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for /images API.
 *
 * @author Michael Vorburger.ch
 */
@Slf4j
class ImageTest extends FeignIntegrationTest {

    static final String TEST_RESOURCE = "michael.vorburger-crepes.jpg";
    static final int TEST_IMAGE_DIFF_PERCENTAGE = 2;
    static final int REJECTED_UPLOAD_STATUS = 403;
    static final String REJECTED_UPLOAD_CODE = "error.msg.document.request.invalid";

    final MultipartData testPart = createPart(TEST_RESOURCE, TEST_RESOURCE, "image/jpeg");

    Long clientId = new ClientTest().getClientId();
    Long staffId = new StaffTest().getStaffId();

    @Test
    @Order(1)
    void create() {
        assertSuccess(fineractClient().images().create("staff", staffId, testPart));
        assertSuccess(fineractClient().images().create("clients", clientId, testPart));
    }

    @Test
    @Order(2)
    void getOriginalSize() throws IOException {
        try (Response r = getImage(3505, 1972, null)) {
            assertContentType(r, "text/plain");
            var encodedImage = bodyAsString(r);
            assertThat(encodedImage).startsWith("data:image/jpeg;base64,");
            assertThat(r.body().length()).isNull();
            assertImage(encodedImage);
        }
    }

    @Test
    @Order(3)
    void getSmallerSize() throws IOException {
        try (Response r = getImage(128, 128, null)) {
            assertThat(bodyAsString(r)).hasSize(7067);
        }
    }

    @Test
    @Order(4)
    void getBiggerSize() throws IOException {
        try (Response r = getImage(9000, 6000, null)) {
            assertImage(bodyAsString(r));
        }
    }

    @Test
    @Order(5)
    void getInlineOctetOutput() throws IOException {
        // 3505x1972 is the exact original size of testFile
        try (Response r = getImage(3505, 1972, "inline_octet")) {
            assertContentType(r, "image/jpeg");
            assertImage(bodyAsBytes(r));

            var staff = ok(() -> fineractClient().staff().retrieveOneStaff(staffId));
            assertThat(FeignParts.fileName(r)).hasValue(staff.getDisplayName());
        }
    }

    @Test
    @Order(6)
    void getOctetOutput() throws IOException {
        try (Response r = getImage(3505, 1972, "octet")) {
            assertContentType(r, "image/jpeg");
            // NOTE: content length is not a reliable criteria; the server removes metadata (see it as a security
            // feature) which makes the file immediately only half the size, but pixel wise the images are still the
            // same
            assertImage(bodyAsBytes(r));
        }
    }

    @Test
    @Order(7)
    void getAnotherOutput() throws IOException {
        try (Response r = getImage(3505, 1972, "abcd")) {
            assertContentType(r, "text/plain");
            var content = bodyAsString(r);
            assertThat(content).startsWith("data:image/jpeg;base64,");
            assertImage(content);
        }
    }

    @Test
    @Order(8)
    void getText() throws IOException {
        try (Response r = fineractClient().create(ImagesApiWithHeadersForTest.class).getText("staff", staffId, 3505, 1972)) {
            assertContentType(r, "text/plain");
            assertThat(bodyAsString(r)).startsWith("data:image/jpeg;base64,");
        }
    }

    @Test
    @Order(9)
    void getBytes() throws IOException {
        try (Response r = fineractClient().create(ImagesApiWithHeadersForTest.class).getBytes("staff", staffId, 3505, 1972)) {
            assertContentType(r, "image/jpeg");
            assertImage(bodyAsBytes(r));
        }
    }

    @Test
    @Order(50)
    void update() {
        assertSuccess(fineractClient().images().update("staff", staffId, testPart));
    }

    @Test
    @Order(99)
    void delete() {
        assertSuccess(fineractClient().images().delete("staff", staffId));
        assertSuccess(fineractClient().images().delete("clients", clientId));
    }

    @Test
    @Order(100)
    void pathTraversalJsp() {
        final var part = createPart("image-text-wrong-content.jsp", "../../../../../../../../../../tmp/image-text-wrong-content.jsp",
                "image/gif");

        assertThat(part).isNotNull();

        try (Response response = fineractClient().images().create("clients", clientId, part)) {
            assertRejected(response, "Should not be able to upload a file that doesn't match the indicated content type");
        }
    }

    @Test
    @Order(101)
    void gifWithPngExtension() {
        final var part = createPart("image-gif-wrong-extension.png", "image-gif-wrong-extension.png", "image/png");

        assertThat(part).isNotNull();

        try (Response response = fineractClient().images().create("clients", clientId, part)) {
            assertRejected(response, "Should not be able to upload a gif by just renaming the file extension");
        }
    }

    @Test
    @Order(102)
    void gifImage() {
        final var part = createPart("image-gif-correct-extension.gif", "image-gif-correct-extension.gif", "image/png");

        assertThat(part).isNotNull();

        try (Response response = fineractClient().images().create("clients", clientId, part)) {
            assertRejected(response, "Should not be able to upload a gif it is not whitelisted");
        }
    }

    @Test
    @Order(103)
    void pathTraversalJpg() {
        final var part = createPart("michael.vorburger-crepes.jpg", "../../../../../../../../../../tmp/michael.vorburger-crepes.jpg",
                "image/jpeg");

        assertThat(part).isNotNull();

        try (Response response = fineractClient().images().create("clients", clientId, part)) {
            assertRejected(response, "Should not be able to upload a file with a forbidden name pattern");
        }
    }

    @Test
    @Order(104)
    void pathTraversalWithAbsolutePathJpg() {
        create();
        final var part = createPart("michael.vorburger-crepes.jpg", "../17/michael.vorburger-crepes.jpg", "image/jpeg");

        assertThat(part).isNotNull();

        try (Response response = fineractClient().images().create("clients", clientId, part)) {
            assertRejected(response, "Should not be able to upload a file with a forbidden name pattern");
        }
    }

    @Test
    @Order(105)
    void pathTraversalWithAbsolutePathJpg2() {
        final var part = createPart("michael.vorburger-crepes.jpg", "..//17//michael.vorburger-crepes.jpg", "image/jpeg");

        assertThat(part).isNotNull();

        try (Response response = fineractClient().images().create("clients", clientId, part)) {
            assertRejected(response, "Should not be able to upload a file with a forbidden name pattern");
        }
    }

    private MultipartData createPart(String fileResource, String fileName, String mediaType) {
        try {
            byte[] data = ImageTest.class.getClassLoader().getResourceAsStream(fileResource).readAllBytes();
            return new MultipartData().addFile("file", fileName, data, mediaType);
        } catch (Exception e) {
            log.error("Error creating file part.", e);
        }

        return null;
    }

    private void assertImage(String content) {
        assertImage(content, TEST_IMAGE_DIFF_PERCENTAGE);
    }

    private void assertImage(String content, double diffPercent) {
        if (content.contains(",")) {
            content = content.substring(content.indexOf(",") + 1);
        }
        assertImage(new Base64().decode(content), diffPercent);
    }

    private Response getImage(int maxWidth, int maxHeight, String output) {
        Map<String, Object> queryParams = output == null ? Map.of("maxWidth", maxWidth, "maxHeight", maxHeight)
                : Map.of("maxWidth", maxWidth, "maxHeight", maxHeight, "output", output);
        Response response = fineractClient().images().get("staff", staffId, queryParams);
        assertThat(response.status()).isEqualTo(200);
        return response;
    }

    private static void assertSuccess(Response response) {
        try (response) {
            assertThat(response.status()).isEqualTo(200);
        }
    }

    private static void assertRejected(Response response, String reason) {
        String body = bodyAsString(response);
        assertThat(response.status()).as(reason).isEqualTo(REJECTED_UPLOAD_STATUS);
        assertThat(body).as(reason).contains(REJECTED_UPLOAD_CODE);
        log.warn("{}: {}", reason, body);
    }

    private static void assertContentType(Response response, String expected) {
        assertThat(String.join(",", response.headers().get("Content-Type"))).isEqualTo(expected);
    }

    private static String bodyAsString(Response response) {
        return new String(bodyAsBytes(response), StandardCharsets.UTF_8);
    }

    private static byte[] bodyAsBytes(Response response) {
        try (InputStream body = response.body().asInputStream()) {
            return body.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void assertImage(byte[] data) {
        assertImage(data, TEST_IMAGE_DIFF_PERCENTAGE);
    }

    private void assertImage(byte[] data, double diffPercent) {
        try (var resource = ImageTest.class.getClassLoader().getResourceAsStream(TEST_RESOURCE)) {
            requireNonNull(resource);

            var expectedImage = ImageIO.read(resource);
            var actualImage = ImageIO.read(new ByteArrayInputStream(data));

            var result = new ImageComparison(expectedImage, actualImage).setAllowingPercentOfDifferentPixels(diffPercent).compareImages();
            // result.writeResultTo(new File("build/diff.png"));

            log.info("Image diff percentage: {}", result.getDifferencePercent());

            assertEquals(MATCH, result.getImageComparisonState(), "The images should be identical");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The server picks the image representation from the Accept header. {@code ImagesApi.get} lets the client default
     * it to JSON, so these two pin it explicitly.
     */
    interface ImagesApiWithHeadersForTest {

        @Headers("Accept: text/plain")
        @RequestLine("GET /v1/{entityType}/{entityId}/images?maxWidth={maxWidth}&maxHeight={maxHeight}")
        Response getText(@Param("entityType") String entityType, @Param("entityId") Long entityId, @Param("maxWidth") Integer maxWidth,
                @Param("maxHeight") Integer maxHeight);

        @Headers("Accept: application/octet-stream")
        @RequestLine("GET /v1/{entityType}/{entityId}/images?maxWidth={maxWidth}&maxHeight={maxHeight}")
        Response getBytes(@Param("entityType") String entityType, @Param("entityId") Long entityId, @Param("maxWidth") Integer maxWidth,
                @Param("maxHeight") Integer maxHeight);
    }
}
