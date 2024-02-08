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
package org.apache.fineract.infrastructure.creditbureau.service;

import java.io.File;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauReportData;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauToken;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

public interface ThitsaWorksCreditBureauIntegrationWritePlatformService {

    CreditBureauToken createToken(Long creditBureauID);

    Long extractUniqueId(String jsonResult);

    String okHttpConnectionMethod(String userName, String password, String subscriptionKey, String subscriptionId, String url, String token,
            File report, FormDataContentDisposition fileDetail, Long uniqueId, String nrcId, String process);

    CreditBureauReportData getCreditReportFromThitsaWorks(JsonCommand command);

    String addCreditReport(Long bureauId, File creditReport, FormDataContentDisposition fileDetail);

}
