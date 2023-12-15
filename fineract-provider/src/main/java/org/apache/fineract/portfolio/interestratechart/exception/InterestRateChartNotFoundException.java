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
package org.apache.fineract.portfolio.interestratechart.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;

public class InterestRateChartNotFoundException extends AbstractPlatformResourceNotFoundException {

    public InterestRateChartNotFoundException(final Long id) {
        super("error.msg.interest.rate.chart.id.invalid", "Interest rate chart with identifier " + id + " does not exist", id);
    }

    public InterestRateChartNotFoundException(Long id, EmptyResultDataAccessException e) {
        super("error.msg.interest.rate.chart.id.invalid", "Interest rate chart with identifier " + id + " does not exist", id, e);
    }
}
