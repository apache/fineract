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

package org.apache.fineract.v3.command.data;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.apache.fineract.commands.domain.CommandProcessingResultType;

@Data
@SuperBuilder
@FieldNameConstants
public class CommandRequest<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID requestIdempotencyKey;
    private CommandProcessingResultType commandProcessingStatus;
    private JsonNode result;
    private Integer resultStatusCode;
    private OffsetDateTime createdDate;
    private OffsetDateTime processedAt;
    private T body;
}
