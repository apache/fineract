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
package org.apache.fineract.organisation.office.data;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.Length;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class OfficeUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    private Long id;

    @Pattern(regexp = ".*\\S.*", message = "{org.apache.fineract.organisation.office.name.not-blank}")
    @Length(max = 100, message = "{org.apache.fineract.organisation.office.name.length}")
    private String name;

    @Min(value = 1, message = "{org.apache.fineract.organisation.office.parent-id.min}")
    private Long parentId;

    @Pattern(regexp = ".*\\S.*", message = "{org.apache.fineract.organisation.office.opening-date.not-blank}")
    private String openingDate;

    @Length(max = 100, message = "{org.apache.fineract.organisation.office.external-id.length}")
    private String externalId;

    private String dateFormat;
    private String locale;
}
