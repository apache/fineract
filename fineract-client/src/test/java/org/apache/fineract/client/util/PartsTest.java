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
package org.apache.fineract.client.util;

import com.google.common.truth.Truth;
import com.google.common.truth.Truth8;
import okhttp3.Headers;
import okhttp3.MediaType;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

public class PartsTest {

    @Test
    void validMediaType() {
        Truth.assertThat(Parts.mediaType("test.jpg")).isEqualTo(MediaType.get("image/jpeg"));
    }

    @Test
    void dotMediaType() {
        Truth.assertThat(Parts.mediaType("test.")).isNull();
    }

    @Test
    void emptyMediaType() {
        Truth.assertThat(Parts.mediaType("")).isNull();
    }

    @Test
    void nullMediaType() {
        Truth.assertThat(Parts.mediaType(null)).isNull();
    }

    @Test
    void fileName() {
        Truth8.assertThat(Parts.fileName(Response.success(null, Headers.of("Content-Disposition", "attachment; filename=\"doc.pdf\""))))
                .hasValue("doc.pdf");
    }

    @Test
    void fileNameWithoutContentDisposition() {
        Truth8.assertThat(Parts.fileName(Response.success(null))).isEmpty();
    }
}
