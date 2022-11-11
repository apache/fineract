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
package org.apache.fineract.infrastructure.creditbureau.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonParser;
import io.vavr.CheckedFunction1;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauConfigurations;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauReportData;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauConfiguration;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauToken;
import org.apache.fineract.infrastructure.creditbureau.domain.TokenRepositoryWrapper;
import org.apache.fineract.infrastructure.creditbureau.serialization.CreditBureauTokenCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class ThitsaWorksCreditBureauIntegrationWritePlatformServiceImplTest {

    @Spy
    private FromJsonHelper fromJsonHelper = new FromJsonHelper();

    @Spy
    private CreditBureauTokenCommandFromApiJsonDeserializer fromApiJsonDeserializer = new CreditBureauTokenCommandFromApiJsonDeserializer(
            fromJsonHelper);
    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private CreditBureauConfigurationRepositoryWrapper configurationRepositoryWrapper;

    @Mock
    private PlatformSecurityContext platformSecurityContext;

    @Mock
    private TokenRepositoryWrapper tokenRepositoryWrapper;

    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private ThitsaWorksCreditBureauIntegrationWritePlatformServiceImpl underTest;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(configurationRepositoryWrapper.getCreditBureauConfigData(1, CreditBureauConfigurations.USERNAME.name()))
                .thenReturn(new CreditBureauConfiguration().setValue("testUsername"));
        when(configurationRepositoryWrapper.getCreditBureauConfigData(1, CreditBureauConfigurations.PASSWORD.name()))
                .thenReturn(new CreditBureauConfiguration().setValue("testPassword"));
        when(configurationRepositoryWrapper.getCreditBureauConfigData(1, CreditBureauConfigurations.CREDITREPORTURL.name()))
                .thenReturn(new CreditBureauConfiguration().setValue("https://credit.report.url/api/"));
        when(configurationRepositoryWrapper.getCreditBureauConfigData(1, CreditBureauConfigurations.SEARCHURL.name()))
                .thenReturn(new CreditBureauConfiguration().setValue("https://search.report.url/api/"));
        when(configurationRepositoryWrapper.getCreditBureauConfigData(1, CreditBureauConfigurations.TOKENURL.name()))
                .thenReturn(new CreditBureauConfiguration().setValue("https://token.url/api/"));
        when(configurationRepositoryWrapper.getCreditBureauConfigData(1, CreditBureauConfigurations.SUBSCRIPTIONID.name()))
                .thenReturn(new CreditBureauConfiguration().setValue("subscriptionId"));
        when(configurationRepositoryWrapper.getCreditBureauConfigData(1, CreditBureauConfigurations.SUBSCRIPTIONKEY.name()))
                .thenReturn(new CreditBureauConfiguration().setValue("subscriptionKey"));

    }

    private String createResponseObjectArrayData(Supplier<String> responseMessageGenerator, Function<ArrayNode, ArrayNode> dataGenerator)
            throws JsonProcessingException {
        ObjectNode jsonResponse = mapper.createObjectNode();

        jsonResponse.put("ResponseMessage", responseMessageGenerator.get());
        jsonResponse.set("Data", dataGenerator.apply(mapper.createArrayNode()));
        return mapper.writeValueAsString(jsonResponse);
    }

    private String createResponseObjectObjectData(Supplier<String> responseMessageGenerator, Function<ObjectNode, ObjectNode> dataGenerator)
            throws JsonProcessingException {
        ObjectNode jsonResponse = mapper.createObjectNode();

        jsonResponse.put("ResponseMessage", responseMessageGenerator.get());
        jsonResponse.set("Data", dataGenerator.apply(mapper.createObjectNode()));
        return mapper.writeValueAsString(jsonResponse);
    }

    public void mockOkHttpCall(CheckedFunction1<Request, Response> responseGenerator) throws IOException {
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        Call callMock = mock(Call.class);
        when(okHttpClient.newCall(requestCaptor.capture())).thenReturn(callMock);
        when(callMock.execute()).thenAnswer(invocation -> responseGenerator.apply(requestCaptor.getValue()));
    }

    public Response createOkhttpResponse(Request request, String body) {
        return new Response.Builder().request(request).protocol(okhttp3.Protocol.HTTP_1_1).code(200).message("")
                .body(ResponseBody.create(body, MediaType.parse("application/json"))).build();
    }

    public Response createOkhttpResponse(Request request, int status, String message, ResponseBody body) {
        return new Response.Builder().request(request).protocol(okhttp3.Protocol.HTTP_1_1).code(status).message(message).body(body).build();
    }

    public Response createOkhttpResponse(Request request, int status, String message) {
        return new Response.Builder().request(request).protocol(okhttp3.Protocol.HTTP_1_1).code(status).message(message)
                .body(ResponseBody.create(message, MediaType.parse("text/html"))).build();
    }

    @Test
    public void okHttpInternalServerErrorTest() throws IOException {

        mockOkHttpCall(request -> createOkhttpResponse(request, 500, "Internal Server Error"));

        assertThrows(PlatformDataIntegrityException.class, () -> {
            underTest.okHttpConnectionMethod("testUser", "testPassword", "subscriptionKey", "subscriptionId", "https://nrc.test.url.com",
                    "AccessToken", null, null, 0L, "nrcId", "NRC");

        });

    }

    @Test
    public void okHttpIOExceptionTest() throws IOException {
        mockOkHttpCall(request -> {
            throw new IOException("IO Exception");
        });

        assertThrows(PlatformDataIntegrityException.class, () -> {
            underTest.okHttpConnectionMethod("testUser", "testPassword", "subscriptionKey", "subscriptionId", "https://nrc.test.url.com",
                    "AccessToken", null, null, 0L, "nrcId", "NRC");

        });

    }

    @Test
    public void okHttpNrcSuccessTest() throws IOException {

        String jsonResponse = createResponseObjectArrayData(() -> "Success",
                data -> data.add(mapper.createObjectNode().put("UniqueID", "123456")));

        mockOkHttpCall(request -> {
            assertEquals(request.header("Authorization"), "Bearer AccessToken");
            assertEquals(request.header("mcix-subscription-key"), "subscriptionKey");
            assertEquals(request.header("mcix-subscription-id"), "subscriptionId");
            assertEquals(request.header("Content-Type"), "application/x-www-form-urlencoded");
            BufferedSink sink = Okio.buffer(Okio.sink(new ByteArrayOutputStream()));
            request.body().writeTo(sink);
            String urlEncodedForm = sink.getBuffer().readUtf8();
            assertTrue(urlEncodedForm.contains("nrc=nrcId"));
            return createOkhttpResponse(request, jsonResponse);
        });

        String result = underTest.okHttpConnectionMethod("testUser", "testPassword", "subscriptionKey", "subscriptionId",
                "https://nrc.test.url.com", "AccessToken", null, null, 0L, "nrcId", "NRC");
        assertEquals(jsonResponse, result);
    }

    @Test
    public void okHttpNrcNoTokenTest() throws IOException {

        String jsonResponse = createResponseObjectArrayData(() -> "Success",
                data -> data.add(mapper.createObjectNode().put("UniqueID", "123456")));

        mockOkHttpCall(request -> {
            List<String> auhtorizationHeaders = request.headers("Authorization");
            assertTrue(auhtorizationHeaders.isEmpty());
            assertEquals(request.header("mcix-subscription-key"), "subscriptionKey");
            assertEquals(request.header("mcix-subscription-id"), "subscriptionId");
            assertEquals(request.header("Content-Type"), "application/x-www-form-urlencoded");
            BufferedSink sink = Okio.buffer(Okio.sink(new ByteArrayOutputStream()));
            request.body().writeTo(sink);
            String urlEncodedForm = sink.getBuffer().readUtf8();
            assertTrue(urlEncodedForm.contains("nrc=nrcId"));
            return createOkhttpResponse(request, jsonResponse);
        });

        String result = underTest.okHttpConnectionMethod("testUser", "testPassword", "subscriptionKey", "subscriptionId",
                "https://nrc.test.url.com", null, null, null, 0L, "nrcId", "NRC");
        assertEquals(jsonResponse, result);
    }

    @Test
    public void okhttpUploadCreditReportTest() throws IOException {

        String jsonResponse = createResponseObjectArrayData(() -> "UPLOADED", data -> data);

        Path temp = Files.createTempFile("upload_test" + System.currentTimeMillis(), ".data");
        Files.writeString(temp, "test");

        mockOkHttpCall(request -> {
            assertEquals(request.header("Authorization"), "Bearer AccessToken");
            assertEquals(request.header("mcix-subscription-key"), "subscriptionKey");
            assertEquals(request.header("mcix-subscription-id"), "subscriptionId");
            assertEquals(request.header("Content-Type"), "multipart/form-data");
            return createOkhttpResponse(request, jsonResponse);
        });
        FormDataContentDisposition fileDetail = mock(FormDataContentDisposition.class);
        when(fileDetail.getFileName()).thenReturn("test.pdf");

        PlatformDataIntegrityException resultException = assertThrows(PlatformDataIntegrityException.class, () -> {
            underTest.okHttpConnectionMethod("testUser", "testPassword", "subscriptionKey", "subscriptionId", "https://upload.test.url.com",
                    "AccessToken", temp.toFile(), fileDetail, 0L, "nrcId", "UploadCreditReport");
        });
        assertEquals("UPLOADED", resultException.getDefaultUserMessage());
    }

    @Test
    public void okHttpNoTokenTest() throws IOException {
        mockOkHttpCall(request -> {
            List<String> auhtorizationHeaders = request.headers("Authorization");
            assertTrue(auhtorizationHeaders.isEmpty());
            assertEquals(request.header("mcix-subscription-key"), "subscriptionKey");
            assertEquals(request.header("mcix-subscription-id"), "subscriptionId");
            assertEquals(request.header("Content-Type"), "application/x-www-form-urlencoded");
            BufferedSink sink = Okio.buffer(Okio.sink(new ByteArrayOutputStream()));
            request.body().writeTo(sink);
            String urlEncodedForm = sink.getBuffer().readUtf8();
            assertTrue(urlEncodedForm.contains("grant_type=password"));
            assertTrue(urlEncodedForm.contains("userName=testUser"));
            assertTrue(urlEncodedForm.contains("password=testPassword"));
            return createOkhttpResponse(request, 401, "Unauthorized");
        });
        assertThrows(PlatformDataIntegrityException.class, () -> {
            underTest.okHttpConnectionMethod("testUser", "testPassword", "subscriptionKey", "subscriptionId", "https://nrc.test.url.com",
                    null, null, null, 0L, "nrcId", "token");
        });
    }

    @Test
    public void okHttpGetNrcReportSuccessTest() throws IOException {

        String jsonResponse = createResponseObjectArrayData(() -> "Success",
                data -> data.add(mapper.createObjectNode().put("UniqueID", "123456")));

        mockOkHttpCall(request -> {
            assertEquals(request.header("Authorization"), "Bearer AccessToken");
            assertEquals(request.header("mcix-subscription-key"), "subscriptionKey");
            assertEquals(request.header("mcix-subscription-id"), "subscriptionId");
            assertEquals(request.header("Content-Type"), "application/x-www-form-urlencoded");
            return createOkhttpResponse(request, jsonResponse);
        });

        String result = underTest.okHttpConnectionMethod("testUser", "testPassword", "subscriptionKey", "subscriptionId",
                "https://nrc.test.url.com", "AccessToken", null, null, 0L, "nrcId", "CreditReport");
        assertEquals(jsonResponse, result);
    }

    @Test
    public void okHttpForbiddenTest() throws IOException {
        mockOkHttpCall(request -> createOkhttpResponse(request, 403, "Forbidden"));

        assertThrows(PlatformDataIntegrityException.class, () -> {
            underTest.okHttpConnectionMethod("testUser", "testPassword", "subscriptionKey", "subscriptionId", "https://nrc.test.url.com",
                    "AccessToken", null, null, 0L, "nrcId", "CreditReport");
        });
    }

    @Test // TODO: Null body not handled
    public void okHttpNoBodyReturned() throws IOException {
        mockOkHttpCall(request -> createOkhttpResponse(request, 200, "OK", null));

        assertThrows(NullPointerException.class, () -> {
            underTest.okHttpConnectionMethod("testUser", "testPassword", "subscriptionKey", "subscriptionId", "https://nrc.test.url.com",
                    "AccessToken", null, null, 0L, "nrcId", "CreditReport");
        });
    }

    @Test
    public void extractUniqueIdSuccessTest() throws JsonProcessingException {
        String json = createResponseObjectArrayData(() -> "Success", data -> data.add(mapper.createObjectNode().put("UniqueID", "123456")));
        Long id = underTest.extractUniqueId(json);
        assertEquals(123456L, id.longValue());
    }

    @Test
    public void extractUniqueIdEmptyResultTest() throws JsonProcessingException {
        String json = createResponseObjectArrayData(() -> "NoResult", data -> data);
        PlatformDataIntegrityException result = assertThrows(PlatformDataIntegrityException.class, () -> {
            underTest.extractUniqueId(json);
        });
        assertEquals("NoResult", result.getDefaultUserMessage());
    }

    @Test
    public void extractUniqueIdMultipleResultTest() throws JsonProcessingException {
        String json = createResponseObjectArrayData(() -> "NoResult",
                data -> data.add(mapper.createObjectNode().put("UniqueID", "123456").put("NRC", "NRCID1"))
                        .add(mapper.createObjectNode().put("UniqueID", "7654321").put("NRC", "NRCID2")));
        PlatformDataIntegrityException result = assertThrows(PlatformDataIntegrityException.class, () -> {
            underTest.extractUniqueId(json);
        });
        assertTrue(result.getDefaultUserMessage().contains("\"NRCID1\", \"NRCID2\""));
    }

    @Test
    public void extractUniqueIdNoDataTest() throws JsonProcessingException {
        String json = createResponseObjectArrayData(() -> "NoData", data -> null);
        PlatformDataIntegrityException result = assertThrows(PlatformDataIntegrityException.class, () -> {
            underTest.extractUniqueId(json);
        });
        assertEquals("NoData", result.getDefaultUserMessage());
    }

    @Test
    public void createTokenTest() throws IOException {
        mockTokenGeneration();
        mockOkHttpCall(request -> {
            List<String> auhtorizationHeaders = request.headers("Authorization");
            assertTrue(auhtorizationHeaders.isEmpty());
            assertEquals(request.header("mcix-subscription-key"), "subscriptionKey");
            assertEquals(request.header("mcix-subscription-id"), "subscriptionId");
            assertEquals(request.header("Content-Type"), "application/x-www-form-urlencoded");
            BufferedSink sink = Okio.buffer(Okio.sink(new ByteArrayOutputStream()));
            request.body().writeTo(sink);
            String urlEncodedForm = sink.getBuffer().readUtf8();
            assertTrue(urlEncodedForm.contains("grant_type=password"));
            assertTrue(urlEncodedForm.contains("userName=testUser"));
            assertTrue(urlEncodedForm.contains("password=testPassword"));

            return createOkhttpResponse(request, 200, createValidToken());
        });
        CreditBureauToken token = underTest.createToken(1L);
        assertNotNull(token);
    }

    @NotNull
    private String createValidToken() throws JsonProcessingException {
        ObjectNode jsonResponse = mapper.createObjectNode();
        jsonResponse.put("access_token", "AccessToken");
        jsonResponse.put("expires_in", 3600);
        jsonResponse.put("token_type", "Bearer");
        jsonResponse.put("userName", "testUser");
        jsonResponse.put(".issued", "sample");
        jsonResponse.put(".expires", ZonedDateTime.now(ZoneId.systemDefault()).plusSeconds(3600)
                .format(new DateTimeFormatterBuilder().appendPattern("EEE, dd MMM yyyy kk:mm:ss zzz").toFormatter()));
        return mapper.writeValueAsString(jsonResponse);
    }

    private JsonCommand initialJsonCommand() throws JsonProcessingException {
        ObjectNode command = mapper.createObjectNode();
        command.put("NRC", "NRCID");
        command.put("creditBureauID", "1"); // Must match to the mocked config
        String json = mapper.writeValueAsString(command);
        return JsonCommand.from(json, JsonParser.parseString(json), fromJsonHelper, null, 1L, 2L, 3L, 4L, null, null, null, null, null,
                null, null, null);
    }

    private void mockTokenGeneration() {
        ArgumentCaptor<CreditBureauToken> tokenCaptor = ArgumentCaptor.forClass(CreditBureauToken.class);
        when(tokenRepositoryWrapper.getToken()).thenAnswer(answer -> {
            if (tokenCaptor.getAllValues().isEmpty()) {
                return null;
            }
            return tokenCaptor.getValue();
        });
        doNothing().when(tokenRepositoryWrapper).save(tokenCaptor.capture());
    }

    @Test
    public void getCreditReportFromThitsaWorksSuccessTest() throws IOException {
        mockTokenGeneration();
        mockOkHttpCall(request -> {
            // NRC Call
            if (request.url().host().equals("search.report.url")) {
                return createOkhttpResponse(request, createResponseObjectArrayData(() -> "Success",
                        data -> data.add(mapper.createObjectNode().put("UniqueID", "123456"))));
            }
            if (request.url().host().equals("credit.report.url")) {
                assertTrue(request.url().encodedPath().endsWith("/123456"));
                return createOkhttpResponse(request, createResponseObjectObjectData(() -> "Success", data -> {
                    ObjectNode borrowerData = mapper.createObjectNode();
                    borrowerData.put("Name", "Test Name");
                    borrowerData.put("Gender", "Male");
                    borrowerData.put("Address", "Test Address");
                    data.set("BorrowerInfo", borrowerData);
                    data.set("CreditScore", mapper.createObjectNode().put("Score", "123"));
                    data.set("ActiveLoans", mapper.createArrayNode().add("Loan1").add("Loan2"));
                    data.set("WriteOffLoans", mapper.createArrayNode().add("Loan3").add("Loan4"));
                    return data;
                }));
            }
            if (request.url().host().equals("token.url")) {
                return createOkhttpResponse(request, 200, createValidToken());
            }
            return createOkhttpResponse(request, 404, "Not Found");
        });

        CreditBureauReportData result = underTest.getCreditReportFromThitsaWorks(initialJsonCommand());
        assertNotNull(result);
    }

    @Test
    public void addCreditReportTest() throws IOException {
        mockTokenGeneration();

        when(configurationRepositoryWrapper.getCreditBureauConfigData(1, "addCreditReporturl"))
                .thenReturn(new CreditBureauConfiguration().setValue("https://addcredit.report.url/api/"));
        String jsonResponse = createResponseObjectArrayData(() -> "ADD_CREDIT_RESPONSE", data -> data);

        Path temp = Files.createTempFile("add_credit_report" + System.currentTimeMillis(), ".data");
        Files.writeString(temp, "test");

        mockOkHttpCall(request -> {
            if (request.url().host().equals("addcredit.report.url")) {
                return createOkhttpResponse(request, jsonResponse);
            }
            if (request.url().host().equals("token.url")) {
                return createOkhttpResponse(request, 200, createValidToken());
            }
            return createOkhttpResponse(request, 404, "Not Found");
        });
        FormDataContentDisposition fileDetail = mock(FormDataContentDisposition.class);
        when(fileDetail.getFileName()).thenReturn("test.pdf");

        PlatformDataIntegrityException result = assertThrows(PlatformDataIntegrityException.class,
                () -> underTest.addCreditReport(1L, temp.toFile(), fileDetail));
        assertEquals("ADD_CREDIT_RESPONSE", result.getDefaultUserMessage());
    }

    // TODO: if no borrower is throw NPE
    // @Test
    public void getCreditReportFromThitsaWorksEmptyBorrowerTest() throws IOException {
        mockTokenGeneration();
        mockOkHttpCall(request -> {
            // NRC Call
            if (request.url().host().equals("search.report.url")) {
                return createOkhttpResponse(request, createResponseObjectArrayData(() -> "Success",
                        data -> data.add(mapper.createObjectNode().put("UniqueID", "123456"))));
            }
            if (request.url().host().equals("credit.report.url")) {
                assertTrue(request.url().encodedPath().endsWith("/123456"));
                return createOkhttpResponse(request, createResponseObjectObjectData(() -> "Success", data -> {
                    data.set("CreditScore", mapper.createObjectNode().put("Score", "123"));
                    data.set("ActiveLoans", mapper.createArrayNode().add("Loan1").add("Loan2"));
                    data.set("WriteOffLoans", mapper.createArrayNode().add("Loan3").add("Loan4"));
                    return data;
                }));
            }
            if (request.url().host().equals("token.url")) {
                return createOkhttpResponse(request, 200, createValidToken());
            }
            return createOkhttpResponse(request, 404, "Not Found");
        });

        CreditBureauReportData result = underTest.getCreditReportFromThitsaWorks(initialJsonCommand());
        assertNotNull(result);
        assertNull(result.getGender());
        assertNotNull(result.getCreditScore());
    }

    // TODO: empty gender not handler correctly (NPE)
    // @Test
    public void getCreditReportFromThitsaWorksNoGenderTest() throws IOException {
        mockTokenGeneration();
        mockOkHttpCall(request -> {
            // NRC Call
            if (request.url().host().equals("search.report.url")) {
                return createOkhttpResponse(request, createResponseObjectArrayData(() -> "Success",
                        data -> data.add(mapper.createObjectNode().put("UniqueID", "123456"))));
            }
            if (request.url().host().equals("credit.report.url")) {
                assertTrue(request.url().encodedPath().endsWith("/123456"));
                return createOkhttpResponse(request, createResponseObjectObjectData(() -> "Success", data -> {
                    ObjectNode borrowerData = mapper.createObjectNode();
                    borrowerData.put("Name", "Test Name");
                    borrowerData.put("Address", "Test Address");
                    data.set("BorrowerInfo", borrowerData);
                    data.set("CreditScore", mapper.createObjectNode().put("Score", "123"));
                    data.set("ActiveLoans", mapper.createArrayNode().add("Loan1").add("Loan2"));
                    data.set("WriteOffLoans", mapper.createArrayNode().add("Loan3").add("Loan4"));
                    return data;
                }));
            }
            if (request.url().host().equals("token.url")) {
                return createOkhttpResponse(request, 200, createValidToken());
            }
            return createOkhttpResponse(request, 404, "Not Found");
        });

        CreditBureauReportData result = underTest.getCreditReportFromThitsaWorks(initialJsonCommand());
        assertNotNull(result);
        assertNull(result.getGender());
        assertNotNull(result.getCreditScore());
    }

    // TODO: null credit script invalid result
    // @Test
    public void getCreditReportFromThitsaWorksNoLoansTest() throws IOException {
        mockTokenGeneration();
        mockOkHttpCall(request -> {
            // NRC Call
            if (request.url().host().equals("search.report.url")) {
                return createOkhttpResponse(request, createResponseObjectArrayData(() -> "Success",
                        data -> data.add(mapper.createObjectNode().put("UniqueID", "123456"))));
            }
            if (request.url().host().equals("credit.report.url")) {
                assertTrue(request.url().encodedPath().endsWith("/123456"));
                return createOkhttpResponse(request, createResponseObjectObjectData(() -> "Success", data -> {
                    ObjectNode borrowerData = mapper.createObjectNode();
                    borrowerData.put("Name", "Test Name");
                    borrowerData.put("Gender", "Male");
                    borrowerData.put("Address", "Test Address");
                    data.set("BorrowerInfo", borrowerData);
                    return data;
                }));
            }
            if (request.url().host().equals("token.url")) {
                return createOkhttpResponse(request, 200, createValidToken());
            }
            return createOkhttpResponse(request, 404, "Not Found");
        });

        CreditBureauReportData result = underTest.getCreditReportFromThitsaWorks(initialJsonCommand());
        assertNotNull(result);
        assertNotNull(result.getGender());
        assertNull(result.getCreditScore());
        assertNull(result.getOpenAccounts());
        assertNull(result.getClosedAccounts());
    }

}
