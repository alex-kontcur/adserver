package ru.adserver.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * TcpServer
 *
 * @author Kontsur Alex (bona)
 * @since 12.11.2015
 */
public class AdTcpServer {

    private Channel serverChannel;

    private int serverPort;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private Map<ChannelOption, Object> channelOptions;

    private Map<ChannelOption, Object> childChannelOptions;

    private ChannelHandler channelInitializer;

    @PostConstruct
    public void init() throws InterruptedException, NoSuchAlgorithmException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(channelInitializer);
        for (Map.Entry<ChannelOption, Object> channelOption : channelOptions.entrySet()) {
            bootstrap.option(channelOption.getKey(), channelOption.getValue());
        }
        for (Map.Entry<ChannelOption, Object> channelOption : childChannelOptions.entrySet()) {
            bootstrap.childOption(channelOption.getKey(), channelOption.getValue());
        }
        ChannelFuture channelFuture = bootstrap.bind(serverPort).sync();
        serverChannel = channelFuture.channel();
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        serverChannel.close();
    }

    public void setBossGroup(EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
    }

    public void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public void setChannelOptions(Map<ChannelOption, Object> channelOptions) {
        this.channelOptions = new HashMap<>(channelOptions);
    }

    public void setChildChannelOptions(Map<ChannelOption, Object> childChannelOptions) {
        this.childChannelOptions = new HashMap<>(childChannelOptions);
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setChannelInitializer(ChannelHandler channelInitializer) {
        this.channelInitializer = channelInitializer;
    }

}
