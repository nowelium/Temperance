<?php

//
// {{{ Request
//

class Temperance_Query_Request_Select extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('query', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

//
// }}} Request
//

//
// {{{ Response
//

class Temperance_Query_Response_Select extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('values', PhpBuf_Type::STRING, PhpBuf_Rule::REPEATED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

//
// }}} Response
//

//
// {{{ Service
//

class Temperance_QueryService extends PhpBuf_RPC_Service_Client {
    public function __construct(PhpBuf_RPC_Context $context){
        parent::__construct($context);
        $this->setServiceFullQualifiedName('temperance.protobuf.QueryService');
        $this->registerMethodResponderClass('select', Temperance_Query_Response_Select::name());
    }
}

//
// }}} Service
//
