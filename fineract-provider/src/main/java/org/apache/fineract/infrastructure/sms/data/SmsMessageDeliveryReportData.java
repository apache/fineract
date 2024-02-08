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
package org.apache.fineract.infrastructure.sms.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Immutable data object representing an outbound SMS message delivery report data
 **/
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SmsMessageDeliveryReportData {

    private Long id;
    private String externalId;
    private String addedOnDate;
    private String deliveredOnDate;
    private Integer deliveryStatus;
    private Boolean hasError;
    private String errorMessage;

    /**
     * @return an instance of the SmsMessageDeliveryReportData class
     **/
    public static SmsMessageDeliveryReportData getInstance(Long id, String externalId, String addedOnDate, String deliveredOnDate,
            Integer deliveryStatus, Boolean hasError, String errorMessage) {

        return new SmsMessageDeliveryReportData().setId(id).setExternalId(externalId).setAddedOnDate(addedOnDate)
                .setDeliveredOnDate(deliveredOnDate).setDeliveryStatus(deliveryStatus).setHasError(hasError).setErrorMessage(errorMessage);
    }
}
