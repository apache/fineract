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

import java.io.File;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.MultipartBody.Part;
import okhttp3.ResponseBody;
import org.apache.fineract.client.models.GetEntityTypeEntityIdDocumentsResponse;
import org.apache.fineract.client.util.Parts;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

/**
 * Integration Test for /documents API.
 *
 * @author Michael Vorburger.ch
 */
public class DocumentTest extends IntegrationTest {

    final File testFile = new File(getClass().getResource("/michael.vorburger-crepes.jpg").getFile());

    Long clientId = new ClientTest().getClientId();
    Long documentId;

    @Test
    @Order(1)
    void retrieveAllDocuments() {
        assertThat(ok(fineract().documents.retrieveAllDocuments("clients", clientId))).isNotNull();
    }

    @Test
    @Order(2)
    void createDocument() {
        String name = "Test";
        Part part = Parts.fromFile(testFile);
        String description = "The Description";
        var response = ok(fineract().documents.createDocument("clients", clientId, part, name, description));
        assertThat(response.getResourceId()).isNotNull();
        assertThat(response.getResourceIdentifier()).isNotEmpty();
        documentId = response.getResourceId();
    }

    @Test
    @Order(3)
    void getDocument() {
        GetEntityTypeEntityIdDocumentsResponse doc = ok(fineract().documents.getDocument("clients", clientId, documentId));
        assertThat(doc.getName()).isEqualTo("Test");
        assertThat(doc.getFileName()).isEqualTo(testFile.getName());
        assertThat(doc.getDescription()).isEqualTo("The Description");
        assertThat(doc.getId()).isEqualTo(documentId);
        assertThat(doc.getParentEntityType()).isEqualTo("clients");
        assertThat(doc.getParentEntityId()).isEqualTo(clientId);
        // TODO FINERACT-1251 It's more than uploaded file; seems like a bug - it's including create body, not just file
        // size
        assertThat(doc.getSize()).isEqualTo(testFile.length() + 618);
        assertThat(doc.getType()).isEqualTo("image/jpeg");
        // TODO doc.getStorageType() shouldn't be exposed by the API?!
    }

    @Test
    @Order(4)
    void downloadFile() throws IOException {
        Response<ResponseBody> r = okR(fineract().documents.downloadFile("clients", clientId, documentId));
        try (ResponseBody body = r.body()) {
            assertThat(body.contentType()).isEqualTo(MediaType.get("image/jpeg"));
            assertThat(body.bytes().length).isEqualTo(testFile.length());
            assertThat(body.contentLength()).isEqualTo(testFile.length());
        }
        assertThat(Parts.fileName(r)).hasValue(testFile.getName());
    }

    @Test
    @Order(10)
    void updateDocumentWithoutNewUpload() {
        String newName = "Test changed name";
        String newDescription = getClass().getName();
        ok(fineract().documents.updateDocument("clients", clientId, documentId, null, newName, newDescription));

        GetEntityTypeEntityIdDocumentsResponse doc = ok(fineract().documents.getDocument("clients", clientId, documentId));
        assertThat(doc.getName()).isEqualTo(newName);
        assertThat(doc.getDescription()).isEqualTo(newDescription);
        // TODO FINERACT-1251 It's more than uploaded file; seems like a bug - it's including create body, not just file
        // size
        assertThat(doc.getSize()).isEqualTo(testFile.length() + 618);
    }

    @Test
    @Order(99)
    void deleteDocument() {
        ok(fineract().documents.deleteDocument("clients", clientId, documentId));
        assertThat(fineract().documents.getDocument("clients", clientId, documentId)).hasHttpStatus(404);
    }

    @Order(9999)
    @Test // FINERACT-1036
    void createDocumentBadArgs() {
        assertThat(fineract().documents.createDocument("clients", 123L, null, "test.pdf", null)).hasHttpStatus(400);
    }
}
