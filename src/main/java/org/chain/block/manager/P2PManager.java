package org.chain.block.manager;

import com.alibaba.fastjson.JSON;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.chain.block.domain.Block;
import org.chain.block.domain.Message;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * P2P Socket连接管理
 *   用于广播及接收区块数据
 * @author  宇文芬
 */
public class P2PManager {
    private List<WebSocket> sockets;
    private BlockManager blockManager;
    private final static int QUERY_LATEST        = 0;
    private final static int QUERY_ALL           = 1;
    private final static int RESPONSE_BLOCK_CHAIN = 2;

    public P2PManager(BlockManager blockManager) {
        this.blockManager = blockManager;
        this.sockets = new CopyOnWriteArrayList<WebSocket>();
    }

    /**
     * 启动 WebSocketServer, 注册端口监听服务
     * @param port
     */
    public void initP2PServer(int port) {
        final WebSocketServer socket = new WebSocketServer(new InetSocketAddress(port)) {
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                write(webSocket, queryChainLengthMsg());
                sockets.add(webSocket);
            }

            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
                sockets.remove(webSocket);
            }

            public void onMessage(WebSocket webSocket, String s) {
                handleMessage(webSocket, s);
            }

            public void onError(WebSocket webSocket, Exception e) {
                System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
                sockets.remove(webSocket);
            }

            public void onStart() {

            }
        };
        socket.start();
        System.out.println("listening webSocket p2p port on: " + port);
    }

    /**
     * 连接客户端
     * @param peer
     */
    public void connectToPeer(String peer) {
        try {
            final WebSocketClient socket = new WebSocketClient(new URI(peer)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    write(this, queryChainLengthMsg());
                    sockets.add(this);
                }

                @Override
                public void onMessage(String s) {
                    handleMessage(this, s);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    System.out.println("connection failed");
                    sockets.remove(this);
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("connection failed");
                    sockets.remove(this);
                }
            };
            socket.connect();
        } catch (URISyntaxException e) {
            System.out.println("p2p connect is error:" + e.getMessage());
        }
    }

    /**
     * 获取所有的链接
     * @return
     */
    public List<WebSocket> getSockets() {
        return sockets;
    }

    /**
     * 消息广播
     * @param message
     */
    public void broadcast(String message) {
        for (WebSocket socket : sockets) {
            this.write(socket, message);
        }
    }


    public String responseLatestMsg() {
        Block[] blocks = {blockManager.getLatestBlock()};
        return JSON.toJSONString(new Message(RESPONSE_BLOCK_CHAIN, JSON.toJSONString(blocks)));
    }


    private String queryAllMsg() {
        return JSON.toJSONString(new Message(QUERY_ALL));
    }

    private String queryChainLengthMsg() {
        return JSON.toJSONString(new Message(QUERY_LATEST));
    }

    private String responseChainMsg() {
        return JSON.toJSONString(new Message(RESPONSE_BLOCK_CHAIN, JSON.toJSONString(blockManager.getBlockChain())));
    }

    private void handleMessage(WebSocket webSocket, String s) {
        try {
            Message message = JSON.parseObject(s, Message.class);
            System.out.println("Received message" + JSON.toJSONString(message));
            switch (message.getType()) {
                case QUERY_LATEST:
                    write(webSocket, responseLatestMsg());
                    break;
                case QUERY_ALL:
                    write(webSocket, responseChainMsg());
                    break;
                case RESPONSE_BLOCK_CHAIN:
                    handleBlockChainResponse(message.getData());
                    break;
            }
        } catch (Exception e) {
            System.out.println("handle message is error:" + e.getMessage());
        }
    }

    private void handleBlockChainResponse(String message) {
        List<Block> receiveBlocks = JSON.parseArray(message, Block.class);
        Collections.sort(receiveBlocks, new Comparator<Block>() {
            public int compare(Block o1, Block o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });

        Block latestBlockReceived = receiveBlocks.get(receiveBlocks.size() - 1);
        Block latestBlock = blockManager.getLatestBlock();
        if (latestBlockReceived.getIndex() > latestBlock.getIndex()) {
            if (latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
                System.out.println("We can append the received block to our chain");
                blockManager.addBlock(latestBlockReceived);
                broadcast(responseLatestMsg());
            } else if (receiveBlocks.size() == 1) {
                System.out.println("We have to query the chain from our peer");
                broadcast(queryAllMsg());
            } else {
                blockManager.replaceChain(receiveBlocks);
            }
        } else {
            System.out.println("received blockChain is not longer than received blockChain. Do nothing");
        }
    }


    private void write(WebSocket ws, String message) {
        ws.send(message);
    }
}
