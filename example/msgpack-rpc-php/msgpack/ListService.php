<?php

class Temperance_ListService extends MessagePackRPC_Client {
    
    const SERVICE_NAME = 'temperance.rpc.msgpack.MsgpackListService';
    
    public function add($key, $value, $expire = 86400){
        return $this->call(self::SERVICE_NAME . '#add', array($key, $value, $expire));
    }
    
    public function get($key, $offset = 0, $limit = 1000){
        return $this->call(self::SERVICE_NAME . '#get', array($key, $offset, $limit));
    }
    
    public function count($key){
        return $this->call(self::SERVICE_NAME . '#count', array($key));
    }
}