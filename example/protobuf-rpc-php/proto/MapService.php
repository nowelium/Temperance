<?php

class Temperance_Map_Request_Set extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('namespace', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
        $this->setField('value', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 3);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Map_Request_Get extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('namespace', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Map_Response_Set extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('succeed', PhpBuf_Type::BOOL, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}
class Temperance_Map_Response_Get extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('value', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_MapService extends PhpBuf_RPC_Socket_Service_Client {
    public function __construct($host, $port){
        parent::__construct($host, $port);
        $this->setServiceFullQualifiedName('temperance.protobuf.MapService');
        $this->registerMethodResponderClass('set', Temperance_Map_Response_Set::name());
        $this->registerMethodResponderClass('get', Temperance_Map_Response_Get::name());
    }
}
