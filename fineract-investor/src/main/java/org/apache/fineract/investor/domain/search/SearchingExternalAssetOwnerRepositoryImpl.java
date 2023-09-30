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
package org.apache.fineract.investor.domain.search;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.jpa.CriteriaQueryFactory;
import org.apache.fineract.infrastructure.core.service.PagedRequest;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferDetails;
import org.apache.fineract.investor.service.search.domain.ExternalAssetOwnerSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchingExternalAssetOwnerRepositoryImpl implements SearchingExternalAssetOwnerRepository {

    private final EntityManager entityManager;
    private final CriteriaQueryFactory criteriaQueryFactory;

    @Override
    public Page<SearchedExternalAssetOwner> searchInvestorData(PagedRequest<ExternalAssetOwnerSearchRequest> searchRequest) {
        final ExternalAssetOwnerSearchRequest request = searchRequest.getRequest().get();
        final Pageable pageable = searchRequest.toPageable();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SearchedExternalAssetOwner> query = cb.createQuery(SearchedExternalAssetOwner.class);

        Root<ExternalAssetOwnerTransfer> root = query.from(ExternalAssetOwnerTransfer.class);
        Path<ExternalAssetOwnerTransferDetails> details = root.join("externalAssetOwnerTransferDetails", JoinType.LEFT);
        Path<ExternalAssetOwner> owner = root.get("owner");

        Specification<ExternalAssetOwnerTransfer> spec = (r, q, builder) -> {
            Path<ExternalAssetOwner> o = r.get("owner");

            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(request.getText())) {
                String searchLikeValue = "%" + request.getText() + "%";
                predicates.add(cb.or(cb.like(r.get("externalId"), searchLikeValue), cb.like(o.get("externalId"), searchLikeValue),
                        cb.like(r.get("externalLoanId"), searchLikeValue)));
            }

            if (request.getSubmittedFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(r.get("settlementDate"), request.getSubmittedFromDate()));
            }
            if (request.getSubmittedToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(r.get("settlementDate"), request.getSubmittedToDate()));
            }

            if (request.getEffectiveFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(r.get("effectiveDateFrom"), request.getEffectiveFromDate()));
            }
            if (request.getEffectiveToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(r.get("effectiveDateTo"), request.getEffectiveToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        criteriaQueryFactory.applySpecificationToCriteria(root, spec, query);

        List<Order> orders = criteriaQueryFactory.ordersFromPageable(pageable, cb, root, () -> cb.desc(root.get("settlementDate")));
        query.orderBy(orders);

        query.select(cb.construct(SearchedExternalAssetOwner.class, root.get("id"), root.get("loanId"), root.get("externalLoanId"),
                owner.get("externalId"), root.get("externalId"), root.get("status"), root.get("subStatus"), root.get("purchasePriceRatio"),
                root.get("settlementDate"), root.get("effectiveDateFrom"), root.get("effectiveDateTo"), details.get("id"),
                details.get("totalOutstanding"), details.get("totalPrincipalOutstanding"), details.get("totalInterestOutstanding"),
                details.get("totalFeeChargesOutstanding"), details.get("totalPenaltyChargesOutstanding"), details.get("totalOverpaid")));

        TypedQuery<SearchedExternalAssetOwner> queryToExecute = entityManager.createQuery(query);
        return criteriaQueryFactory.readPage(queryToExecute, ExternalAssetOwnerTransfer.class, pageable, spec);
    }

}
