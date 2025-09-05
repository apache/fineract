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
package org.apache.fineract.cob.converter;

import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.data.LoanCOBParameter;

public final class COBParameterConverter {

    private COBParameterConverter() {}

    public static COBParameter convert(Object obj) {
        if (obj instanceof COBParameter) {
            return (COBParameter) obj;
        } else if (obj instanceof LoanCOBParameter loanCOBParameter) {
            return loanCOBParameter.toCOBParameter();
        }
        return null;
    }
}
