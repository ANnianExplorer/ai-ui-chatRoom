package com.chat.core.common.config;

import com.chat.core.common.handler.ChatWebSocketHandler;
import com.chat.service.GroupService;
import com.chat.service.MessageService;
import com.chat.service.UserService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 实时通讯 netty 服务
 *
 * @author y
 * @since 2026-01-06
 */

@Slf4j
@Component
public class NettyServer implements CommandLineRunner {

    @Value("${netty.port}")
    private Integer nettyPort;

    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private MessageService messageService;


    @Override
    public void run(String... args) {
        // 使用新线程启动Netty服务器，避免阻塞Spring Boot主线程
        Thread nettyThread = new Thread(this::startNettyServer, "NettyServerThread");
        nettyThread.setDaemon(false); // 设置为非守护线程
        nettyThread.start();
        log.info("Netty服务器启动成功，端口：{}", nettyPort);
    }

    /**
     * 启动Netty服务器
     */
    private void startNettyServer() {
        try {
            // 创建Netty工作线程组
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            // 创建Netty工作线程组
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap sb = new ServerBootstrap();
                sb.option(ChannelOption.SO_BACKLOG, 1024)
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .localAddress("0.0.0.0", nettyPort)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                log.info("收到新连接: {}", ch.remoteAddress());
                                ch.pipeline()
                                        // 添加心跳检测，30秒没有读操作则触发
                                        .addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                        .addLast(new HttpServerCodec()) // HTTP请求解码
                                        .addLast(new ChunkedWriteHandler()) // 支持大文件传输
                                        .addLast(new HttpObjectAggregator(8192)) // HTTP请求聚合最大请求长度
                                        .addLast(new ChatWebSocketHandler(userService, groupService, messageService)) // WebSocket处理器
                                        .addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536 * 10));
                            }
                        })
                        .childOption(ChannelOption.SO_KEEPALIVE, true) // 保持长连接
                        .childOption(ChannelOption.TCP_NODELAY, true); // 启用Nagle算法禁用，降低延迟

                // 等待绑定端口并启动服务器
                ChannelFuture cf = sb.bind().sync();
                // 等待服务器通道关闭
                cf.channel().closeFuture().sync();
            } finally {
                // 关闭线程组
                workerGroup.shutdownGracefully().sync();
                bossGroup.shutdownGracefully().sync();
            }
        } catch (InterruptedException e) {
            log.error("Netty服务器启动失败", e);
        }
    }
}
