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
package org.apache.fineract.portfolio.tax.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class TaxGroupUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    @NotBlank(message = "{org.apache.fineract.portfolio.tax.group.name.not-empty}")
    private String name;
    private String locale;
    @Valid
    private Set<TaxGroupComponentData> taxComponents;
    private String dateFormat;

    @JsonIgnore
    @AssertTrue(message = "org.apache.fineract.portfolio.tax.group.component-end-date.valid")
    public boolean isComponentsEndDateValid() {
        if (taxComponents == null || dateFormat == null) {
            return true;
        }

        DateTimeFormatter formatter;

        try {
            formatter = DateTimeFormatter.ofPattern(dateFormat);
        } catch (IllegalArgumentException e) {
            return false;
        }

        for (TaxGroupComponentData component : taxComponents) {
            if (component.getEndDate() != null) {
                try {
                    var parsed = LocalDate.parse(component.getEndDate(), formatter);

                    if (!parsed.isAfter(DateUtils.getBusinessLocalDate())) {
                        return false;
                    }
                } catch (DateTimeParseException e) {
                    return false;
                }
            }
        }

        return true;
    }

    @JsonIgnore
    @AssertTrue(message = "org.apache.fineract.portfolio.tax.group.component-start-date.valid")
    public boolean isComponentsStartEndDateMutuallyExclusive() {
        if (taxComponents == null) {
            return true;
        }

        for (TaxGroupComponentData component : taxComponents) {
            if (component.getStartDate() != null && component.getEndDate() != null) {
                return false;
            }
        }

        return true;
    }
}
