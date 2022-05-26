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
package org.apache.fineract.gradle.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.ConnectionPool
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.fineract.gradle.FineractPluginExtension.FineractPluginConfigConfluence
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*

import java.util.concurrent.TimeUnit

class ConfluenceService {
    private static final Logger log = LoggerFactory.getLogger(ConfluenceService.class)

    private ConfluenceApi api

    ConfluenceService(FineractPluginConfigConfluence config) {
        def credentials = Credentials.basic(config.username, config.password)

        final ConnectionPool connectionPool = new ConnectionPool(5, 60, TimeUnit.SECONDS);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(log::debug)
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        OkHttpClient.Builder okClient = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
                        def request = chain.request()
                        request = request.newBuilder().header("Authorization", credentials).build()
                        return chain.proceed(request)
                    }
                })

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(config.url)
                .client(okClient.build())
                .addConverterFactory(JacksonConverterFactory.create(mapper))

        Retrofit retrofit = builder.build()

        this.api = retrofit.create(ConfluenceApi.class)
    }

    ConfluenceContent getContent(String id) {
        this.api.getContentById(id).execute().body()
    }

    ConfluenceContent createContent(ConfluenceContent content) {
        this.api.postContent(content).execute().body()
    }

    ConfluenceContent updateContent(String id, ConfluenceContent content) {
        this.api.putContent(id, content).execute().body()
    }

    ConfluenceResponse deleteContent(String id) {
        this.api.deleteContentById(id).execute().body()
    }

    static class ConfluenceContent {
        String id
        String type
        String title
        String status
        String self
        List<ConfluenceParent> ancestors
        ConfluenceSpace space
        ConfluenceBody body
        ConfluenceVersion version
        ConfluenceHistory history
    }

    static class ConfluenceContentResult {
        List<ConfluenceContent> results
        private int start
        private int limit
        private int size
    }

    static class ConfluenceParent {
        String id
        String type
    }

    static class ConfluenceVersion {
        ConfluenceCreatedBy by
        Date when
        String message
        Integer number
    }

    static class ConfluenceCreatedBy {
        String username
        String displayName
    }

    static class ConfluenceHistory {
        boolean latest
        ConfluenceCreatedBy createdBy
        ConfluenceVersion nextVersion
        ConfluenceVersion previousVersion
        ConfluenceVersion lastUpdated
    }

    static class ConfluenceSpace {
        String key
        String name
    }

    static class ConfluenceSpaceResult {
        List<ConfluenceSpace> results
    }

    static class ConfluenceBody {
        ConfluenceStorage storage
    }

    static class ConfluenceStorage {
        String value
        String representation
    }

    static class ConfluenceResponse {
        int statusCode
        String message
        Map<String, ?> data
    }

    static interface ConfluenceApi {
        @GET("content")
        Call<ConfluenceContentResult> getContentResults();

        @GET("content")
        Call<ConfluenceContentResult> getContentBySpaceKeyAndTitle(final @Query("key") String key, final @Query("title") String title);

        @GET("content/{id}/child/{type}")
        Call<ConfluenceContentResult> getChildren(final @Path("id") String parentId, final @Path("type") String type, final @QueryMap Map<String, String> params);


        @GET("content/{id}?expand=body.storage")
        Call<ConfluenceContent> getContentById(final @Path("id") String id);

        @POST("contentbody/convert/{to}")
        Call<ConfluenceStorage> postContentConversion(final @Body ConfluenceStorage storage, final @Path("to") String convertToFormat);

        @POST("content")
        Call<ConfluenceContent> postContent(final @Body ConfluenceContent content);

        @PUT("content/{id}")
        Call<ConfluenceContent> putContent(final @Path("id") String id, final @Body ConfluenceContent content);

        @POST("content")
        void postContentWithCallback(final @Body ConfluenceContent content, final Callback<ConfluenceContent> callback);

        @DELETE("content/{id}")
        Call<ConfluenceResponse> deleteContentById(final @Path("id") String id);

        @GET("space")
        Call<ConfluenceSpaceResult> getSpaces();

        @POST("space")
        Call<ConfluenceSpace> createSpace(final @Body ConfluenceSpace space);

        @POST("space/_private")
        Call<ConfluenceSpace> createPrivateSpace(final @Body ConfluenceSpace space);

        @GET("space/{spaceKey}/content/page")
        Call<ConfluenceContentResult> getAllSpaceContent(final @Path("spaceKey") String spaceKey, final @QueryMap Map<String, String> params);

        @GET("space/{spaceKey}/content/{type}")
        Call<ConfluenceContentResult> getRootContentBySpaceKey(final @Path("spaceKey") String spaceKey, final @Path("type") String contentType);
    }
}
// curl -u aleks:xxx -X POST -H 'Content-Type: application/json' -d '{"type":"page","title":"1.7.0 - Apache Fineract","space":{"key":"FINERACT"},"ancestors":[{"id":75974324}],"body":{"storage":{"value":"<p>This is <br/> a new page</p>","representation": "storage"}}}' https://cwiki.apache.org/confluence/content/ | python -mjson.tool