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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import feign.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.fineract.client.feign.FineractMultipartEncoder.MultipartData;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DocumentData;
import org.apache.fineract.client.util.FeignParts;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for /documents API.
 *
 * @author Michael Vorburger.ch
 */
class DocumentTest extends FeignIntegrationTest {

    final File testFile = Path.of(getClass().getResource("/michael.vorburger-crepes.jpg").getFile()).toFile();

    Long clientId = new ClientTest().getClientId();
    Long documentId;

    @Test
    @Order(1)
    void retrieveAllDocuments() {
        assertThat(ok(() -> fineractClient().documentsFixed().retrieveAllDocuments("clients", clientId))).isNotNull();
    }

    @Test
    @Order(2)
    void createDocument() throws IOException {
        String name = "Test";
        String description = "The Description";
        MultipartData multipartData = new MultipartData()
                .addFile("file", testFile.getName(), Files.readAllBytes(testFile.toPath()), "image/jpeg").addText("name", name)
                .addText("description", description);
        var response = ok(() -> fineractClient().documentsFixed().createDocument("clients", clientId, multipartData));
        assertThat(response.getResourceId()).isNotNull();
        assertThat(response.getResourceIdentifier()).isNotEmpty();
        documentId = response.getResourceId();
    }

    @Test
    @Order(3)
    void getDocument() {
        DocumentData doc = ok(() -> fineractClient().documentsFixed().getDocument("clients", clientId, documentId));
        assertThat(doc.getName()).isEqualTo("Test");
        assertThat(doc.getFileName()).isEqualTo(testFile.getName());
        assertThat(doc.getDescription()).isEqualTo("The Description");
        assertThat(doc.getId()).isEqualTo(documentId);
        assertThat(doc.getParentEntityType()).isEqualTo("clients");
        assertThat(doc.getParentEntityId()).isEqualTo(clientId);
        // TODO FINERACT-1251 It's more than uploaded file; seems like a bug - it's including create body, not just file
        // size. The exact excess depends on the multipart boundary, which the Feign encoder picks at random.
        assertThat(doc.getSize()).isGreaterThan(testFile.length());
        assertThat(doc.getType()).isEqualTo("image/jpeg");
        // TODO doc.getStorageType() shouldn't be exposed by the API?!
    }

    @Test
    @Order(4)
    void downloadFile() throws IOException {
        Response r = fineractClient().documentsFixed().downloadFile("clients", clientId, documentId);
        try (InputStream body = r.body().asInputStream()) {
            assertThat(r.status()).isEqualTo(200);
            assertThat(String.join(",", r.headers().get("Content-Type"))).isEqualTo("image/jpeg");
            assertThat(body.readAllBytes().length).isEqualTo(testFile.length());
            // NOTE: now that everything is properly streamed and NOT loaded into memory the framework (Jersey) uses
            // chunked encoding to serve dynamic aka large content; this is more efficient and outweighs the presenće of
            // this information beforehand; the user can always count bytes after the download of the content; just to
            // say: this here is a feature and intentional
            // assertThat(body.contentLength()).isEqualTo(testFile.length());
        }
        assertThat(FeignParts.fileName(r)).hasValue(testFile.getName());
    }

    @Test
    @Order(10)
    void updateDocumentWithoutNewUpload() {
        String newName = "Test changed name";
        String newDescription = getClass().getName();
        ok(() -> fineractClient().documentsFixed().updateDocument("clients", clientId, documentId,
                new MultipartData().addText("name", newName).addText("description", newDescription)));

        DocumentData doc = ok(() -> fineractClient().documentsFixed().getDocument("clients", clientId, documentId));
        assertThat(doc.getName()).isEqualTo(newName);
        assertThat(doc.getDescription()).isEqualTo(newDescription);
        // TODO FINERACT-1251 It's more than uploaded file; seems like a bug - it's including create body, not just file
        // size
        assertThat(doc.getSize()).isGreaterThan(testFile.length());
    }

    @Test
    @Order(99)
    void deleteDocument() {
        ok(() -> fineractClient().documentsFixed().deleteDocument("clients", clientId, documentId));
        CallFailedRuntimeException exception = fail(() -> fineractClient().documentsFixed().getDocument("clients", clientId, documentId));
        assertEquals(404, exception.getStatus());
    }

    @Order(9999)
    @Test // FINERACT-1036
    void createDocumentBadArgs() {
        CallFailedRuntimeException exception = fail(
                () -> fineractClient().documentsFixed().createDocument("clients", 123L, new MultipartData().addText("name", "test.pdf")));
        assertEquals(400, exception.getStatus());
    }
}
