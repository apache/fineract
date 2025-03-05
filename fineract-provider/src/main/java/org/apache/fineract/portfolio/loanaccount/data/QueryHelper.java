package org.apache.fineract.portfolio.loanaccount.data;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public class QueryHelper {

    public static <E, J, T> Predicate equal(From<E, J> from, CriteriaQuery<?> query, CriteriaBuilder builder, String field, T value) {
        return builder.equal(from.get(field), value);
    }

    public static <E, J, T> Predicate in(From<E, J> from, CriteriaQuery<?> query, CriteriaBuilder builder, String field,
            Collection<T> values) {
        return from.get(field).in(values);
    }

    public static <E, J, T> Predicate notIn(From<E, J> from, CriteriaQuery<?> query, CriteriaBuilder builder, String field,
            Collection<T> value) {
        return builder.not(in(from, query, builder, field, value));
    }

    public static <E, J, T> Predicate likeWildcard(From<E, J> from, CriteriaQuery<?> query, CriteriaBuilder builder, String field,
            T value) {
        return builder.like(from.get(field).as(String.class), "%" + value + "%");
    }

    public static <E, J, T> Predicate likeWildcardEnd(From<E, J> from, CriteriaQuery<?> query, CriteriaBuilder builder, String field,
            T value) {
        return builder.like(from.get(field).as(String.class), value + "%");
    }

    public static <E, D> Page<E> query(JpaSpecificationExecutor<E> repository, D queryDTO, QueryDTOMapper<E, D> queryDTOMapper,
            Pageable pageable) {
        return repository.findAll((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            final Map<String, QueryMapping<E, D, ?>> queryMapping = queryDTOMapper.getQueryMapping();
            for (Map.Entry<String, QueryMapping<E, D, ?>> entry : queryMapping.entrySet()) {
                final QueryMapping value = entry.getValue();
                final Object queryValue = value.getParamExtractor().apply(queryDTO);
                if (queryValue != null && (!(queryValue instanceof Collection<?> querryCollection) || !querryCollection.isEmpty())) {
                    predicates.add(value.getQueryParamToPredicate().toPredicate(root, query, builder, entry.getKey(),
                            value.getParamExtractor().apply(queryDTO)));
                }
            }

            return builder.and(predicates.toArray(new Predicate[] {}));
        }, pageable);
    }
}
