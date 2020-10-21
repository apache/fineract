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
package org.apache.fineract.integrationtests.newstyle;

import java.io.File;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
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
public class ImageTest extends IntegrationTest {

    // TODO This "new style" test is equivalent to the old StaffImageApiTest, so we could delete that (after
    // FINERACT-1209)

    final File testImage = new File(getClass().getResource("/michael.vorburger-crepes.jpg").getFile());

    Long clientId = new ClientTest().getClientId();
    // staffId is hard-coded to 1L below, because that always exists

    @Test
    @Order(1)
    void create() {
        ok(fineract().images.create("staff", 1L, Parts.fromFile(testImage)));
        ok(fineract().images.create("clients", clientId, Parts.fromFile(testImage)));
    }

    @Test
    @Order(2)
    void getOriginalSize() throws IOException {
        ResponseBody r = ok(fineract().images.get("staff", 1L, 3505, 1972, null));
        assertThat(r.contentType()).isEqualTo(MediaType.get("text/plain"));
        String encodedImage = r.string();
        assertThat(encodedImage).startsWith("data:image/jpeg;base64,");
        assertThat(encodedImage).hasLength(2846549);
        assertThat(r.contentLength()).isEqualTo(-1);
    }

    @Test
    @Order(3)
    void getSmallerSize() throws IOException {
        ResponseBody r = ok(fineract().images.get("staff", 1L, 128, 128, null));
        assertThat(r.string()).hasLength(6591);
    }

    @Test
    @Order(4)
    void getBiggerSize() throws IOException {
        ResponseBody r = ok(fineract().images.get("staff", 1L, 9000, 6000, null));
        assertThat(r.string()).hasLength(2846549);
    }

    @Test
    @Order(5)
    void getInlineOctetOutput() throws IOException {
        // 3505x1972 is the exact original size of testFile
        ResponseBody r = ok(fineract().images.get("staff", 1L, 3505, 1972, "inline_octet"));
        assertThat(r.contentType()).isEqualTo(MediaType.get("image/jpeg"));
        assertThat(r.bytes().length).isEqualTo(testImage.length());
        assertThat(r.contentLength()).isEqualTo(testImage.length());
    }

    @Test
    @Order(6)
    void getOctetOutput() throws IOException {
        ResponseBody r = ok(fineract().images.get("staff", 1L, 3505, 1972, "octet"));
        assertThat(r.contentType()).isEqualTo(MediaType.get("image/jpeg"));
        assertThat(r.bytes().length).isEqualTo(testImage.length());
        assertThat(r.contentLength()).isEqualTo(testImage.length());
    }

    @Test
    @Order(7)
    void getAnotherOutput() throws IOException {
        ResponseBody r = ok(fineract().images.get("staff", 1L, 3505, 1972, "abcd"));
        assertThat(r.contentType()).isEqualTo(MediaType.get("text/plain"));
        assertThat(r.string()).startsWith("data:image/jpeg;base64,");
    }

    @Test
    @Order(8)
    void getText() throws IOException {
        ResponseBody r = ok(fineract().createService(ImagesApiWithHeadersForTest.class).getText("staff", 1L, 3505, 1972, null));
        assertThat(r.contentType()).isEqualTo(MediaType.get("text/plain"));
        assertThat(r.string()).startsWith("data:image/jpeg;base64,");
    }

    @Test
    @Order(9)
    void getBytes() throws IOException {
        ResponseBody r = ok(fineract().createService(ImagesApiWithHeadersForTest.class).getBytes("staff", 1L, 3505, 1972, null));
        assertThat(r.contentType()).isEqualTo(MediaType.get("image/jpeg"));
        assertThat(r.bytes().length).isEqualTo(testImage.length());
    }

    @Test
    @Order(50)
    void update() {
        ok(fineract().images.update("staff", 1L, Parts.fromFile(testImage)));
    }

    @Test
    @Order(99)
    void delete() {
        ok(fineract().images.delete("staff", 1L));
        ok(fineract().images.delete("clients", clientId));
    }

    interface ImagesApiWithHeadersForTest extends ImagesApi {

        @Headers("Accept: text/plain")
        @GET("{entityType}/{entityId}/images")
        Call<ResponseBody> getText(@retrofit2.http.Path("entityType") String entityType, @retrofit2.http.Path("entityId") Long entityId,
                @retrofit2.http.Query("maxWidth") Integer maxWidth, @retrofit2.http.Query("maxHeight") Integer maxHeight,
                @retrofit2.http.Query("output") String output);

        @Headers("Accept: application/octet-stream")
        @GET("{entityType}/{entityId}/images")
        Call<ResponseBody> getBytes(@retrofit2.http.Path("entityType") String entityType, @retrofit2.http.Path("entityId") Long entityId,
                @retrofit2.http.Query("maxWidth") Integer maxWidth, @retrofit2.http.Query("maxHeight") Integer maxHeight,
                @retrofit2.http.Query("output") String output);
    }
}
