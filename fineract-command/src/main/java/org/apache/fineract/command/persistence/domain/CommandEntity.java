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
package org.apache.fineract.command.persistence.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.apache.fineract.command.persistence.converter.JsonAttributeConverter;

@Getter
@Setter
@ToString
@FieldNameConstants
@Entity
@Table(name = "m_command")
public class CommandEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Transient
    @Setter(value = AccessLevel.NONE)
    private boolean isNew = true;

    @Column(name = "command_id")
    private UUID commandId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "username")
    private String username;

    @Column(name = "payload")
    @Convert(converter = JsonAttributeConverter.class)
    private JsonNode payload;

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
