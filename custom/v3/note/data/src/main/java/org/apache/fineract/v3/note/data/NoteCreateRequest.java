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

package org.apache.fineract.v3.note.data;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.v3.command.data.CommandRequest;

@Getter
@SuperBuilder
@FieldNameConstants
public class NoteCreateRequest extends CommandRequest<NoteRequestBody> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Resource ID can never be null")
    private final Long resourceId;

    @NotNull(message = "Note Type can never be null")
    private final NoteType noteType;
}
