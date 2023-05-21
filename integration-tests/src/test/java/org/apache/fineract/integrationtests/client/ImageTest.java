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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.apache.fineract.client.services.ImagesApi;
import org.apache.fineract.client.util.Parts;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Headers;

/**
 * Integration Test for /images API.
 *
 * @author Michael Vorburger.ch
 */
@Slf4j
public class ImageTest extends IntegrationTest {

    final MultipartBody.Part testPart = createPart("michael.vorburger-crepes.jpg", "michael.vorburger-crepes.jpg", "image/jpeg");

    Long clientId = new ClientTest().getClientId();
    Long staffId = new StaffTest().getStaffId();

    @Test
    @Order(1)
    void create() {
        ok(fineract().images.create("staff", staffId, testPart));
        ok(fineract().images.create("clients", clientId, testPart));
    }

    @Test
    @Order(2)
    void getOriginalSize() throws IOException {
        ResponseBody r = ok(fineract().images.get("staff", staffId, 3505, 1972, null));
        assertThat(r.contentType()).isEqualTo(MediaType.get("text/plain"));
        String encodedImage = r.string();
        assertThat(encodedImage).startsWith("data:image/jpeg;base64,");
        assertThat(encodedImage).hasLength(2846549);
        assertThat(r.contentLength()).isEqualTo(-1);
    }

    @Test
    @Order(3)
    void getSmallerSize() throws IOException {
        ResponseBody r = ok(fineract().images.get("staff", staffId, 128, 128, null));
        assertThat(r.string()).hasLength(6591);
    }

    @Test
    @Order(4)
    void getBiggerSize() throws IOException {
        ResponseBody r = ok(fineract().images.get("staff", staffId, 9000, 6000, null));
        assertThat(r.string()).hasLength(2846549);
    }

    @Test
    @Order(5)
    void getInlineOctetOutput() throws IOException {
        // 3505x1972 is the exact original size of testFile
        Response<ResponseBody> r = okR(fineract().images.get("staff", staffId, 3505, 1972, "inline_octet"));
        try (ResponseBody body = r.body()) {
            assertThat(body.contentType()).isEqualTo(MediaType.get("image/jpeg"));
            assertThat(body.bytes().length).isEqualTo(testPart.body().contentLength());
            assertThat(body.contentLength()).isEqualTo(testPart.body().contentLength());
        }

        var staff = ok(fineract().staff.retrieveOne8(staffId));
        String expectedFileName = staff.getDisplayName() + "JPEG"; // without dot!
        assertThat(Parts.fileName(r)).hasValue(expectedFileName);
    }

    @Test
    @Order(6)
    void getOctetOutput() throws IOException {
        ResponseBody r = ok(fineract().images.get("staff", staffId, 3505, 1972, "octet"));
        assertThat(r.contentType()).isEqualTo(MediaType.get("image/jpeg"));
        assertThat(r.bytes().length).isEqualTo(testPart.body().contentLength());
        assertThat(r.contentLength()).isEqualTo(testPart.body().contentLength());
    }

    @Test
    @Order(7)
    void getAnotherOutput() throws IOException {
        ResponseBody r = ok(fineract().images.get("staff", staffId, 3505, 1972, "abcd"));
        assertThat(r.contentType()).isEqualTo(MediaType.get("text/plain"));
        assertThat(r.string()).startsWith("data:image/jpeg;base64,");
    }

    @Test
    @Order(8)
    void getText() throws IOException {
        ResponseBody r = ok(fineract().createService(ImagesApiWithHeadersForTest.class).getText("staff", staffId, 3505, 1972, null));
        assertThat(r.contentType()).isEqualTo(MediaType.get("text/plain"));
        assertThat(r.string()).startsWith("data:image/jpeg;base64,");
    }

    @Test
    @Order(9)
    void getBytes() throws IOException {
        ResponseBody r = ok(fineract().createService(ImagesApiWithHeadersForTest.class).getBytes("staff", staffId, 3505, 1972, null));
        assertThat(r.contentType()).isEqualTo(MediaType.get("image/jpeg"));
        assertThat(r.bytes().length).isEqualTo(testPart.body().contentLength());
    }

    @Test
    @Order(50)
    void update() {
        ok(fineract().images.update("staff", staffId, testPart));
    }

    @Test
    @Order(99)
    void delete() {
        ok(fineract().images.delete("staff", staffId));
        ok(fineract().images.delete("clients", clientId));
    }

    @Test
    @Order(100)
    void pathTraversalJsp() {
        final MultipartBody.Part part = createPart("image-text-wrong-content.jsp",
                "../../../../../../../../../../tmp/image-text-wrong-content.jsp", "image/gif");

        assertThat(part).isNotNull();

        Exception exception = assertThrows(Exception.class, () -> {
            ok(fineract().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a file that doesn't match the indicated content type: {}", exception.getMessage());
    }

    @Test
    @Order(101)
    void gifWithPngExtension() {
        final MultipartBody.Part part = createPart("image-gif-wrong-extension.png", "image-gif-wrong-extension.png", "image/png");

        assertThat(part).isNotNull();

        Exception exception = assertThrows(Exception.class, () -> {
            ok(fineract().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a gif by just renaming the file extension: {}", exception.getMessage());
    }

    @Test
    @Order(102)
    void gifImage() {
        final MultipartBody.Part part = createPart("image-gif-correct-extension.gif", "image-gif-correct-extension.gif", "image/png");

        assertThat(part).isNotNull();

        Exception exception = assertThrows(Exception.class, () -> {
            ok(fineract().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a gif it is not whitelisted: {}", exception.getMessage());
    }

    @Test
    @Order(103)
    void pathTraversalJpg() {
        final MultipartBody.Part part = createPart("michael.vorburger-crepes.jpg",
                "../../../../../../../../../../tmp/michael.vorburger-crepes.jpg", "image/jpeg");

        assertThat(part).isNotNull();

        Exception exception = assertThrows(Exception.class, () -> {
            ok(fineract().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a file with a forbidden name pattern: {}", exception.getMessage());
    }

    @Test
    @Order(104)
    void pathTraversalWithAbsolutePathJpg() {
        final MultipartBody.Part part = createPart("michael.vorburger-crepes.jpg", "../17/michael.vorburger-crepes.jpg", "image/jpeg");

        assertThat(part).isNotNull();

        Exception exception = assertThrows(Exception.class, () -> {
            ok(fineract().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a file with a forbidden name pattern: {}", exception.getMessage());
    }

    @Test
    @Order(105)
    void pathTraversalWithAbsolutePathJpg2() {
        final MultipartBody.Part part = createPart("michael.vorburger-crepes.jpg", "..//17//michael.vorburger-crepes.jpg", "image/jpeg");

        assertThat(part).isNotNull();

        Exception exception = assertThrows(Exception.class, () -> {
            ok(fineract().images.create("clients", clientId, part));
        });

        assertThat(exception).isNotNull();

        log.warn("Should not be able to upload a file with a forbidden name pattern: {}", exception.getMessage());
    }

    private MultipartBody.Part createPart(String fileResource, String fileName, String mediaType) {
        try {
            byte[] data = IOUtils.toByteArray(ImageTest.class.getClassLoader().getResourceAsStream(fileResource));
            RequestBody rb = RequestBody.create(data, MediaType.get(mediaType));
            return MultipartBody.Part.createFormData("file", fileName, rb);
        } catch (Exception e) {
            log.error("Error creating file part.", e);
        }

        return null;
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
