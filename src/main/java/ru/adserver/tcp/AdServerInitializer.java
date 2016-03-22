package ru.adserver.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import javax.inject.Inject;

public class AdServerInitializer extends ChannelInitializer<SocketChannel> {

    @Inject
    private AdServerHandler adServerHandler;

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        DefaultEventExecutorGroup executorGroup = new DefaultEventExecutorGroup(2);
        pipeline.addLast(executorGroup, "handler", adServerHandler);
    }


}