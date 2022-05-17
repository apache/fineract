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
package org.apache.fineract.notification.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

final class NotificationApiResourceSwagger {

    private NotificationApiResourceSwagger() {}

    @Schema(description = "GetNotificationsResponse")
    public static final class GetNotificationsResponse {

        private GetNotificationsResponse() {}

        static final class GetNotification {

            private GetNotification() {}

            @Schema(example = "1")
            public Long id;
            @Schema(example = "a")
            public String objectType;
            @Schema(example = "1")
            public Long objectId;
            @Schema(example = "a")
            public String action;
            @Schema(example = "1")
            public Long actorId;
            @Schema(example = "a")
            public String content;
            @Schema(example = "true")
            public boolean isRead;
            @Schema(example = "true")
            public boolean isSystemGenerated;
            @Schema(example = "a")
            public String tenantIdentifier;
            @Schema(example = "a")
            public String createdAt;
            @Schema(example = "1")
            public Long officeId;
            @Schema(example = "[]")
            public List<Long> userIds;
        }

        @Schema(example = "10")
        public int totalFilteredRecords;

        public List<GetNotification> pageItems;
    }
}
