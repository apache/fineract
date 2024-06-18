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

package org.apache.fineract.v3.command.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Getter
@Setter
@FieldNameConstants
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_command")
public class Command extends AbstractPersistableCustom<Long> {

    @Column(name = "request_idempotency_key", nullable = false, unique = true)
    private String requestIdempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "command_processing_status")
    private CommandProcessingResultType commandProcessingStatus;

    @Column(name = "result", columnDefinition = "text")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode result;

    @Column(name = "result_status_code")
    private Integer resultStatusCode;

    @Column(name = "created_date")
    private OffsetDateTime createdDate;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "body", columnDefinition = "text")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode body;

}
