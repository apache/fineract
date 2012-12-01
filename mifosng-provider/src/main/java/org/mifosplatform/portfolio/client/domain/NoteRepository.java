package org.mifosplatform.portfolio.client.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

	List<Note> findByLoanId(Long id);
	
	List<Note> findByClientId(Long id);
}