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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Selects proxies like the wrapped selector, after checking that the target host of the request resolves only to
 * publicly routable addresses. OkHttp asks the selector for every target (also for redirects), whether or not a proxy
 * is used, while the DNS lookup only covers the proxy host when a proxy is used.
 */
final class OutboundProxySelector extends ProxySelector {

    private final ProxySelector delegate;
    private final Set<String> proxyHosts = ConcurrentHashMap.newKeySet();

    OutboundProxySelector(final ProxySelector delegate) {
        this.delegate = delegate;
    }

    /**
     * @throws IllegalArgumentException
     *             if the target of the request must not be called
     */
    @Override
    public List<Proxy> select(final URI uri) {
        OutboundAddressValidator.validateUrl(uri.toString(), false);
        if (delegate == null) {
            return List.of(Proxy.NO_PROXY);
        }
        final List<Proxy> proxies = delegate.select(uri);
        for (final Proxy proxy : proxies) {
            if (proxy.address() instanceof InetSocketAddress socketAddress) {
                proxyHosts.add(socketAddress.getHostString().toLowerCase(Locale.ROOT));
            }
        }
        return proxies;
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
        if (delegate != null) {
            delegate.connectFailed(uri, sa, ioe);
        }
    }

    /**
     * @return true if the host was returned as a proxy for an earlier request
     */
    boolean isProxyHost(final String host) {
        return proxyHosts.contains(host.toLowerCase(Locale.ROOT));
    }
}
