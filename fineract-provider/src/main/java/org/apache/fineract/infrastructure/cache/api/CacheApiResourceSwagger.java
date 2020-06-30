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
package org.apache.fineract.infrastructure.cache.api;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Created by sanyam on 28/7/17.
 */
final class CacheApiResourceSwagger {

    private CacheApiResourceSwagger() {

    }

    @Schema(description = "GetCachesResponse")
    public static final class GetCachesResponse {

        private GetCachesResponse() {

        }

        public EnumOptionData cacheType;
        public boolean enabled;
    }

    @Schema(description = "PutCachesRequest")
    public static final class PutCachesRequest {

        private PutCachesRequest() {

        }

        @Schema(example = "2")
        public Long cacheType;

    }

    @Schema(description = "PutCachesResponse")
    public static final class PutCachesResponse {

        private PutCachesResponse() {

        }

        public static final class PutCachechangesSwagger {

            private PutCachechangesSwagger() {

            }

            @Schema(example = "2")
            public Long cacheType;

        }

        public PutCachechangesSwagger cacheType;

    }
}
