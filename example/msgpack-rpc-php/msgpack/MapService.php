<?php

class Temperance_MapService extends MessagePackRPC_Client {
    
    const SERVICE_NAME = 'temperance.rpc.msgpack.MsgpackMapService';
    
    public function set($key, $value, $expire = 86400){
        return $this->call(self::SERVICE_NAME . '#set', array($key, $value, $expire));
    }
    
    public function get($key){
        return $this->call(self::SERVICE_NAME . '#get', array($key));
    }
    
    public function delete($key, $expire = 0){
        return $this->call(self::SERVICE_NAME . '#delete', array($key, $expire));
    }
}