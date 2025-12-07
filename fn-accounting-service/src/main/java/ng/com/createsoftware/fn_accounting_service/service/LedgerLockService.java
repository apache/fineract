package ng.com.createsoftware.fn_accounting_service.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LedgerLockService {
    private final Map<Long, Boolean> locks = new ConcurrentHashMap<>();

    public void lock(Long ledgerId){
        locks.put(ledgerId, true);
    }
    public void unlock(Long ledgerId){
        locks.remove(ledgerId);
    }
    public boolean isLocked(Long ledgerId){
        return locks.getOrDefault(ledgerId, false);
    }
}
