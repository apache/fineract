package org.mifosplatform.accounting.domain;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

@Repository
public class GLJournalEntryRepositoryImpl implements GLJournalEntryRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<GLJournalEntry> findFirstJournalEntryForAccount(long glAccountId) {
        List<GLJournalEntry> journalEntries = entityManager
                .createQuery("SELECT journalEntry FROM GLJournalEntry journalEntry where journalEntry.glAccount.id= :glAccountId")
                .setParameter("glAccountId", glAccountId).setFirstResult(0).setMaxResults(1).getResultList();
        return journalEntries;
    }
}
