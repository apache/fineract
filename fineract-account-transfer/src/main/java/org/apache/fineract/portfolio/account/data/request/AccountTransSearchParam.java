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
package org.apache.fineract.portfolio.account.data.request;

import jakarta.ws.rs.QueryParam;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransSearchParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @QueryParam("fromOfficeId")
    private Long fromOfficeId;
    @QueryParam("fromClientId")
    private Long fromClientId;
    @QueryParam("fromAccountId")
    private Long fromAccountId;
    @QueryParam("fromAccountType")
    private Integer fromAccountType;
    @QueryParam("toOfficeId")
    private Long toOfficeId;
    @QueryParam("toClientId")
    private Long toClientId;
    @QueryParam("toAccountId")
    private Long toAccountId;
    @QueryParam("toAccountType")
    private Integer toAccountType;
}
