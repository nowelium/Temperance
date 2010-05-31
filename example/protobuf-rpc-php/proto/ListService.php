<?php

class Temperance_List_Request_Add extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('value', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
        $this->setField('expire', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 3, 86400);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_List_Request_Get extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('offset', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 2, 0);
        $this->setField('limit', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 3, 1000);
    }
    public static function name(){
        return __CLASS__;
    }
}
class Temperance_List_Request_Count extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_List_Response_Add extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('succeed', PhpBuf_Type::BOOL, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}
class Temperance_List_Response_Get extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('values', PhpBuf_Type::STRING, PhpBuf_Rule::REPEATED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}
class Temperance_List_Response_Count extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('count', PhpBuf_Type::INT, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_ListService extends PhpBuf_RPC_Socket_Service_Client {
    public function __construct($host, $port){
        parent::__construct($host, $port);
        $this->setServiceFullQualifiedName('temperance.protobuf.ListService');
        $this->registerMethodResponderClass('add', Temperance_List_Response_Add::name());
        $this->registerMethodResponderClass('get', Temperance_List_Response_Get::name());
        $this->registerMethodResponderClass('count', Temperance_List_Response_Count::name());
    }
}
