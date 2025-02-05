/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.as.controller.remote;

import java.io.IOException;

import org.jboss.as.controller.client.OperationAttachments;
import org.jboss.as.controller.client.OperationMessageHandler;
import org.jboss.as.protocol.mgmt.ManagementChannelHandler;
import org.jboss.dmr.ModelNode;

/**
 * Factory to create a {@link TransactionalProtocolClient}.
 *
 * @author Emanuel Muckenhuber
 */
public final class TransactionalProtocolHandlers {

    private TransactionalProtocolHandlers() {
        //
    }

    /**
     * Create a transactional protocol client.
     *
     * @param channelAssociation the channel handler
     * @return the transactional protocol client
     */
    public static TransactionalProtocolClient createClient(final ManagementChannelHandler channelAssociation) {
        final TransactionalProtocolClientImpl client = new TransactionalProtocolClientImpl(channelAssociation);
        channelAssociation.addHandlerFactory(client);
        return client;
    }

    /**
     * Wrap an operation's parameters in a simple encapsulating object
     * @param operation  the operation
     * @param messageHandler the message handler
     * @param attachments  the attachments
     * @return  the encapsulating object
     */
    public static TransactionalProtocolClient.Operation wrap(final ModelNode operation, final OperationMessageHandler messageHandler, final OperationAttachments attachments) {
        return new TransactionalOperationImpl(operation, messageHandler, attachments);
    }

    /**
     * Execute blocking for a prepared result.
     *
     * @param operation the operation to execute
     * @param client the protocol client
     * @return the prepared operation
     * @throws IOException
     * @throws InterruptedException
     */
    public static TransactionalProtocolClient.PreparedOperation<TransactionalProtocolClient.Operation> executeBlocking(final ModelNode operation, TransactionalProtocolClient client) throws IOException, InterruptedException {
        final BlockingQueueOperationListener<TransactionalProtocolClient.Operation> listener = new BlockingQueueOperationListener<>();
        client.execute(listener, operation, OperationMessageHandler.DISCARD, OperationAttachments.EMPTY);
        return listener.retrievePreparedOperation();
    }

}
