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
package org.apache.fineract.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "notification_generator")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Notification extends AbstractPersistableCustom<Long> {

    @Column(name = "object_type")
    private String objectType;

    @Column(name = "object_identifier")
    private Long objectIdentifier;

    @Column(name = "action")
    private String action;

    @Column(name = "actor")
    private Long actorId;

    @Column(name = "is_system_generated")
    private boolean isSystemGenerated;

    @Column(name = "notification_content")
    private String notificationContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
