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
package org.apache.fineract.infrastructure.gcm.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Result of a GCM multicast message request .
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class MulticastResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private int success;
    private int failure;
    private int canonicalIds;
    private long multicastId;
    private List<Result> results;
    private List<Long> retryMulticastIds;

    public static final class Builder {

        private final List<Result> results = new ArrayList<>();

        // required parameters
        private final int success;
        private final int failure;
        private final int canonicalIds;
        private final long multicastId;

        // optional parameters
        private List<Long> retryMulticastIds;

        public Builder(int success, int failure, int canonicalIds, long multicastId) {
            this.success = success;
            this.failure = failure;
            this.canonicalIds = canonicalIds;
            this.multicastId = multicastId;
        }

        public Builder addResult(Result result) {
            results.add(result);
            return this;
        }

        public Builder retryMulticastIds(List<Long> retryMulticastIds) {
            this.retryMulticastIds = retryMulticastIds;
            return this;
        }

        public MulticastResult build() {
            return new MulticastResult(this);
        }
    }

    private MulticastResult(Builder builder) {
        success = builder.success;
        failure = builder.failure;
        canonicalIds = builder.canonicalIds;
        multicastId = builder.multicastId;
        results = Collections.unmodifiableList(builder.results);
        List<Long> tmpList = builder.retryMulticastIds;
        if (tmpList == null) {
            tmpList = Collections.emptyList();
        }
        retryMulticastIds = Collections.unmodifiableList(tmpList);
    }
}
