<?php

//
// {{{ Request
//

class Temperance_Queue_Request_Enqueue extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('value', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
        $this->setField('expire', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 3, 86400);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Queue_Request_Dequeue extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('timeout', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 2, 10);
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

class Temperance_Queue_Response_Enqueue extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('succeed', PhpBuf_Type::BOOL, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Queue_Response_Dequeue extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('value', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
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

class Temperance_QueueService extends PhpBuf_RPC_Service_Client {
    public function __construct(PhpBuf_RPC_Context $context){
        parent::__construct($context);
        $this->setServiceFullQualifiedName('temperance.protobuf.QueueService');
        $this->registerMethodResponderClass('enqueue', Temperance_Queue_Response_Enqueue::name());
        $this->registerMethodResponderClass('dequeue', Temperance_Queue_Response_Dequeue::name());
    }
}

//
// }}} Service
//
