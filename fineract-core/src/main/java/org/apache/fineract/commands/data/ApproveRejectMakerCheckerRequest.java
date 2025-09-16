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
package org.apache.fineract.commands.data;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveRejectMakerCheckerRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "{org.apache.fineract.commands.makerchecker.auditId.notnull}")
    @PositiveOrZero(message = "{org.apache.fineract.commands.makerchecker.auditId.positiveOrZero}")
    @Digits(integer = 10, fraction = 0, message = "{org.apache.fineract.commands.makerchecker.auditId.digits}")
    private Long auditId;

    @NotBlank(message = "{org.apache.fineract.commands.makerchecker.command.param.notblank}")
    @Pattern(regexp = "^(approve|reject)$", message = "{org.apache.fineract.commands.makerchecker.command.param.pattern}")
    private String commandParam;
}
