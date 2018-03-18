package org.chain.block.manager;

import org.chain.block.domain.Block;
import org.chain.block.util.CryptoUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 区块管理
 * @author 宇文芬
 */
public class BlockManager {
    private List<Block> blockChain;

    public BlockManager() {
        this.blockChain = new CopyOnWriteArrayList<Block>();
        blockChain.add(this.getFirstBlock());
    }

    /**
     * 获取区块链最后面的一个区块
     * @return
     */
    public Block getLatestBlock() {
        return blockChain.get(blockChain.size() - 1);
    }

    /**
     * 生成新区块
     * @param blockData
     * @return
     */
    public Block generateNextBlock(String blockData) {
        Block previousBlock = this.getLatestBlock();
        int nextIndex = previousBlock.getIndex() + 1;
        long nextTimestamp = System.currentTimeMillis();
        String nextHash = calculateHash(previousBlock.getHash(), nextTimestamp, blockData);
        return new Block(nextIndex, previousBlock.getHash(), nextTimestamp, blockData, nextHash);
    }

    /**
     * 新区块加入到区块链
     * 会检测区块是否有效, 无效的区块无法加入到区块链
    */
    public boolean addBlock(Block newBlock) {
        return isValidNewBlock(newBlock, getLatestBlock()) && blockChain.add(newBlock);
    }

    /**
     * 获取整个区块链
     * @return
     */
    public List<Block> getBlockChain() {
        return blockChain;
    }

    /**
     * 新的区块链中每个区块都符合要求,并且长度比本地区块链长,则替换本地区块链
     * @param newBlocks
     */
    public void replaceChain(List<Block> newBlocks) {
        if (isValidBlocks(newBlocks) && newBlocks.size() > blockChain.size()) {
            blockChain = newBlocks;
        } else {
            System.out.println("Received blockChain invalid");
        }
    }

    /**
     * 生成创世块
     * @return
     */
    private Block getFirstBlock() {
        return new Block(1, "0", System.currentTimeMillis(), "First Block", "aa212344fc10ea0a2cb885078fa9bc2354e55efc81be8f56b66e4a837157662e");
    }


    private boolean isValidBlocks(List<Block> newBlocks) {
        Block firstBlock = newBlocks.get(0);
        if (firstBlock.equals(getFirstBlock())) {
            return false;
        }

        for (int i = 1; i < newBlocks.size(); i++) {
            if (isValidNewBlock(newBlocks.get(i), firstBlock)) {
                firstBlock = newBlocks.get(i);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查区块是否有效
     * @param newBlock
     * @param previousBlock
     * @return
     */
    private boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        if (previousBlock.getIndex() + 1 != newBlock.getIndex()) {
            System.out.println("invalid index");
            return false;
        } else if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
            System.out.println("invalid previousHash");
            return false;
        } else {
            String hash = calculateHash(newBlock.getPreviousHash(), newBlock.getTimestamp(), newBlock.getData());
            if (!hash.equals(newBlock.getHash())) {
                System.out.println("invalid hash: " + hash + " " + newBlock.getHash());
                return false;
            }
        }
        return true;
    }

    /**
     * 计算区块内容的Hash值
     * @param previousHash
     * @param timestamp
     * @param data
     * @return
     */
    private String calculateHash(String previousHash, long timestamp, String data) {
        return CryptoUtil.getSHA256(previousHash + timestamp + data);
    }

}
