# 活石链测试
### Quick start
```
git clone https://github.com/buwan/chain.git
cd chain
mvn clean package
java -jar chain.jar 8001 9001
java -jar chain.jar 8002 9002 ws://localhost:9001

```


### HTTP API

- query blocks

  ```
  curl http://localhost:8001/blocks

  ```

- mine block

  ```
  curl -H "Content-type:application/json" --data '{"data" : "Some data to the first block"}' http://localhost:8001/mineBlock

  ```

- add peer

  ```
  curl -H "Content-type:application/json" --data '{"peer" : "ws://localhost:9002"}' http://localhost:8001/addPeer

  ```

- query peers

  ```
  curl http://localhost:8001/peers
  curl http://localhost:8002/peers
  ```