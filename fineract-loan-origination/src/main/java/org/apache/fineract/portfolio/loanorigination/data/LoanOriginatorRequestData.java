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
package org.apache.fineract.portfolio.loanorigination.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Loan Originator request payload")
public class LoanOriginatorRequestData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Unique external identifier (Revenue Share ID)", example = "REV-SHARE-001", required = true)
    private String externalId;

    @Schema(description = "Originator name", example = "Acme Merchant")
    private String name;

    @Schema(description = "Originator status", example = "ACTIVE", allowableValues = { "ACTIVE", "PENDING", "INACTIVE" })
    private String status;

    @Schema(description = "Code value ID for originator type (from LoanOriginatorType code)", example = "1")
    private Long originatorTypeId;

    @Schema(description = "Code value ID for channel type (from LoanOriginationChannelType code)", example = "2")
    private Long channelTypeId;
}
