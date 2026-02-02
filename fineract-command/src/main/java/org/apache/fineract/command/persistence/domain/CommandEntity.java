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
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@FieldNameConstants
@Table("m_command")
public class CommandEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    @Column("executed_at")
    private Instant executedAt;

    @Column("approved_at")
    private Instant approvedAt;

    @Column("rejected_at")
    private Instant rejectedAt;

    @Column("initiated_by_username")
    private String initiatedByUsername;

    @Column("executed_by_username")
    private String executedByUsername;

    @Column("approved_by_username")
    private String approvedByUsername;

    @Column("rejected_by_username")
    private String rejectedByUsername;

    @Column("idempotency_key")
    private String idempotencyKey;

    @Column("state")
    private CommandEntityState state;

    @Column("error")
    private String error;

    @Column("ip_address")
    private String ipAddress;

    @Column("request")
    private JsonNode request;

    @Column("response")
    private JsonNode response;
}
