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
package org.apache.fineract.portfolio.tax.api;

public interface TaxApiConstants {

    String nameParamName = "name";
    String percentageParamName = "percentage";
    String debitAccountTypeParamName = "debitAccountType";
    String debitAcountIdParamName = "debitAcountId";
    String creditAccountTypeParamName = "creditAccountType";
    String creditAcountIdParamName = "creditAcountId";

    String startDateParamName = "startDate";
    String endDateParamName = "endDate";

    String chargeIncludesTaxParamName = "chargeIncludesTax";
    String taxComponentsParamName = "taxComponents";
    String idParamName = "id";
    String taxComponentIdParamName = "taxComponentId";

}
