package ru.adserver.tcp;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.adserver.service.CaseDispatcher;

import javax.inject.Inject;

import java.util.concurrent.Future;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Service
@ChannelHandler.Sharable
public class AdServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AdServerHandler.class);

    @Inject
    private CaseDispatcher caseDispatcher;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String referer = request.headers().get("Referer");
            if (referer != null) {
                return;
            }
            if (HttpHeaders.is100ContinueExpected(request)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            Future<Pair<String, String>> futurePair = caseDispatcher.processRequest(request.getUri());
            try {
                Pair<String, String> pair = futurePair.get();
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(pair.getLeft(), Charsets.UTF_8));
                response.headers().set(CONTENT_TYPE, pair.getRight());
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

                if (HttpHeaders.isKeepAlive(request)) {
                    response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    ctx.write(response);
                } else {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String message = cause.getMessage();
        logger.error("Error while processing request -> {}", message);
        if (ctx.channel().isActive()) {
            if (message.isEmpty()) {
                message = cause.getClass().getCanonicalName();
            } else if (message.contains(":")) {
                message = message.split(":")[1].trim();
            }
            ByteBuf content = Unpooled.copiedBuffer(message, Charsets.UTF_8);
            HttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, content);
            response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

}