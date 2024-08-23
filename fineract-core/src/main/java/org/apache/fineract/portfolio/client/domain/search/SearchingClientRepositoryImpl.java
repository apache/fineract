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
package org.apache.fineract.portfolio.client.domain.search;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.jpa.CriteriaQueryFactory;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientIdentifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchingClientRepositoryImpl implements SearchingClientRepository {

    private final EntityManager entityManager;
    private final CriteriaQueryFactory criteriaQueryFactory;

    @Override
    public Page<SearchedClient> searchByText(String searchText, Pageable pageable, String officeHierarchy) {
        /*
         * this whole thing can be replaced with Spring Data JPA 3+ with a findBy(Specification, Pageable) call but at
         * this point the upgrade is too costly
         *
         * https://github.com/spring-projects/spring-data-jpa/issues/2499
         */
        String hierarchyLikeValue = officeHierarchy + "%";

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SearchedClient> query = cb.createQuery(SearchedClient.class);

        Root<Client> root = query.from(Client.class);
        Path<Office> office = root.get("office");

        Specification<Client> spec = (r, q, builder) -> {
            Path<Office> o = r.get("office");
            Join<Client, ClientIdentifier> identity = r.join("identifiers", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(o.get("hierarchy"), hierarchyLikeValue));

            String searchLikeValue = "%" + searchText + "%";
            predicates.add(cb.or(cb.like(r.get("accountNumber"), searchLikeValue), cb.like(r.get("displayName"), searchLikeValue),
                    cb.like(r.get("externalId"), searchLikeValue), cb.like(r.get("mobileNo"), searchLikeValue),
                    cb.like(identity.get("documentKey"), searchLikeValue)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        criteriaQueryFactory.applySpecificationToCriteria(root, spec, query);

        List<Order> orders = criteriaQueryFactory.ordersFromPageable(pageable, cb, root, () -> cb.desc(root.get("id")));
        query.orderBy(orders);

        query.select(cb.construct(SearchedClient.class, root.get("id"), root.get("displayName"), root.get("externalId"),
                root.get("accountNumber"), office.get("id"), office.get("name"), root.get("mobileNo"), root.get("status"),
                root.get("activationDate"), root.get("createdDate")));

        TypedQuery<SearchedClient> queryToExecute = entityManager.createQuery(query);

        return criteriaQueryFactory.readPage(queryToExecute, Client.class, pageable, spec);
    }
}
