package ng.com.createsoftware.fn_postings_service.repository;

import ng.com.createsoftware.fn_postings_service.model.PostingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingTransactionRepository extends JpaRepository<PostingTransaction, String> {
}
