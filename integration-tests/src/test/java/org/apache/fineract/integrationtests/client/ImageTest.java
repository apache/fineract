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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.romankh3.image.comparison.ImageComparison;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.fineract.client.services.ImagesApi;
import org.apache.fineract.client.util.Parts;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Integration Test for /images API.
 *
 * @author Michael Vorburger.ch
 */
@Slf4j
class ImageTest extends IntegrationTest {

    static final String TEST_RESOURCE = "michael.vorburger-crepes.jpg";
    static final int TEST_IMAGE_DIFF_PERCENTAGE = 2;

    final MultipartBody.Part testPart = createPart(TEST_RESOURCE, TEST_RESOURCE, "image/jpeg");

    Long clientId = new ClientTest().getClientId();
    Long staffId = new StaffTest().getStaffId();

    @Test
    @Order(1)
    void create() {
        ok(fineractClient().images.create("staff", staffId, testPart));
        ok(fineractClient().images.create("clients", clientId, testPart));
    }

    @Test
    @Order(2)
    void getOriginalSize() throws IOException {
        var r = ok(fineractClient().images.get("staff", staffId, 3505, 1972, null));
        assertThat(r.contentType()).isEqualTo(MediaType.get("text/plain"));
        var encodedImage = r.string();
        assertThat(encodedImage).startsWith("data:image/jpeg;base64,");
        assertThat(r.contentLength()).isEqualTo(-1);
        assertImage(encodedImage);
    }

    @Test
    @Order(3)
    void getSmallerSize() throws IOException {
        var r = ok(fineractClient().images.get("staff", staffId, 128, 128, null));
        assertThat(r.string()).hasSize(7251);
    }

    @Test
    @Order(4)
    void getBiggerSize() throws IOException {
        var r = ok(fineractClient().images.get("staff", staffId, 9000, 6000, null));
        assertImage(r.string());
    }

    @Test
    @Order(5)
    void getInlineOctetOutput() throws IOException {
        // 3505x1972 is the exact original size of testFile
        var r = okR(fineractClient().images.get("staff", staffId, 3505, 1972, "inline_octet"));
        try (var body = r.body()) {
            assertThat(body.contentType()).isEqualTo(MediaType.get("image/jpeg"));
            assertImage(body);
        }

        var staff = ok(fineractClient().staff.retrieveOne8(staffId));
        assertThat(Parts.fileName(r)).hasValue(staff.getDisplayName());
    }

    @Test
    @Order(6)
    void getOctetOutput() throws IOException {
        var r = ok(fineractClient().images.get("staff", staffId, 3505, 1972, "octet"));
        assertThat(r.contentType()).isEqualTo(MediaType.get("image/jpeg"));
        // NOTE: content length is not a reliable criteria; the server removes metadata (see it as a security feature)
        // which makes the file immediately only half the size, but pixel wise the images are still the same
        assertImage(r);
    }

    @Test
    @Order(7)
    void getAnotherOutput() throws IOException {
        var r = ok(fineractClient().images.get("staff", staffId, 3505, 1972, "abcd"));
        assertThat(r.contentType()).isEqualTo(MediaType.get("text/plain"));
        var content = r.string();
        assertThat(content).startsWith("data:image/jpeg;base64,");
        assertImage(content);
    }

    @Test
    @Order(8)
    void getText() throws IOException {
        var r = ok(fineractClient().createService(ImagesApiWithHeadersForTest.class).getText("staff", staffId, 3505, 1972, null));
        assertThat(r.contentType()).isEqualTo(MediaType.get("text/plain"));
        assertThat(r.string()).startsWith("data:image/jpeg;base64,");
    }

    @Test
    @Order(9)
    void getBytes() throws IOException {
        var r = ok(fineractClient().createService(ImagesApiWithHeadersForTest.class).getBytes("staff", staffId, 3505, 1972, null));
        assertThat(r.contentType()).isEqualTo(MediaType.get("image/jpeg"));
        assertImage(r.bytes());
    }

    @Test
    @Order(50)
    void update() {
        ok(fineractClient().images.update("staff", staffId, testPart));
    }

