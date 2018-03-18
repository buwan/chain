package org.chain.block.service;

import com.alibaba.fastjson.JSON;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.java_websocket.WebSocket;
import org.chain.block.domain.Block;
import org.chain.block.manager.BlockManager;
import org.chain.block.manager.P2PManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 区块链服务
 *   包括: 查询区块链,挖矿,查询链接池,接受连接请求等
 * @author  宇文芬
 */
public class HTTPService {
    private BlockManager blockManager;
    private P2PManager p2PManager;

    public HTTPService(BlockManager blockManager, P2PManager p2PManager) {
        this.blockManager = blockManager;
        this.p2PManager = p2PManager;
    }

    /**
     * 服务初始化
     * @param port
     */
    public void initHTTPServer(int port) {
        try {
            Server server = new Server(port);
            System.out.println("listening http port on: " + port);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            context.addServlet(new ServletHolder(new BlocksServlet()), "/blocks");
            context.addServlet(new ServletHolder(new MineBlockServlet()), "/mineBlock");
            context.addServlet(new ServletHolder(new PeersServlet()), "/peers");
            context.addServlet(new ServletHolder(new AddPeerServlet()), "/addPeer");
            server.start();
            server.join();
        } catch (Exception e) {
            System.out.println("init http server is error:" + e.getMessage());
        }
    }

    /**
     * 查询整个区块链数据的请求
     */
    private class BlocksServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().println(JSON.toJSONString(blockManager.getBlockChain()));
        }
    }

    /**
     * 接受新的链接请求
     */
    private class AddPeerServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            this.doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            String peer = req.getParameter("peer");
            p2PManager.connectToPeer(peer);
            resp.getWriter().print("ok");
        }
    }


    /**
     * 查询所有在线节点的请求
     */
    private class PeersServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            for (WebSocket socket : p2PManager.getSockets()) {
                InetSocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
                resp.getWriter().print(remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort());
            }
        }
    }

    /**
     * 挖矿请求
     */
    private class MineBlockServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            this.doPost(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("UTF-8");
            String data = req.getParameter("data");
            /**
             * 生成新区块
             */
            Block newBlock = blockManager.generateNextBlock(data);
            /**
             * 将区块加入到区块链中
             */
            boolean isSuccess = blockManager.addBlock(newBlock);
            if(isSuccess){
                /**
                 * 将挖矿结果广播到其他节点
                 */
                p2PManager.broadcast(p2PManager.responseLatestMsg());
                String s = JSON.toJSONString(newBlock);
                System.out.println("block added: " + s);
                resp.getWriter().print(s);
            }else{
                resp.getWriter().print("mineBlock failure ");
            }
        }
    }
}

