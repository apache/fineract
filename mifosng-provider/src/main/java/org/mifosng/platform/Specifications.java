package org.mifosng.platform;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.loan.domain.Loan;
import org.springframework.data.jpa.domain.Specification;

@SuppressWarnings("unused")
public final class Specifications {
	
	public static Specification<Loan> loansThatMatch(final Client client) {
		return new Specification<Loan>() {

			@Override
			public Predicate toPredicate(final Root<Loan> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.equal(root.get("client"), client);
			}
		};
	}
}