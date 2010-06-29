<?php

class Temperance_ListService extends MessagePackRPC_Client {
    
    const SERVICE_NAME = 'temperance.rpc.msgpack.MsgpackListService';
    
    const STATUS_SUCCESS = 0;
    const STATUS_ENQUEUE = 1;
    const STATUS_FAILURE = 10;
    const STATUS_TIMEOUT = 11;
    
    public function add($key, $value, $expire = 86400){
        return $this->call(self::SERVICE_NAME . '#add', array($key, $value, $expire));
    }
    public function get($key, $offset = 0, $limit = 1000){
        return $this->call(self::SERVICE_NAME . '#get', array($key, $offset, $limit));
    }
    public function count($key){
        return $this->call(self::SERVICE_NAME . '#count', array($key));
    }
    public function delete($key, $expire = 0){
        return $this->call(self::SERVICE_NAME . '#delete', array($key, $expire));
    }
    public function deleteByValue($key, $value, $expire = 0){
        return $this->call(self::SERVICE_NAME . '#deleteByValue', array($key, $value, $expire));
    }
    public function reindex($key){
        return $this->call(self::SERVICE_NAME . '#reindex', array($key));
    }
}