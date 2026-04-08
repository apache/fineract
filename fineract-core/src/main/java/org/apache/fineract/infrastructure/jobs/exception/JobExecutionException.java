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
package org.apache.fineract.infrastructure.jobs.exception;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.exception.MultiException;

public class JobExecutionException extends MultiException {

    public JobExecutionException(String message, List<Throwable> problems) {
        super(message, problems);
    }

    public JobExecutionException(List<Throwable> problems) {
        super(problems);
    }

    public JobExecutionException(MultiException multiException) {
        super(multiException.getCauses());
    }

    public static void throwErrors(@NotNull Map<Throwable, List<String>> errorMap) throws JobExecutionException {
        int size = errorMap.size();
        if (size == 0) {
            return;
        }
        List<Throwable> errors;
        StringBuilder msg = new StringBuilder("Job failed on ");
        Stream<Map.Entry<Throwable, List<String>>> entryStream = errorMap.entrySet().stream().filter(e -> e.getValue() != null);
        if (size < 10) {
            errors = new ArrayList<>(errorMap.keySet());
            Map<String, List<List<String>>> errorTypes = entryStream
                    .collect(Collectors.groupingBy(e -> e.getKey().getClass().getSimpleName(), mapping(Map.Entry::getValue, toList())));
            errorTypes.forEach((key, value) -> msg.append(key).append(':').append(value.size()).append(':')
                    .append(value.stream().flatMap(List::stream).collect(Collectors.joining(","))).append(";\n"));
        } else {
            errors = List.of(errorMap.keySet().iterator().next());
            Map<String, Long> errorTypes = entryStream
                    .collect(Collectors.groupingBy(e -> e.getKey().getClass().getSimpleName(), Collectors.counting()));
            errorTypes.forEach((key, value) -> msg.append(key).append(':').append(value).append(";\n"));
        }
        throw new JobExecutionException(msg.toString(), errors);
    }
}
