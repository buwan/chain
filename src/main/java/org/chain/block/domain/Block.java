package org.chain.block.domain;

/**
 * 区块体 数据模型 定义
 * @author  宇文芬
 */
public class Block {
    /**
     * 区块序号
     */
    private int    index;

    /**
     * 上一个区块的Hash值
     */
    private String previousHash;

    /**
     * 时间戳
     */
    private long   timestamp;

    /**
     * 区块内容
     */
    private String data;

    /**
     * 区块Hash 值
     */
    private String hash;

    public Block() {
    }

    public Block(int index, String previousHash, long timestamp, String data, String hash) {
        this.index = index;
        this.previousHash = previousHash;
        this.timestamp = timestamp;
        this.data = data;
        this.hash = hash;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}

