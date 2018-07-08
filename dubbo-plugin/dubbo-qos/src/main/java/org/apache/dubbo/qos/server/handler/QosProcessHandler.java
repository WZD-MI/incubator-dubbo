/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.qos.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class QosProcessHandler extends ByteToMessageDecoder {

    private ScheduledFuture<?> welcomeFuture;

    private String welcome;
    // true means to accept foreign IP
    private boolean acceptForeignIp;

    public static String prompt = "dubbo>";

    public QosProcessHandler(String welcome, boolean acceptForeignIp) {
        this.welcome = welcome;
        this.acceptForeignIp = acceptForeignIp;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        //这里起一个schedule 不是为了效率,而是为了后边方便关闭,因为http 不需要输出这个欢迎词
        welcomeFuture = ctx.executor().schedule(new Runnable() {

            @Override
            public void run() {
                if (welcome != null) {
                    ctx.write(Unpooled.wrappedBuffer(welcome.getBytes()));
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(prompt.getBytes()));
                }
            }

        }, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * 虽说就一个解码器,但到解码的时候,会根据magic code切换成不通的解码方式,可以是 telnet也可以是http
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 1) {
            return;
        }

        // read one byte to guess protocol
        final int magic = in.getByte(in.readerIndex());//支持两种编码  (http 或者  telnet)

        ChannelPipeline p = ctx.pipeline();
        p.addLast(new LocalHostPermitHandler(acceptForeignIp));
        if (isHttp(magic)) {//根据magic code 进行编码切换
            // no welcome output for http protocol
            if (welcomeFuture != null && welcomeFuture.isCancellable()) {
                welcomeFuture.cancel(false);//关闭欢迎词输出
            }
            //添加http的编解码和业务处理
            p.addLast(new HttpServerCodec());
            p.addLast(new HttpObjectAggregator(1048576));
            p.addLast(new HttpProcessHandler());//http 的业务处理handler
            p.remove(this);//移除自己
        } else {
            //添加telenet的编解码
            p.addLast(new LineBasedFrameDecoder(2048));
            p.addLast(new StringDecoder(CharsetUtil.UTF_8));
            p.addLast(new StringEncoder(CharsetUtil.UTF_8));
            p.addLast(new IdleStateHandler(0, 0, 5 * 60));
            p.addLast(new TelnetProcessHandler());//telnet 的业务处理handler
            p.remove(this);//移除自己
        }
    }

    // G for GET, and P for POST
    private static boolean isHttp(int magic) {
        return magic == 'G' || magic == 'P';
    }
}
