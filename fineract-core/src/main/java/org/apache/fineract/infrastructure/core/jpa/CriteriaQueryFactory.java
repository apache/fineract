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

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CriteriaQueryFactory {

    private final EntityManager entityManager;

    public List<Order> ordersFromPageable(Pageable pageable, CriteriaBuilder cb, Root<?> root) {
        return ordersFromPageable(pageable, cb, root, () -> null);
    }

    public List<Order> ordersFromPageable(Pageable pageable, CriteriaBuilder cb, Root<?> root, Supplier<Order> defaultOrderSupplier) {
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

    public <S, U> Page<S> readPage(TypedQuery<S> query, Class<U> domainClass, Pageable pageable, Specification<U> spec) {

        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return PageableExecutionUtils.getPage(query.getResultList(), pageable, () -> executeCountQuery(getCountQuery(spec, domainClass)));
    }

    private static long executeCountQuery(TypedQuery<Long> query) {
        List<Long> totals = query.getResultList();
        long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }
        return total;
    }

    private <S> TypedQuery<Long> getCountQuery(Specification<S> spec, Class<S> domainClass) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);

        Root<S> root = query.from(domainClass);

        applySpecificationToCriteria(root, spec, query);

        if (query.isDistinct()) {
            query.select(builder.countDistinct(root));
        } else {
            query.select(builder.count(root));
        }

        // Remove all Orders the Specifications might have applied
        query.orderBy(Collections.emptyList());

        return entityManager.createQuery(query);
    }

    public <S, U> Root<U> applySpecificationToCriteria(Root<U> root, Specification<U> spec, CriteriaQuery<S> query) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        Predicate predicate = spec.toPredicate(root, query, builder);

        if (predicate != null) {
            query.where(predicate);
        }

        return root;
    }
}
