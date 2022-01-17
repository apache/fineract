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
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.fineract.gradle.FineractPluginExtension
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*

import java.util.concurrent.TimeUnit

class JiraService {
    private static final Logger log = LoggerFactory.getLogger(JiraService.class)

    private JiraApi api

    JiraService(FineractPluginExtension.FineractPluginConfigJira config) {
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

        this.api = retrofit.create(JiraApi.class)
    }

    Map<String, Object> serverInfo() {
        return this.api.serverInfo.execute().body()
    }

    FineractPluginExtension.FineractPluginJiraParams search(FineractPluginExtension.FineractPluginJiraParams params) {
        def query = [:]

        query['jql'] = params.query
        query['startAt'] = params.pageOffset?:0
        query['maxResults'] = params.pageSize?:50
        // query['expand'] = [
        //     "names",
        //     "schema",
        //     "operations"
        // ]
        // query['fields'] = params.fields?:"*all"
        query['fields'] = params.includes?:[
            "summary",
            "status",
            "assignee",
            "fixVersions"
        ]

        // params.total = params.total?:1000
        params.total = 1000

        def result = this.api.searchIssues(query).execute().body()

        params.result.addAll(result?.issues as List)

        if(result?.issues.size()>=params.pageSize && params.result.size() < params.total) {
            params.pageOffset += params.pageSize
            search(params);
        }

        return params
    }

    List getProjects() {
        return this.api.getProjects()?.execute()?.body()
    }

    List getProjectVersions(String projectId) {
        return this.api.getProjectVersions(projectId)?.execute()?.body()
    }

    JiraVersion getVersion(String id) {
        return this.api.getVersion(id)?.execute()?.body()
    }

    void updateVersion(JiraVersion version) {
        this.api.updateVersion(version.id, version).execute()
    }

    static class JiraVersion {
        String self
        String id
        String description
        String name
        boolean archived
        boolean released
        String releaseDate
        String userReleaseDate
        String project
        Integer projectId
    }

    static class JiraSearchResult {
        String expand
        Integer startAt
        Integer maxResults
        Integer total
        List<JiraIssue> issues
        List<String> warningMessages
        Map<String, Object> names
        Map<String, Object> schema
    }

    static class JiraIssue {
        String expand
        String self
        String id
        String key
        Map<String, Object> renderedFields
        Map<String, Object> properties
        Map<String, Object> names
        Map<String, Object> schema
        Map<String, Object> fields
    }

    static class JiraIssueUpdateRequest {
        Map<String, Map<String, Object>> historyMetadata
        Map<String, List<Map<String, Object>>> update
        Map<String, Object> fields
        Map<String, List<Map<String, Object>>> properties
    }

    static interface JiraApi {
        // Server Info
        @GET("serverInfo")
        Call<Map<String, Object>> getServerInfo()

        // Components
        @GET("component/{id}")
        Call<Object> getComponent(@Path("id") String id)

        @POST("component")
        Call<Object> createComponent(@Body Object component)

        @PUT("component/{id}")
        Call<Void> updateComponent(@Path("id") String id, @Body Object component)

        @GET("component/{id}/relatedIssueCounts")
        Call<Object> getComponentIssueCount(@Path("id") String id)

        // Issue
        @GET("issue/{issueIdOrKey}")
        Call<Object> getIssue(@Path("issueIdOrKey") String issueIdOrKey)

        @POST("issue")
        Call<Object> createIssue(@Body Object issue)

        @PUT("issue/{issueIdOrKey}")
        Call<Object> updateIssue(@Path("issueIdOrKey") String issueIdOrKey, @Body Object issue, @QueryMap Map<String, String> queryMap)

        @POST("issue/bulk")
        Call<Object> createIssues(@Body Object issues)

        @PUT("issue/{issueIdOrKey}/assignee")
        Call<Void> assignIssue(@Path("issueIdOrKey") String issueIdOrKey, @Body Object user)

        @GET("issue/{issueIdOrKey}/comment")
        Call<Object> getComments(@Path("issueIdOrKey") String issueIdOrKey)

        @POST("issue/{issueIdOrKey}/comment")
        Call<Object> addComment(@Path("issueIdOrKey") String issueIdOrKey, @Body Object comment)

        @PUT("issue/{issueIdOrKey}/comment/{id}")
        Call<Object> updateComment(@Path("issueIdOrKey") String issueIdOrKey, @Path("id") String id, @Body Object comment)

        @GET("issue/{issueIdOrKey}/comment/{id}")
        Call<Object> getComment(@Path("issueIdOrKey") String issueIdOrKey, @Path("id") String id)

        @POST("issue/{issueIdOrKey}/notify")
        Call<Void> notifyIssue(@Path("issueIdOrKey") String issueIdOrKey, @Body Object notify)

