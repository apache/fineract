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
package org.apache.fineract.portfolio.group.data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // set from the path by the resource, never from the body
    @NotNull(message = "{org.apache.fineract.portfolio.group.name.not-null}")
    @Length(max = 100, message = "{org.apache.fineract.portfolio.group.name.max}")
    private String name;
    @Length(max = 100, message = "{org.apache.fineract.portfolio.group.external-id.max}")
    private String externalId;
    @Positive(message = "{org.apache.fineract.portfolio.group.office-id.positive}")
    private Long officeId;
    @Positive(message = "{org.apache.fineract.portfolio.group.staff-id.positive}")
    private Long staffId;
    @Positive(message = "{org.apache.fineract.portfolio.group.center-id.positive}")
    private Long centerId;
    private String activationDate;
    private String submittedOnDate;
    private String dateFormat;
    private String locale;
}
