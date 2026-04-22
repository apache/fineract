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
package org.apache.fineract.infrastructure.documentmanagement.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "{org.apache.fineract.document.id.not-null}")
    private Long id;
    @NotNull(message = "{org.apache.fineract.document.entity-type.not-null}")
    @Size(max = 50, message = "{org.apache.fineract.document.entity-type.size}")
    private String entityType;
    @NotNull(message = "{org.apache.fineract.document.entity-id.not-null}")
    private Long entityId;
    @Size(max = 250, message = "{org.apache.fineract.document.name.size}")
    private String name;
    @Size(max = 250, message = "{org.apache.fineract.document.description.size}")
    private String description;
    @Size(max = 250, message = "{org.apache.fineract.document.file-name.size}")
    private String fileName;
    @NotNull(message = "{org.apache.fineract.document.size.not-null}")
    @Min(value = 1, message = "{org.apache.fineract.document.size.min}")
    private Long size;
    private String type;
    @Hidden
    @JsonIgnore
    private InputStream stream;
}
