package com.github.shenwii;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TCP转发服务类
 * @author shenwii
 */
public class TcpForwardServer {

    /**
     * 缓存大小
     */
    private final static int BUFFER_SIZE = 4 * 1024 * 1024;
    /**
     * 服务端Channel
     */
    private final ServerSocketChannel serverChannel;
    /**
     * 用于双向绑定2个SocketChannel的map
     */
    private final Map<SocketChannel, SocketChannel> map = new HashMap<>();
    /**
     * 转发的目标地址
     */
    private final String forwardAddress;
    /**
     * 转发的目标端口
     */
    private final int forwardPort;

    public TcpForwardServer(String bindAddress, int bindPort, String forwardAddress, int forwardPort) throws IOException {
        this.forwardAddress = forwardAddress;
        this.forwardPort = forwardPort;
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(bindAddress, bindPort));
        System.out.println(String.format("Server started on %s:%d", bindAddress, bindPort));
    }

    /**
     * 死循环监听处理事件
     * @throws IOException IO异常
     */
    public void startForever() throws IOException {
        final Map<SocketChannel, SocketChannel> map = new HashMap<>();
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        while(true) {
            selector.select();
            Set<SelectionKey> keysSet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keysSet.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) {
                        onAccepted(selector, key);
                    }
                    if(key.isConnectable()) {
                        onConnected(selector, key);
                    }
                    if(key.isReadable()) {
                        onRead(selector, key);
                    }
                    if (key.isWritable()) {
                        onWrote(selector, key);
                    }
                } catch (IOException | CancelledKeyException e) {
                    key.cancel();
                    SocketChannel client1 = (SocketChannel) key.channel();
                    if(map.containsKey(client1)) {
                        SocketChannel client2 = map.get(client1);
                        try {
                            client2.close();
                        } catch (IOException ex) {}
                        map.remove(client2);
                    }
                    try {
                        client1.close();
                    } catch (IOException cex) {}
                    map.remove(client1);
                }
            }
        }
    }

    /**
     * 当有新的连接时的事件
     * @param selector NIO选择器
     * @param key 事件对象
     * @throws IOException IO异常
     */
    private void onAccepted(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        SocketChannel forwardClient = SocketChannel.open();
        forwardClient.configureBlocking(false);
        //连接目标服务器
        forwardClient.connect(new InetSocketAddress(forwardAddress, forwardPort));
        forwardClient.register(selector, SelectionKey.OP_CONNECT);
        //将2个SocketChannel双向绑定
        map.put(client, forwardClient);
        map.put(forwardClient, client);
    }

    /**
     * 当连接完成时候的事件
     * @param selector NIO选择器
     * @param key 事件对象
     * @throws IOException IO异常
     */
    private void onConnected(Selector selector, SelectionKey key) throws IOException {
        SocketChannel client1 = (SocketChannel) key.channel();
        if(!map.containsKey(client1))
            return;
        SocketChannel client2 = map.get(client1);
        if(client1.finishConnect()) {
            //当连接成功时，将双向的SocketChannel注册上读取事件
            key.interestOps(SelectionKey.OP_READ);
            client2.register(selector, SelectionKey.OP_READ);
        } else {
            //当连接失败时，关闭
            try {
                client2.close();
            } catch (IOException ex) {}
            map.remove(client2);
            try {
                client1.close();
            } catch (IOException cex) {}
            map.remove(client1);
        }
    }

    /**
     * 当有数据需要读时的事件
     * @param selector NIO选择器
     * @param key 事件对象
     * @throws IOException IO异常
     */
    private void onRead(Selector selector, SelectionKey key) throws IOException {
        SocketChannel client1 = (SocketChannel) key.channel();
        if(!map.containsKey(client1))
            return;
        SocketChannel client2 = map.get(client1);
        SelectionKey key2 = client2.keyFor(selector);
        if(key2 == null)
            return;
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.clear();
        int read = client1.read(buffer);
        if(read >= 0) {
            //正常读取数据
            buffer.flip();
            key2.attach(buffer);
            //将对向的SocketChannel注册写入事件
            key2.interestOps(key2.interestOps() | SelectionKey.OP_WRITE);
            //取消自己的SocketChannel的读取事件（直到对向的SocketChannel写入完成后，再次注册读取事件）
            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
        } else {
            //流被关闭
            try {
                client2.close();
            } catch (IOException ex) {}
            map.remove(client2);
            try {
                client1.close();
            } catch (IOException cex) {}
            map.remove(client1);
        }
    }

    /**
     * 当可以写入时的事件
     * @param selector NIO选择器
     * @param key 事件对象
     * @throws IOException IO异常
     */
    private void onWrote(Selector selector, SelectionKey key) throws IOException {
        SocketChannel client1 = (SocketChannel) key.channel();
        if(!map.containsKey(client1))
            return;
        SocketChannel client2 = map.get(client1);
        SelectionKey key2 = client2.keyFor(selector);
        if(key2 == null)
            return;
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        if(buffer.hasRemaining()) {
            //写入数据
            client1.write(buffer);
        } else {
            //写入完成
            //将写入事件取消
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            //将对向的SocketChannel重新注册读取事件
            key2.interestOps(key2.interestOps() | SelectionKey.OP_READ);
        }
    }
}
