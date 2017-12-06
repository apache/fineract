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
package org.apache.fineract.infrastructure.bulkimport.populator.comparator;

import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;

import java.util.Comparator;

/**
 * Sorting the loan values based on loan status giving priority to active loans
 * */

public class LoanComparatorByStatusActive implements Comparator<LoanAccountData> {

    @Override
    public int compare(LoanAccountData  o1, LoanAccountData o2) {

        boolean isData1StatusActive = o1.getStatusStringValue().equals("Active");
        boolean isData2StatusActive = o2.getStatusStringValue().equals("Active");

        // if both status active, these have the same rank
        if (isData1StatusActive && isData2StatusActive){
            return 0;
        }

        if (isData1StatusActive){
            return -1;
        }

        if (isData2StatusActive){
            return 1;
        }
        // if no status active, these have the same rank
        return 0;
    }
}