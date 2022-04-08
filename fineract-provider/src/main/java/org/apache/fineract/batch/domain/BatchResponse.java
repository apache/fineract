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
package org.apache.fineract.batch.domain;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Provides an object for separate HTTP responses in the Batch Response for Batch API. It contains all the information
 * about a particular HTTP response in the Batch Response. Getter and Setter functions are also included to access
 * response data fields.
 *
 * @author Rishabh Shukla
 *
 * @see org.apache.fineract.batch.api.BatchApiResource
 * @see org.apache.fineract.batch.service.BatchApiService
 * @see Header
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BatchResponse {

    private Long requestId;
    private Integer statusCode;
    private Set<Header> headers;
    private String body;
}
