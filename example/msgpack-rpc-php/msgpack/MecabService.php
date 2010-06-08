<?php

class Temperance_MecabService extends MessagePackRPC_Client {
    
    const SERVICE_NAME = 'temperance.rpc.msgpack.MsgpackMecabService';
    
    public function parse($str){
        return $this->call(self::SERVICE_NAME . '#parse', array($str));
    }
}