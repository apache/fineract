package org.mifosng.platform.client.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientIdentifierRepository extends
		JpaRepository<ClientIdentifier, Long>,
		JpaSpecificationExecutor<ClientIdentifier> {

	@Query("from ClientIdentifier ci where ci.documentType.id= :documentTypeId and ci.documentKey= :documentKey")
	ClientIdentifier getClientIdentifierByDocumentTypeAndKey(
			@Param("documentTypeId") Long documentTypeId,
			@Param("documentKey") String documentKey);
}