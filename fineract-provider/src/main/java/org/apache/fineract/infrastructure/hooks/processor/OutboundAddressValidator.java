/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.hooks.processor;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Decides whether an outbound (webhook) request may be sent to an address. Only http and https are allowed and, unless
 * private addresses are explicitly permitted, the host must not resolve to a loopback, any-local, link-local,
 * site-local/private, carrier-grade NAT, unique-local or multicast address.
 */
public final class OutboundAddressValidator {

    private OutboundAddressValidator() {}

    /**
     * @throws IllegalArgumentException
     *             if the URL is not an http(s) URL, the host cannot be resolved or (when private addresses are not
     *             allowed) any address of the host is not publicly routable
     */
    public static void validateUrl(final String url, final boolean allowPrivateAddresses) {
        final URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
        final String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new IllegalArgumentException("Only http and https URLs are allowed");
        }
        final String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("URL has no host");
        }
        if (allowPrivateAddresses) {
            return;
        }
        try {
            checkAddresses(Arrays.asList(InetAddress.getAllByName(host)), host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Host cannot be resolved", e);
        }
    }

    /**
     * @return the given addresses if all of them are allowed
     * @throws UnknownHostException
     *             if any of the addresses is not allowed
     */
    public static List<InetAddress> filterResolved(final String host, final List<InetAddress> addresses) throws UnknownHostException {
        final String rejection = rejectionReason(addresses, host);
        if (rejection != null) {
            throw new UnknownHostException(rejection);
        }
        return new ArrayList<>(addresses);
    }

    private static void checkAddresses(final List<InetAddress> addresses, final String host) {
        final String rejection = rejectionReason(addresses, host);
        if (rejection != null) {
            throw new IllegalArgumentException(rejection);
        }
    }

    /**
     * @return why the addresses cannot be used, or null if all of them can
     */
    private static String rejectionReason(final List<InetAddress> addresses, final String host) {
        if (addresses.isEmpty()) {
            return "Host cannot be resolved";
        }
        for (final InetAddress address : addresses) {
            if (isRestricted(address)) {
                return "Host " + host + " resolves to a non-public address";
            }
        }
        return null;
    }

    public static boolean isRestricted(final InetAddress address) {
        if (address.isLoopbackAddress() || address.isAnyLocalAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }
        final byte[] b = address.getAddress();
        if (address instanceof Inet4Address) {
            return isRestrictedIpv4(b);
        }
        if (address instanceof Inet6Address) {
            // fc00::/7 (unique local), fe80::/10 (link-local) and fec0::/10 (deprecated site-local)
            final int first = b[0] & 0xff;
            if ((first & 0xfe) == 0xfc || (first == 0xfe && (b[1] & 0xc0) >= 0x80)) {
                return true;
            }
            final byte[] embedded = embeddedIpv4(b);
            return embedded != null && isRestricted(toIpv4(embedded));
        }
        return false;
    }

    private static boolean isRestrictedIpv4(final byte[] b) {
        final int first = b[0] & 0xff;
        final int second = b[1] & 0xff;
        final int third = b[2] & 0xff;
        // 0.0.0.0/8, 100.64.0.0/10 (carrier-grade NAT), 192.0.0.0/24 (protocol assignments),
        // 198.18.0.0/15 (benchmarking), 240.0.0.0/4 (reserved, includes 255.255.255.255)
        return first == 0 || (first == 100 && (second & 0xc0) == 64) || (first == 192 && second == 0 && third == 0)
                || (first == 198 && (second & 0xfe) == 18) || first >= 240;
    }

    /**
     * @return the IPv4 address embedded in a NAT64 (64:ff9b::/96), 6to4 (2002::/16) or IPv4-compatible (::a.b.c.d)
     *         address, or null if the address does not embed one
     */
    private static byte[] embeddedIpv4(final byte[] b) {
        if (b[0] == 0x00 && b[1] == 0x64 && (b[2] & 0xff) == 0xff && (b[3] & 0xff) == 0x9b && allZero(b, 4, 12)) {
            return Arrays.copyOfRange(b, 12, 16);
        }
        if (b[0] == 0x20 && b[1] == 0x02) {
            return Arrays.copyOfRange(b, 2, 6);
        }
        if (allZero(b, 0, 12)) {
            return Arrays.copyOfRange(b, 12, 16);
        }
        return null;
    }

    private static InetAddress toIpv4(final byte[] address) {
        try {
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            // only thrown for an address of illegal length, which four bytes are not
            throw new IllegalStateException("Invalid IPv4 address length", e);
        }
    }

    private static boolean allZero(final byte[] b, final int from, final int to) {
        for (int i = from; i < to; i++) {
            if (b[i] != 0) {
                return false;
            }
        }
        return true;
    }
}
