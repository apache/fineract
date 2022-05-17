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
package org.apache.fineract.notification.data;

import java.io.Serializable;
import java.util.Set;
import lombok.Data;

@Data
public class NotificationData implements Serializable {

    private Long id;
    private String objectType;
    private Long objectId;
    private String action;
    private Long actorId;
    private String content;
    private boolean isRead;
    private boolean isSystemGenerated;
    private String tenantIdentifier;
    private String createdAt;
    private Long officeId;
    private Set<Long> userIds;

    public NotificationData() {}

    public NotificationData(String objectType, Long objectId, String action, Long actorId, String content, boolean isSystemGenerated,
            boolean isRead, String tenantIdentifier, Long officeId, Set<Long> userIds) {
        this.objectType = objectType;
        this.objectId = objectId;
        this.action = action;
        this.actorId = actorId;
        this.content = content;
        this.isRead = isRead;
        this.isSystemGenerated = isSystemGenerated;
        this.tenantIdentifier = tenantIdentifier;
        this.officeId = officeId;
        this.userIds = userIds;
    }
}
