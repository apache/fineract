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
package org.apache.fineract.organisation.teller.domain.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor
public class CashierRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 100)
    public String description;
    @NotNull
    public Boolean isFullDay;
    @NotNull
    @Min(value = 1)
    public Long staffId;
    @NotNull
    public String dateFormat;
    @NotNull
    public String startDate;
    @NotNull
    public String endDate;
    public String locale;

    @SneakyThrows
    public String toJson() {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS).writeValueAsString(this);
    }
}
