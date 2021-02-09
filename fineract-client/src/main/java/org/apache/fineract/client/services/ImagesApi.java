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
package org.apache.fineract.client.services;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Client API (Retrofit) for /images.
 *
 * This class entirely hand-written, inspired by DocumentsApiFixed, and from /images methods which currently end up in
 * DefaultApi (see <a href="https://issues.apache.org/jira/browse/FINERACT-1222">FINERACT-1222</a>), but fixed for bugs
 * in the code generation (see <a href="https://issues.apache.org/jira/browse/FINERACT-1227">FINERACT-1227</a>).
 *
 * @author Michael Vorburger.ch
 */
public interface ImagesApi {

    @POST("{entityType}/{entityId}/images")
    @retrofit2.http.Multipart
    Call<Void> create(@retrofit2.http.Path("entityType") String entityType, @retrofit2.http.Path("entityId") Long entityId,
            @retrofit2.http.Part okhttp3.MultipartBody.Part file);

    @GET("{entityType}/{entityId}/images")
    Call<ResponseBody> get(@retrofit2.http.Path("entityType") String entityType, @retrofit2.http.Path("entityId") Long entityId,
            @retrofit2.http.Query("maxWidth") Integer maxWidth, @retrofit2.http.Query("maxHeight") Integer maxHeight,
            @retrofit2.http.Query("output") String output);

    @PUT("{entityType}/{entityId}/images")
    @retrofit2.http.Multipart
    Call<Void> update(@retrofit2.http.Path("entityType") String entityType, @retrofit2.http.Path("entityId") Long entityId,
            @retrofit2.http.Part okhttp3.MultipartBody.Part file);

    @DELETE("{entityType}/{entityId}/images")
    Call<Void> delete(@retrofit2.http.Path("entityType") String entityType, @retrofit2.http.Path("entityId") Long entityId);
}
