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
package org.apache.fineract.portfolio.savings.service;

import org.springframework.stereotype.Service;

@Service
public class NubanAccountServiceImpl implements NubanAccountService {

    @Override
    public String generateNextSerialNumber(String serialNumber) {
        final int maxLength = 9;
        Integer newNumber = Integer.parseInt(serialNumber) + 1;
        String nextSerialNumber = newNumber.toString();
        while (nextSerialNumber.length() < maxLength) {
            nextSerialNumber = "0" + nextSerialNumber;
        }
        return nextSerialNumber;
    }

    /**
     * The NUBAN code of a typical customer of the SLSBank would be derived as follows: VFD Group code is 566 Assume a
     * NUBAN serial number of 000021457 in VFD Group The check digit would be computed as follows: Step 1:
     * 5*3+6*7+6*3+0*3+0*7+0*3+0*3+2*7+1*3+4*3+5*7+7*3 = 160 Step 2: Modulo 10 of 160 is 0 i.e. 0 is the remainder when
     * you divide 160 by 10 Step 3: Subtract 0 from 10 to get 10 Step 4: So the check digit is 0 (if the result is 10
     * use 0 as the check digit) Therefore, the NUBAN code for this illustration is 000021457-0.
     *
     * @param serialNumber
     *            Serial number
     * @param prefix
     *            Account prefix
     * @return NUBAN account number
     */
    @Override
    public String generateNubanAccountNumber(String serialNumber, String prefix) {
        int[] nubanMultipliers = new int[] { 3, 7, 3, 3, 7, 3, 3, 7, 3, 3, 7, 3 };
        serialNumber = prefix + serialNumber.substring(1);
        String extendSerialNumber = UNITED_CAPITAL_UNIQUE_CODE_IDENTIFIER + serialNumber;
        // Step1
        int digit = 0;
        for (int i = 0; i < nubanMultipliers.length; i++) {
            int num = Integer.parseInt(extendSerialNumber.charAt(i) + "");
            digit += num * nubanMultipliers[i];
        }
        // Step2 & 3
        digit = 10 - (digit % 10);
        if (digit == 10) digit = 0;
        return serialNumber + digit;
    }
}
