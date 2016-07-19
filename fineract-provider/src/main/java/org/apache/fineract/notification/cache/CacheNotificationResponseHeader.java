package org.apache.fineract.notification.cache;


public class CacheNotificationResponseHeader {

    private boolean hasNotifications;
    private Long lastFetch;

    public CacheNotificationResponseHeader() {
    }

    public CacheNotificationResponseHeader(boolean hasNotifications, Long lastFetch) {
        this.hasNotifications = hasNotifications;
        this.lastFetch = lastFetch;
    }

    public boolean hasNotifications() {
        return hasNotifications;
    }

    public void setHasNotifications(boolean hasNotifications) {
        this.hasNotifications = hasNotifications;
    }

    public Long getLastFetch() {
        return lastFetch;
    }

    public void setLastFetch(Long lastFetch) {
        this.lastFetch = lastFetch;
    }
}
