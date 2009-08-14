/**
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3.0 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package openr66.protocol.networkhandler.ssl;

import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLEngine;

import openr66.protocol.configuration.Configuration;
import openr66.protocol.exception.OpenR66ProtocolNoDataException;
import openr66.protocol.networkhandler.packet.NetworkPacketCodec;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.jboss.netty.handler.traffic.GlobalTrafficShapingHandler;

/**
 * NetworkServer pipeline for SSL
 *
 * @author Frederic Bregier
 */
public class NetworkSslServerPipelineFactory implements ChannelPipelineFactory {
    private final boolean isClient;
    private final ExecutorService executorService;

    /**
     *
     * @param isClient
     *            True if this Factory is to be used in Client mode
     */
    public NetworkSslServerPipelineFactory(boolean isClient, ExecutorService executor) {
        super();
        this.isClient = isClient;
        this.executorService = executor;
    }

    @Override
    public ChannelPipeline getPipeline() {
        final ChannelPipeline pipeline = Channels.pipeline();
        // Add SSL handler first to encrypt and decrypt everything.
        // You will need something more complicated to identify both
        // and server in the real world.
        SSLEngine engine;
        SslHandler sslhandler;
        if (isClient) {
            engine = SecureSslContextFactory.getClientContext()
                    .createSSLEngine();
            engine.setUseClientMode(true);
            sslhandler = new SslHandler(engine, this.executorService);
        } else {
            engine = SecureSslContextFactory.getServerContext()
                    .createSSLEngine();
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(true);
            sslhandler = new SslHandler(engine, this.executorService);
        }
        pipeline.addLast("ssl", sslhandler);

        pipeline.addLast("codec", new NetworkPacketCodec());
        GlobalTrafficShapingHandler handler = Configuration.configuration
                .getGlobalTrafficShapingHandler();
        if (handler != null) {
            pipeline.addLast("LIMIT", handler);
            ChannelTrafficShapingHandler trafficChannel = null;
            try {
                trafficChannel =
                    Configuration.configuration
                    .newChannelTrafficShapingHandler();
                pipeline.addLast("LIMITCHANNEL", trafficChannel);
            } catch (OpenR66ProtocolNoDataException e) {
            }
        }
        pipeline.addLast("pipelineExecutor", new ExecutionHandler(
                Configuration.configuration.getServerPipelineExecutor()));
        pipeline.addLast("handler", new NetworkSslServerHandler());
        return pipeline;
    }

}