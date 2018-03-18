package org.chain.block;

import org.chain.block.manager.BlockManager;
import org.chain.block.service.HTTPService;
import org.chain.block.manager.P2PManager;

/**
 * 测试
 * @author 宇文芬
 */
public class Main {
    public static void main(String[] args) {
        if (args != null && (args.length == 2 || args.length == 3)) {
            try {
                int httpPort = Integer.valueOf(args[0]);
                int p2pPort = Integer.valueOf(args[1]);
                BlockManager blockManager = new BlockManager();
                P2PManager p2PManager = new P2PManager(blockManager);
                p2PManager.initP2PServer(p2pPort);
                if (args.length == 3 && args[2] != null) {
                    p2PManager.connectToPeer(args[2]);
                }
                HTTPService httpService = new HTTPService(blockManager, p2PManager);
                httpService.initHTTPServer(httpPort);
            } catch (Exception e) {
                System.out.println("startup is error:" + e.getMessage());
            }
        } else {
            System.out.println("usage: java -jar naiveChain.jar 8080 6001");
        }
    }
}
