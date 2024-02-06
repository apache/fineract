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

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauConfigurations;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauReportData;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauConfiguration;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauToken;
import org.apache.fineract.infrastructure.creditbureau.domain.TokenRepositoryWrapper;
import org.apache.fineract.infrastructure.creditbureau.serialization.CreditBureauTokenCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ThitsaWorksCreditBureauIntegrationWritePlatformServiceImpl implements ThitsaWorksCreditBureauIntegrationWritePlatformService {

    public static final String UPLOAD_CREDIT_REPORT = "UploadCreditReport";
    public static final String RESPONSE_MESSAGE = "ResponseMessage";
    public static final String IS_NOT_AVAILABLE_SUFFIX = ".is.not.available";
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final TokenRepositoryWrapper tokenRepositoryWrapper;
    private final CreditBureauConfigurationRepositoryWrapper configDataRepository;
    private final CreditBureauTokenCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final OkHttpClient client;

    @Transactional
    @Override
    public String okHttpConnectionMethod(String userName, String password, String subscriptionKey, String subscriptionId, String url,
            String token, File file, FormDataContentDisposition fileData, Long uniqueId, String nrcId, @NotNull String process) {

        String responseMessage = null;
        if (StringUtils.isBlank(url)) {
            throw new PlatformDataIntegrityException("error.msg.url.is.null.or.empty", "URL is null or empty");
        }
        HttpUrl urlBuilder = HttpUrl.parse(url);
        if (urlBuilder == null) {
            throw new PlatformDataIntegrityException("error.url.not.parsed", "URL not parsed");
        }
        String okHttpUrl = urlBuilder.toString();
        Request request = null;
        Request.Builder baseRequestBuilder = createRequestBuilder(subscriptionKey, subscriptionId, token, okHttpUrl);
        switch (process) {
            case UPLOAD_CREDIT_REPORT ->
                request = createRequest(baseRequestBuilder, () -> new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", fileData.getFileName(), RequestBody.create(file, MediaType.parse("multipart/form-data")))
                        .addFormDataPart("BODY", "formdata").addFormDataPart("userName", userName).build(),
                        (requestBody, builder) -> builder.header(CONTENT_TYPE, MULTIPART_FORM_DATA).post(requestBody).build());
            case "CreditReport" -> request = createRequest(baseRequestBuilder,
                    builder -> builder.header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED).get().build());
            case "token" -> request = createRequest(baseRequestBuilder,
                    () -> RequestBody.create("" + "BODY=x-www-form-urlencoded&\r" + "grant_type=password&\r" + "userName=" + userName
                            + "&\r" + "password=" + password + "&\r", MediaType.parse("application/x-www-form-urlencoded")),
                    (requestBody, builder) -> builder.header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED).post(requestBody).build());
            case "NRC" -> request = createRequest(baseRequestBuilder,
                    () -> RequestBody.create("BODY=x-www-form-urlencoded&nrc=" + nrcId + "&",
                            MediaType.parse("application/x-www-form-urlencoded")),
                    (requestBody, builder) -> builder.header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED).post(requestBody).build());
            default -> handleAPIIntegrityIssues("Invalid Process");
        }

        Response response;
        int responseCode = 0;
        try {
            response = client.newCall(request).execute();
            responseCode = response.code();
            responseMessage = response.body().string();
        } catch (IOException e) {

            log.error("error occured in HTTP request-response method.", e);
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            this.httpResponse(responseCode, responseMessage);
        }

        if (process.equals(UPLOAD_CREDIT_REPORT)) { // to show the Response on frontEnd
            JsonObject reportObject = JsonParser.parseString(responseMessage).getAsJsonObject();
            String responseMessageJson = reportObject.get(RESPONSE_MESSAGE).getAsString();
            this.handleAPIIntegrityIssues(responseMessageJson);
        }
        return responseMessage;

    }

    /**
     * Create a request object with the given request builder without request body support (ex: GET)
     *
     * @param builder
     *            the request builder object
     * @param requestBuilder
     *            create a request from the builder object with customization
     * @return return the generated request object
     */
    private Request createRequest(Request.Builder builder, Function<Request.Builder, Request> requestBuilder) {
        return requestBuilder.apply(builder);
    }

    /**
     * Create a reuqest obejct with a request body support
     *
     * @param builder
     *            the base Builder object
     * @param requestBodySupplier
     *            request body supplier method
     * @param requestBuilder
     *            generate request from the builder object with customization
     * @return return the generated request object
     */
    private Request createRequest(Request.Builder builder, Supplier<RequestBody> requestBodySupplier,
            BiFunction<RequestBody, Request.Builder, Request> requestBuilder) {
        return requestBuilder.apply(requestBodySupplier.get(), builder);
    }

    /**
     * Create a request builder for the given url if no token not adds authorization header
     *
     * @param subscriptionKey
     *            subscription key parameter
     * @param subscriptionId
     *            subscription id parameter
     * @param token
     *            token can be null
     * @param url
     *            the url to connect
     * @return a request builder base object
     */
    private Request.Builder createRequestBuilder(String subscriptionKey, String subscriptionId, @Nullable String token, String url) {
        Request.Builder base = new Request.Builder().header("mcix-subscription-key", subscriptionKey)
                .header("mcix-subscription-id", subscriptionId).url(url);
        if (token != null) {
            return base.header("Authorization", "Bearer " + token);
        }
        return base;
    }

    private void httpResponse(Integer responseCode, String responseMessage) {

        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {

            String httpResponse = "HTTP_UNAUTHORIZED";
            this.handleAPIIntegrityIssues(httpResponse);

        } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {

            String httpResponse = "HTTP_FORBIDDEN";
            this.handleAPIIntegrityIssues(httpResponse);

        } else {
            String responseResult = "HTTP Response Code: " + responseCode + "/" + "Response Message: " + responseMessage;
            this.handleAPIIntegrityIssues(responseResult);
        }

    }

    @Transactional
    @Override
    public CreditBureauReportData getCreditReportFromThitsaWorks(final JsonCommand command) {

        this.context.authenticatedUser();
        String nrcId = command.stringValueOfParameterNamed("NRC");
        String bureauID = command.stringValueOfParameterNamed("creditBureauID");
        Integer creditBureauId = Integer.parseInt(bureauID);

        String userName = getCreditBureauConfiguration(creditBureauId, CreditBureauConfigurations.USERNAME.toString());
        String password = getCreditBureauConfiguration(creditBureauId, CreditBureauConfigurations.PASSWORD.toString());
        String subscriptionId = getCreditBureauConfiguration(creditBureauId, CreditBureauConfigurations.SUBSCRIPTIONID.toString());
        String subscriptionKey = getCreditBureauConfiguration(creditBureauId, CreditBureauConfigurations.SUBSCRIPTIONKEY.toString());

        CreditBureauToken creditbureautoken = createToken(creditBureauId.longValue());
        String token = creditbureautoken.getAccessToken();

        // will use only "NRC" part of code from common http method to get data based on the nrc
        String process = "NRC";
        String url = getCreditBureauConfiguration(creditBureauId, CreditBureauConfigurations.SEARCHURL.toString());

        String nrcUrl = url + nrcId;

        String searchResult = this.okHttpConnectionMethod(userName, password, subscriptionKey, subscriptionId, nrcUrl, token, null, null,
                0L, nrcId, process);

        Long uniqueID = this.extractUniqueId(searchResult);

        process = "CreditReport";
        url = getCreditBureauConfiguration(creditBureauId, CreditBureauConfigurations.CREDITREPORTURL.toString());

        String creditReportUrl = url + uniqueID;

        searchResult = this.okHttpConnectionMethod(userName, password, subscriptionKey, subscriptionId, creditReportUrl, token, null, null,
                uniqueID, null, process);

        // after getting the result(creditreport) from httpconnection-response it will assign creditreport to generic
        // creditreportdata object

        JsonObject reportObject = JsonParser.parseString(searchResult).getAsJsonObject();

        // Credit Reports Stored into Generic CreditReportData

        // Extract Data from Credit Report
        Optional<JsonObject> jsonData = Optional.ofNullable(reportObject.get("Data")).filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject);

        // Extract Borrower from Credit Report data section.
        Optional<JsonElement> element = jsonData.map(data -> data.get("BorrowerInfo"));

        // Fill borrower data if present, and it's a json object.
        Optional<JsonObject> borrowerInfos = element.filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject);

        // Create json object for borrower data.
        String borrowerInfo = borrowerInfos.map(data -> new Gson().toJson(data)).orElse(null);

        String name = borrowerInfos.map(data -> data.get("Name")).map(JsonElement::toString).orElse(null);
        String gender = borrowerInfos.map(data -> data.get("Gender")).map(JsonElement::toString).orElse(null);
        String address = borrowerInfos.map(data -> data.get("Address")).map(JsonElement::toString).orElse(null);

        String creditScore = "CreditScore";
        creditScore = getJsonObjectToString(creditScore, jsonData);

        String activeLoans = "ActiveLoans";

        String[] activeLoanStringArray = Optional.ofNullable(getJsonObjectToArray(activeLoans, jsonData))
                .map(this::convertArrayintoStringArray).orElse(null);

        String writeOffLoans = "WriteOffLoans";
        String[] writeoffLoanStringArray = Optional.ofNullable(getJsonObjectToArray(writeOffLoans, jsonData))
                .map(this::convertArrayintoStringArray).orElse(null);

        return CreditBureauReportData.instance(name, gender, address, creditScore, borrowerInfo, activeLoanStringArray,
                writeoffLoanStringArray);
    }

    @Override
    @Transactional
    public String addCreditReport(Long bureauId, File creditReport, FormDataContentDisposition fileDetail) {

        Integer creditBureauId = bureauId.intValue();

        String userName = this.getCreditBureauConfiguration(creditBureauId, CreditBureauConfigurations.USERNAME.toString());
        String password = this.getCreditBureauConfiguration(creditBureauId, CreditBureauConfigurations.PASSWORD.toString());
        String subscriptionId = this.getCreditBureauConfiguration(creditBureauId, CreditBureauConfigurations.SUBSCRIPTIONID.toString());
        String subscriptionKey = this.getCreditBureauConfiguration(creditBureauId, CreditBureauConfigurations.SUBSCRIPTIONKEY.toString());

        CreditBureauToken creditbureautoken = this.createToken(creditBureauId.longValue());
        String token = creditbureautoken.getAccessToken();

        CreditBureauConfiguration addReportURL = this.configDataRepository.getCreditBureauConfigData(creditBureauId, "addCreditReporturl");
        String url = addReportURL.getValue();

        return this.okHttpConnectionMethod(userName, password, subscriptionKey, subscriptionId, url, token, creditReport, fileDetail, 0L,
                null, UPLOAD_CREDIT_REPORT);
    }

    private String[] convertArrayintoStringArray(JsonArray jsonResult) {

        String[] loanAccounts = new String[jsonResult.size()];

        int i = 0;
        for (JsonElement ele : jsonResult) {
            loanAccounts[i++] = ele.toString();
        }

        return loanAccounts;
    }

    @Override
    @Transactional
    public Long extractUniqueId(String jsonResult) {

        JsonObject reportObject = JsonParser.parseString(jsonResult).getAsJsonObject();

        JsonElement element = reportObject.get("Data");

        if (element.isJsonNull()) {
            String responseMessage = reportObject.get(RESPONSE_MESSAGE).getAsString();
            handleAPIIntegrityIssues(responseMessage);
        }

        // to fetch the Unique ID from Result
        JsonObject jsonObject = JsonParser.parseString(jsonResult).getAsJsonObject();

        long uniqueID = 0L;
        try {
            JsonArray dataArray = jsonObject.getAsJsonArray("Data");

            if (dataArray.size() == 1) {

                JsonObject jobject = dataArray.get(0).getAsJsonObject();

                String uniqueIdString = jobject.get("UniqueID").toString();
                String trimUniqueId = uniqueIdString.substring(1, uniqueIdString.length() - 1);

                uniqueID = Long.parseLong(trimUniqueId);

            } else if (dataArray.size() == 0) {
                String responseMessage = reportObject.get(RESPONSE_MESSAGE).getAsString();
                handleAPIIntegrityIssues(responseMessage);
            } else {
                String nrc;
                List<String> arrlist = new ArrayList<>();

                for (int i = 0; i < dataArray.size(); ++i) {
                    JsonObject data = dataArray.get(i).getAsJsonObject();
                    nrc = data.get("NRC").toString();
                    arrlist.add(nrc);
                }

                String listString = String.join(", ", arrlist);

                this.handleMultipleNRC(listString);
            }

        } catch (IndexOutOfBoundsException e) {
            String responseMessage = jsonObject.get(RESPONSE_MESSAGE).getAsString();
            handleAPIIntegrityIssues(responseMessage);
        }
        return uniqueID;
    }

    private String getJsonObjectToString(String fetchData, Optional<JsonObject> jsonData) {
        return jsonData.map(data -> data.get(fetchData)).filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject)
                .map(data -> new Gson().toJson(data)).orElse(null);
    }

    private JsonArray getJsonObjectToArray(String fetchData, Optional<JsonObject> jsonData) {
        return jsonData.map(data -> data.get(fetchData)).filter(JsonElement::isJsonArray).map(JsonElement::getAsJsonArray).orElse(null);
    }

    @Transactional
    @Override
    public CreditBureauToken createToken(Long bureauID) {

        CreditBureauToken creditBureauToken = this.tokenRepositoryWrapper.getToken();

        // check the expiry date of the previous token.
        if (creditBureauToken != null) {
            LocalDate current = DateUtils.getLocalDateOfTenant();
            LocalDate getExpiryDate = creditBureauToken.getExpires();

            if (DateUtils.isBefore(getExpiryDate, current)) {
                this.tokenRepositoryWrapper.delete(creditBureauToken);
                creditBureauToken = null;
            }
        }
        // storing token if it is valid token(not expired)

        if (creditBureauToken != null) {
            creditBureauToken = this.tokenRepositoryWrapper.getToken();
        }

        String userName = getCreditBureauConfiguration(bureauID.intValue(), CreditBureauConfigurations.USERNAME.toString());
        String password = getCreditBureauConfiguration(bureauID.intValue(), CreditBureauConfigurations.PASSWORD.toString());
        String subscriptionId = getCreditBureauConfiguration(bureauID.intValue(), CreditBureauConfigurations.SUBSCRIPTIONID.toString());
        String subscriptionKey = getCreditBureauConfiguration(bureauID.intValue(), CreditBureauConfigurations.SUBSCRIPTIONKEY.toString());

        if (creditBureauToken == null) {
            String url = getCreditBureauConfiguration(bureauID.intValue(), CreditBureauConfigurations.TOKENURL.toString());

            String process = "token";
            Long uniqueID = 0L;
            String result = this.okHttpConnectionMethod(userName, password, subscriptionKey, subscriptionId, url, null, null, null,
                    uniqueID, null, process);
            // created token will be storing it into database
            final CommandWrapper wrapper = new CommandWrapperBuilder().withJson(result).build();
            final String json = wrapper.getJson();

            final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);

            JsonCommand apicommand = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(),
                    wrapper.getEntityId(), wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(),
                    wrapper.getSavingsId(), wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId(),
                    wrapper.getCreditBureauId(), wrapper.getOrganisationCreditBureauId(), wrapper.getJobName());

            this.fromApiJsonDeserializer.validateForCreate(apicommand.json());

            final CreditBureauToken generatedtoken = CreditBureauToken.fromJson(apicommand);

            final CreditBureauToken credittoken = this.tokenRepositoryWrapper.getToken();
            if (credittoken != null) {
                this.tokenRepositoryWrapper.delete(credittoken);
            }

            this.tokenRepositoryWrapper.save(generatedtoken);

            creditBureauToken = this.tokenRepositoryWrapper.getToken();

        }

        return creditBureauToken;
    }

    public String getCreditBureauConfiguration(Integer creditBureauId, String configurationParameterName) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("ThitsaWorksCreditBureauIntegration");

        String creditBureauConfigurationValue;
        try {

            CreditBureauConfiguration configurationParameterValue = this.configDataRepository.getCreditBureauConfigData(creditBureauId,
                    configurationParameterName);

            creditBureauConfigurationValue = configurationParameterValue.getValue();
            if (creditBureauConfigurationValue.isEmpty()) {

                baseDataValidator.reset()
                        .failWithCode("creditBureau.configuration." + configurationParameterName + IS_NOT_AVAILABLE_SUFFIX);

                throw new PlatformDataIntegrityException(
                        "creditBureau.Configuration." + configurationParameterName + IS_NOT_AVAILABLE_SUFFIX,
                        "creditBureau.Configuration." + configurationParameterName + IS_NOT_AVAILABLE_SUFFIX);

            }
        } catch (Exception ex) {
            baseDataValidator.reset().failWithCode("creditBureau.configuration.is.not.available");
            throw new PlatformApiDataValidationException("creditBureau.Configuration.is.not.available" + ex,
                    "creditBureau.Configuration.is.not.available", dataValidationErrors);

        }

        return creditBureauConfigurationValue;
    }

    private void handleAPIIntegrityIssues(String httpResponse) {

        throw new PlatformDataIntegrityException(httpResponse, httpResponse);

    }

    private void handleMultipleNRC(String nrc) {
        String showMessageForMultipleNRC = "Found Multiple NRC's, Enter one from the given:" + nrc + "." + "";

        throw new PlatformDataIntegrityException(showMessageForMultipleNRC, showMessageForMultipleNRC);

    }
}
