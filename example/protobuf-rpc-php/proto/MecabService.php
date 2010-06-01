<?php

class Temperance_Mecab_Request_Parse extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('str', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Mecab_Response_Node extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('surface', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('feature', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Mecab_Response_Parse extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('nodes', PhpBuf_Type::MESSAGE, PhpBuf_Rule::REPEATED, 1, Temperance_Mecab_Response_Node::name());
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_MecabService extends PhpBuf_RPC_Socket_Service_Client {
    public function __construct($host, $port){
        parent::__construct($host, $port);
        $this->setServiceFullQualifiedName('temperance.protobuf.MecabService');
        $this->registerMethodResponderClass('parse', Temperance_Mecab_Response_Parse::name());
    }
}