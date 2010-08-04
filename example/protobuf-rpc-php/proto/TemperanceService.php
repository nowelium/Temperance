<?php

class Temperance_Temperance_Request_Monitor extends PhpBuf_Message_Abstract {
    public function __construct(){
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Temperance_Response_Monitor extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('inProgressTasks', PhpBuf_Type::INT, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('queuedTasks', PhpBuf_Type::INT, PhpBuf_Rule::REQUIRED, 2);
        $this->setField('totalTasks', PhpBuf_Type::INT, PhpBuf_Rule::REQUIRED, 3);
        $this->setField('totalTime', PhpBuf_Type::INT, PhpBuf_Rule::REQUIRED, 4);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_TemperanceService extends PhpBuf_RPC_Service_Client {
    public function __construct(PhpBuf_RPC_Context $context){
        parent::__construct($context);
        $this->setServiceFullQualifiedName('temperance.protobuf.TemperanceService');
        $this->registerMethodResponderClass('monitor', Temperance_Temperance_Response_Monitor::name());
    }
}
