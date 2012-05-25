package org.mifosng.platform;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.Note;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanTransaction;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.Organisation;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.Role;
import org.springframework.data.jpa.domain.Specification;

public final class Specifications {
	
	public static Specification<AppUser> usersThatMatch(final Organisation organisation, final Long id) {
		return new Specification<AppUser>() {

			@Override
			public Predicate toPredicate(final Root<AppUser> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("organisation"), organisation), cb.equal(root.get("id"), id));
			}
		};
	}
	
	public static Specification<AppUser> usersThatMatch(final Organisation organisation, final String username) {
		return new Specification<AppUser>() {

			@Override
			public Predicate toPredicate(final Root<AppUser> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("organisation"), organisation), cb.equal(root.get("username"), username));
			}
		};
	}
	
	public static Specification<Role> rolesThatMatch(final Organisation organisation, final Long id) {
		return new Specification<Role>() {

			@Override
			public Predicate toPredicate(final Root<Role> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("organisation"), organisation), cb.equal(root.get("id"), id));
			}
		};
	}
	
	public static Specification<LoanTransaction> loanTransactionsThatMatch(final Organisation organisation, final Long id) {
		return new Specification<LoanTransaction>() {
			@Override
			public Predicate toPredicate(final Root<LoanTransaction> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("organisation"), organisation), cb.equal(root.get("id"), id));
			}
		};
	}

	public static Specification<Loan> loansThatMatch(final Organisation organisation, final Long id) {
		return new Specification<Loan>() {
			@Override
			public Predicate toPredicate(final Root<Loan> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("organisation"), organisation), cb.equal(root.get("id"), id));
			}
		};
	}
	
	public static Specification<Loan> loansThatMatch(final Organisation organisation, final Client client) {
		return new Specification<Loan>() {

			@Override
			public Predicate toPredicate(final Root<Loan> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("organisation"), organisation), cb.equal(root.get("client"), client));
			}
		};
	}
	
	public static Specification<LoanProduct> productThatMatches(final Organisation organisation, final Long id) {
		return new Specification<LoanProduct>() {
			@Override
			public Predicate toPredicate(final Root<LoanProduct> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("organisation"), organisation), cb.equal(root.get("id"), id));
			}
		};
	}
	
	public static Specification<Office> officesThatMatch(final Organisation organisation, final Long id) {
		return new Specification<Office>() {
			@Override
			public Predicate toPredicate(final Root<Office> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("organisation"), organisation), cb.equal(root.get("id"), id));
			}
		};
	}
	
	public static Specification<Client> clientsThatMatch(final Organisation organisation, final Long id) {
		return new Specification<Client>() {

			@Override
			public Predicate toPredicate(final Root<Client> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("organisation"), organisation), cb.equal(root.get("id"), id));
			}
		};
	}
	
	public static Specification<Note> notesThatMatch(final Organisation organisation, final Long id) {
		return new Specification<Note>() {

			@Override
			public Predicate toPredicate(final Root<Note> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.and(cb.equal(root.get("organisation"), organisation), cb.equal(root.get("id"), id));
			}
		};
	}
}