    @Test
    @Order(99)
    void delete() {
        ok(fineractClient().images.delete("staff", staffId));
        ok(fineractClient().images.delete("clients", clientId));
    }

    @Test
    @Order(100)
    void pathTraversalJsp() {
        final var part = createPart("image-text-wrong-content.jsp", "../../../../../../../../../../tmp/image-text-wrong-content.jsp",
                "image/gif");

        assertThat(part).isNotNull();

        var exception = assertThrows(Exception.class, () -> {
            ok(fineractClient().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a file that doesn't match the indicated content type: {}", exception.getMessage());
    }

    @Test
    @Order(101)
    void gifWithPngExtension() {
        final var part = createPart("image-gif-wrong-extension.png", "image-gif-wrong-extension.png", "image/png");

        assertThat(part).isNotNull();

        var exception = assertThrows(Exception.class, () -> {
            ok(fineractClient().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a gif by just renaming the file extension: {}", exception.getMessage());
    }

    @Test
    @Order(102)
    void gifImage() {
        final var part = createPart("image-gif-correct-extension.gif", "image-gif-correct-extension.gif", "image/png");

        assertThat(part).isNotNull();

        var exception = assertThrows(Exception.class, () -> {
            ok(fineractClient().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a gif it is not whitelisted: {}", exception.getMessage());
    }

    @Test
    @Order(103)
    void pathTraversalJpg() {
        final var part = createPart("michael.vorburger-crepes.jpg", "../../../../../../../../../../tmp/michael.vorburger-crepes.jpg",
                "image/jpeg");

        assertThat(part).isNotNull();

        var exception = assertThrows(Exception.class, () -> {
            ok(fineractClient().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a file with a forbidden name pattern: {}", exception.getMessage());
    }

    @Test
    @Order(104)
    void pathTraversalWithAbsolutePathJpg() {
        create();
        final var part = createPart("michael.vorburger-crepes.jpg", "../17/michael.vorburger-crepes.jpg", "image/jpeg");

        assertThat(part).isNotNull();

        var exception = assertThrows(Exception.class, () -> {
            ok(fineractClient().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a file with a forbidden name pattern: {}", exception.getMessage());
    }

    @Test
    @Order(105)
    void pathTraversalWithAbsolutePathJpg2() {
        final var part = createPart("michael.vorburger-crepes.jpg", "..//17//michael.vorburger-crepes.jpg", "image/jpeg");

        assertThat(part).isNotNull();

        var exception = assertThrows(Exception.class, () -> {
            ok(fineractClient().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a file with a forbidden name pattern: {}", exception.getMessage());
    }

    private MultipartBody.Part createPart(String fileResource, String fileName, String mediaType) {
        try {
            byte[] data = IOUtils.toByteArray(ImageTest.class.getClassLoader().getResourceAsStream(fileResource));
            var rb = RequestBody.create(data, MediaType.get(mediaType));
            return MultipartBody.Part.createFormData("file", fileName, rb);
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

    private void assertImage(ResponseBody r) throws IOException {
        assertImage(r.bytes(), TEST_IMAGE_DIFF_PERCENTAGE);
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

    interface ImagesApiWithHeadersForTest extends ImagesApi {

        @Headers("Accept: text/plain")
        @GET("v1/{entityType}/{entityId}/images")
        Call<ResponseBody> getText(@retrofit2.http.Path("entityType") String entityType, @retrofit2.http.Path("entityId") Long entityId,
                @retrofit2.http.Query("maxWidth") Integer maxWidth, @retrofit2.http.Query("maxHeight") Integer maxHeight,
                @retrofit2.http.Query("output") String output);

        @Headers("Accept: application/octet-stream")
        @GET("v1/{entityType}/{entityId}/images")
        Call<ResponseBody> getBytes(@retrofit2.http.Path("entityType") String entityType, @retrofit2.http.Path("entityId") Long entityId,
                @retrofit2.http.Query("maxWidth") Integer maxWidth, @retrofit2.http.Query("maxHeight") Integer maxHeight,
                @retrofit2.http.Query("output") String output);
    }
}
