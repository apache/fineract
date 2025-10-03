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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import feign.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.apache.fineract.client.feign.FeignException;
import org.apache.fineract.client.feign.FineractMultipartEncoder;
import org.apache.fineract.client.models.DocumentData;
import org.apache.fineract.client.models.PostEntityTypeEntityIdDocumentsResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeignDocumentTest extends FeignIntegrationTest {

    final File testFile = new File(getClass().getResource("/michael.vorburger-crepes.jpg").getFile());

    Long clientId;
    Long documentId;

    @Test
    @Order(1)
    void setupClient() {
        FeignClientHelper clientHelper = new FeignClientHelper(fineractClient());
        clientId = clientHelper.createClient("Feign", "Test");
        assertThat(clientId).isNotNull();
    }

    @Test
    @Order(2)
    void testCreateDocument() throws IOException {
        String name = "Feign Test Document";
        String description = "Testing DocumentsApiFixed with Feign client";

        byte[] fileData = Files.readAllBytes(testFile.toPath());
        FineractMultipartEncoder.MultipartData multipartData = new FineractMultipartEncoder.MultipartData()
                .addFile("file", testFile.getName(), fileData, "image/jpeg").addText("name", name).addText("description", description);

        PostEntityTypeEntityIdDocumentsResponse response = ok(
                () -> fineractClient().documentsFixed().createDocument("clients", clientId, multipartData));

        assertThat(response).isNotNull();
        assertThat(response.getResourceId()).isNotNull();
        documentId = response.getResourceId();
    }

    @Test
    @Order(3)
    void testRetrieveAllDocuments() {
        var documents = ok(() -> fineractClient().documentsFixed().retrieveAllDocuments("clients", clientId));

        assertThat(documents).isNotNull();
        assertThat(documents).isNotEmpty();
    }

    @Test
    @Order(4)
    void testGetDocument() {
        DocumentData doc = ok(() -> fineractClient().documentsFixed().getDocument("clients", clientId, documentId));

        assertThat(doc).isNotNull();
        assertThat(doc.getName()).isEqualTo("Feign Test Document");
        assertThat(doc.getFileName()).isEqualTo(testFile.getName());
        assertThat(doc.getDescription()).isEqualTo("Testing DocumentsApiFixed with Feign client");
        assertThat(doc.getId()).isEqualTo(documentId);
        assertThat(doc.getParentEntityType()).isEqualTo("clients");
        assertThat(doc.getParentEntityId()).isEqualTo(clientId);
        assertThat(doc.getType()).isEqualTo("image/jpeg");
    }

    @Test
    @Order(5)
    void testDownloadFile() throws IOException {
        Response response = fineractClient().documentsFixed().downloadFile("clients", clientId, documentId);

        assertNotNull(response);
        assertEquals(200, response.status());

        try (InputStream inputStream = response.body().asInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            assertThat(bytes.length).isEqualTo((int) testFile.length());
        }
    }

    @Test
    @Order(6)
    void testUpdateDocument() {
        String newName = "Updated Feign Test";
        String newDescription = "Updated via Feign client";

        FineractMultipartEncoder.MultipartData multipartData = new FineractMultipartEncoder.MultipartData().addText("name", newName)
                .addText("description", newDescription);

        var updateResponse = ok(() -> fineractClient().documentsFixed().updateDocument("clients", clientId, documentId, multipartData));

        assertThat(updateResponse).isNotNull();

        DocumentData doc = ok(() -> fineractClient().documentsFixed().getDocument("clients", clientId, documentId));
        assertThat(doc.getName()).isEqualTo(newName);
        assertThat(doc.getDescription()).isEqualTo(newDescription);
    }

    @Test
    @Order(99)
    void testDeleteDocument() {
        var deleteResponse = ok(() -> fineractClient().documentsFixed().deleteDocument("clients", clientId, documentId));
        assertThat(deleteResponse).isNotNull();

        FeignException exception = assertThrows(FeignException.class,
                () -> fineractClient().documentsFixed().getDocument("clients", clientId, documentId));
        assertEquals(404, exception.status());
    }

    @Test
    @Order(9999)
    void testCreateDocumentBadArgs() {
        FineractMultipartEncoder.MultipartData multipartData = new FineractMultipartEncoder.MultipartData().addText("name", "test.pdf");

        FeignException exception = assertThrows(FeignException.class,
                () -> fineractClient().documentsFixed().createDocument("clients", clientId, multipartData));
        assertEquals(400, exception.status());
    }
}
