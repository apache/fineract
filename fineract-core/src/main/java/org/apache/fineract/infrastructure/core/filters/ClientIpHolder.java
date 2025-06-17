package org.apache.fineract.infrastructure.core.filters;

public class ClientIpHolder {
    private static final ThreadLocal<String> clientIpHolder = new ThreadLocal<>();

    public static void setClientIp(String ip) {
        clientIpHolder.set(ip);
    }

    public static String getClientIp() {
        return clientIpHolder.get();
    }

    public static void clear() {
        clientIpHolder.remove();
    }
}
