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
package org.apache.fineract.infrastructure.event.external.service.serialization;

import java.util.Comparator;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

public class BusinessEventSerializerComparator implements Comparator<BusinessEventSerializer> {

    @Override
    public int compare(BusinessEventSerializer o1, BusinessEventSerializer o2) {
        int o1Order = getOrderOrDefault(o1);
        int o2Order = getOrderOrDefault(o2);
        return Integer.compare(o1Order, o2Order);
    }

    private int getOrderOrDefault(BusinessEventSerializer serializer) {
        Order orderAnnotation = serializer.getClass().getAnnotation(Order.class);
        if (orderAnnotation != null) {
            return orderAnnotation.value();
        } else {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }
}
