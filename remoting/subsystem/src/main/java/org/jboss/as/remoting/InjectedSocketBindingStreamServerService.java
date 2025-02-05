/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.as.remoting;

import java.net.InetSocketAddress;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jboss.as.network.ManagedBinding;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.network.SocketBindingManager;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.remoting3.Endpoint;
import org.wildfly.security.auth.server.SaslAuthenticationFactory;
import org.xnio.OptionMap;
import org.xnio.StreamConnection;
import org.xnio.channels.AcceptingChannel;

import javax.net.ssl.SSLContext;

/**
 * {@link AbstractStreamServerService} that uses an injected socket binding.
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class InjectedSocketBindingStreamServerService extends AbstractStreamServerService {

    private final Supplier<SocketBinding> socketBindingSupplier;
    private final String remotingConnectorName;

    InjectedSocketBindingStreamServerService(
            final Consumer<AcceptingChannel<StreamConnection>> streamServerConsumer,
            final Supplier<Endpoint> endpointSupplier,
            final Supplier<SaslAuthenticationFactory> saslAuthenticationFactorySupplier,
            final Supplier<SSLContext> sslContextSupplier,
            final Supplier<SocketBindingManager> socketBindingManagerSupplier,
            final Supplier<SocketBinding> socketBindingSupplier,
            final OptionMap connectorPropertiesOptionMap,
            final String remotingConnectorName) {
        super(streamServerConsumer, endpointSupplier, saslAuthenticationFactorySupplier,
                sslContextSupplier, socketBindingManagerSupplier, connectorPropertiesOptionMap);
        this.socketBindingSupplier = socketBindingSupplier;
        this.remotingConnectorName = remotingConnectorName;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        super.start(context);
        RemotingConnectorBindingInfoService.install(context.getChildTarget(), remotingConnectorName, getSocketBinding(), Protocol.REMOTE);
    }

    @Override
    public void stop(final StopContext context) {
        super.stop(context);
    }

    @Override
    InetSocketAddress getSocketAddress() {
        return socketBindingSupplier.get().getSocketAddress();
    }

    @Override
    ManagedBinding registerSocketBinding(SocketBindingManager socketBindingManager) {
        ManagedBinding binding = ManagedBinding.Factory.createSimpleManagedBinding(socketBindingSupplier.get());
        socketBindingManager.getNamedRegistry().registerBinding(binding);
        return binding;
    }

    @Override
    void unregisterSocketBinding(ManagedBinding managedBinding, SocketBindingManager socketBindingManager) {
        socketBindingManager.getNamedRegistry().unregisterBinding(managedBinding);
    }

    /**
     * Returns the socket binding applicable for this {@link InjectedSocketBindingStreamServerService}
     * @return
     */
    public SocketBinding getSocketBinding() {
        return this.socketBindingSupplier.get();
    }

}
