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
package org.apache.fineract.infrastructure.cache;

import org.apache.fineract.infrastructure.cache.domain.CacheType;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public class CacheEnumerations {

    public static EnumOptionData cacheType(final int id) {
        return cacheType(CacheType.fromInt(id));
    }

    public static EnumOptionData cacheType(final CacheType cacheType) {
        EnumOptionData optionData = new EnumOptionData(CacheType.INVALID.getValue().longValue(), CacheType.INVALID.getCode(), "Invalid");
        switch (cacheType) {
            case INVALID:
                optionData = new EnumOptionData(CacheType.INVALID.getValue().longValue(), CacheType.INVALID.getCode(), "Invalid");
            break;
            case NO_CACHE:
                optionData = new EnumOptionData(CacheType.NO_CACHE.getValue().longValue(), CacheType.NO_CACHE.getCode(), "No cache");
            break;
            case SINGLE_NODE:
                optionData = new EnumOptionData(CacheType.SINGLE_NODE.getValue().longValue(), CacheType.SINGLE_NODE.getCode(),
                        "Single node");
            break;
            case MULTI_NODE:
                optionData = new EnumOptionData(CacheType.MULTI_NODE.getValue().longValue(), CacheType.MULTI_NODE.getCode(), "Multi node");
            break;
        }

        return optionData;
    }
}