        @GET("issue/{issueIdOrKey}/transitions?expand=transitions.fields")
        Call<Object> getTransitions(@Path("issueIdOrKey") String issueIdOrKey)

        @POST("issue/{issueIdOrKey}/transitions")
        Call<Void> transitionIssue(@Path("issueIdOrKey") String issueIdOrKey, @Body Object issue)

        @GET("issue/{issueIdOrKey}/watchers")
        Call<Object> getIssueWatches(@Path("issueIdOrKey") String issueIdOrKey)

        @POST("issue/{issueIdOrKey}/watchers")
        Call<Void> addIssueWatcher(@Path("issueIdOrKey") String issueIdOrKey, @Body String user)

        // Remote Issue Links
        @GET("issue/{issueIdOrKey}/remotelink")
        Call<Object> getIssueRemoteLinks(@Path("issueIdOrKey") String issueIdOrKey, @Query("globalId") String globalId)

        @GET("issue/{issueIdOrKey}/remotelink/{linkId}")
        Call<Object> getIssueRemoteLink(@Path("issueIdOrKey") String issueIdOrKey, @Path("linkId") String linkId)

        @POST("issue/{issueIdOrKey}/remotelink")
        Call<Object> createIssueRemoteLink(@Path("issueIdOrKey") String issueIdOrKey, @Body Object issueLink)

        @POST("issue/{issueIdOrKey}/remotelink/{linkId}")
        Call<Object> updateIssueRemoteLink(@Path("issueIdOrKey") String issueIdOrKey, @Path("linkId") String linkId, @Body Object issueLink)

        @DELETE("issue/{issueIdOrKey}/remotelink")
        Call<Object> deleteIssueRemoteLinks(@Path("issueIdOrKey") String issueIdOrKey, @Query("globalId") String globalId)

        @DELETE("issue/{issueIdOrKey}/remotelink/{linkId}")
        Call<Object> deleteIssueRemoteLink(@Path("issueIdOrKey") String issueIdOrKey, @Path("linkId") String linkId)

        // Issue Links
        @POST("issueLink")
        Call<Void> createIssueLink(@Body Object issueLink)

        @GET("issueLink/{linkId}")
        Call<Object> getIssueLink(@Path("linkId") String linkId)

        @DELETE("issueLink/{linkId}")
        Call<Object> deleteIssueLink(@Path("linkId") String linkId)

        // Issue Link Types
        @GET("issueLinkType")
        Call<Object> getIssueLinkTypes()

        // Project
        @GET("project?expand=lead,description")
        Call<List> getProjects()

        @GET("project/{projectIdOrKey}")
        Call<Object> getProject(@Path("projectIdOrKey") String projectId)

        @GET("project/{projectIdOrKey}/statuses")
        Call<Object> getProjectStatuses(@Path("projectIdOrKey") String projectId)

        @GET("project/{projectIdOrKey}/components")
        Call<Object> getProjectComponents(@Path("projectIdOrKey") String projectId)

        @GET("project/{projectIdOrKey}/versions")
        Call<List<JiraVersion>> getProjectVersions(@Path("projectIdOrKey") String projectId)

        // Search
        @POST("search")
        Call<JiraSearchResult> searchIssues(@Body Map<String, Object> search)

        // Version
        @POST("version")
        Call<JiraVersion> createVersion(@Body JiraVersion version)

        @GET("version/{id}")
        Call<JiraVersion> getVersion(@Path("id") String id)

        @PUT("version/{id}")
        Call<Void> updateVersion(@Path("id") String id, @Body JiraVersion version)

        // Fields
        @GET("field")
        Call<Object> getFields()

        @POST("field")
        Call<Object> createField(@Body Object field)

        // Users
        @GET("user/search")
        Call<Object> userSearch(@Query("username") String userName, @Query("startAt") int startAt, @Query("maxResults") int maxResults)

        @GET("user/assignable/search")
        Call<Object> assignableUserSearch(@Query("username") String userName, @Query("project") String project, @Query("issueKey") String issueKey, @Query("startAt") int startAt, @Query("maxResults") int maxResults)

        @Multipart
        @retrofit2.http.Headers("X-Atlassian-Token: no-check")
        @POST("issue/{issueIdOrKey}/attachments")
        Call<Object> uploadAttachment(@Path("issueIdOrKey") String issueIdOrKey, @Part MultipartBody.Part file)

        @GET("attachment/{attachmentId}")
        Call<Object> getAttachment(@Path("attachmentId") String attachmentId)

        @DELETE("attachment/{attachmentId}")
        Call<Object> deleteAttachment(@Path("attachmentId") String attachmentId)

        @Streaming
        @GET
        Call<ResponseBody> downloadFileWithDynamicUrl(@Url String fileUrl)
    }
}
