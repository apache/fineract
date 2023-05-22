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
package org.apache.fineract.infrastructure.core.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class CriteriaQueryFactory {

    public List<Order> fromPageable(Pageable pageable, CriteriaBuilder cb, Root<?> root) {
        return fromPageable(pageable, cb, root, () -> null);
    }

    public List<Order> fromPageable(Pageable pageable, CriteriaBuilder cb, Root<?> root, Supplier<Order> defaultOrderSupplier) {
        List<Order> orders = new ArrayList<>();
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            for (Sort.Order order : sort) {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            }
        } else {
            Order defaultOrder = defaultOrderSupplier.get();
            if (defaultOrder != null) {
                orders.add(defaultOrder);
            }
        }
        return orders;
    }
}
