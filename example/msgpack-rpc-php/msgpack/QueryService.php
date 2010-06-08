<?php

class Temperance_QueryService extends MessagePackRPC_Client {
    
    const SERVICE_NAME = 'temperance.rpc.msgpack.MsgpackQueryService';
    
    public function select($query){
        return $this->call(self::SERVICE_NAME . '#select', array($query));
    }
    
    public function delete($query){
        return $this->call(self::SERVICE_NAME . '#delete', array($query));
    }
}