<?php

class Temperance_Query_Request_Get extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('query', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Query_Response_Get extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('values', PhpBuf_Type::STRING, PhpBuf_Rule::REPEATED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_QueryService extends PhpBuf_RPC_Socket_Service_Client {
    public function __construct($host, $port){
        parent::__construct($host, $port);
        $this->setServiceFullQualifiedName('temperance.protobuf.QueryService');
        $this->registerMethodResponderClass('get', Temperance_Query_Response_Get::name());
    }
}
