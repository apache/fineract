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
package org.apache.fineract.cob.loan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.COBPropertyService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

@RequiredArgsConstructor
public class LoanCOBPartitioner implements Partitioner {

    private final LoanRepository loanRepository;
    private final COBPropertyService cobPropertyService;

    private static final String PARTITION_PREFIX = "partition";
    private static final String JOB_NAME = "LOAN_COB";

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int partitionCount = cobPropertyService.getPartitionSize(JOB_NAME);
        Map<String, ExecutionContext> partitions = new HashMap<>();
        for (int i = 0; i < partitionCount; i++) {
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.put("loanIds", new ArrayList<Integer>());
            partitions.put(PARTITION_PREFIX + i, executionContext);
        }

        List<Integer> allNonClosedLoanIds = loanRepository.findAllNonClosedLoanIds();
        for (int i = 0; i < allNonClosedLoanIds.size(); i++) {
            String key = PARTITION_PREFIX + (i % partitionCount);
            ExecutionContext executionContext = partitions.get(key);
            List<Integer> data = (List<Integer>) executionContext.get("loanIds");
            data.add(allNonClosedLoanIds.get(i));
        }
        return partitions;
    }
}
