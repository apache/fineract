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
package org.apache.fineract.cob.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.fineract.cob.converter.COBParameterConverter;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.resolver.CatchUpFlagResolver;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.lang.NonNull;

/**
 * Shared COB worker reader logic: from the {@link COBParameter} stored in the step execution context, resolve the
 * account ids in range that still need processing and retain only those locked by the COB chunk-processing owner. Both
 * the loan and savings COB readers delegate here, parameterizing only the domain-specific pieces (parameter key,
 * id-retrieval and locked-id lookup).
 */
public final class RemainingDataFilter {

    private RemainingDataFilter() {}

    public static LinkedBlockingQueue<Long> filterRemainingData(@NonNull StepExecution stepExecution, String cobParameterKey,
            BiFunction<COBParameter, Boolean, List<Long>> retrieveByParameter, Function<List<Long>, List<Long>> findLockedIds) {
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        COBParameter cobParameter = COBParameterConverter.convert(executionContext.get(cobParameterKey));
        boolean isCatchUp = CatchUpFlagResolver.resolve(stepExecution);
        List<Long> ids;
        if (Objects.isNull(cobParameter)
                || (Objects.isNull(cobParameter.getMinAccountId()) && Objects.isNull(cobParameter.getMaxAccountId()))
                || (cobParameter.getMinAccountId().equals(0L) && cobParameter.getMaxAccountId().equals(0L))) {
            ids = Collections.emptyList();
        } else {
            ids = new ArrayList<>(retrieveByParameter.apply(cobParameter, isCatchUp));
            if (!ids.isEmpty()) {
                List<Long> lockedIds = findLockedIds.apply(ids);
                ids.retainAll(lockedIds);
            }
        }
        return new LinkedBlockingQueue<>(ids);
    }
}
