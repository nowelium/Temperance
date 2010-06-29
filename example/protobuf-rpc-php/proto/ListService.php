<?php

//
// {{{ Request
//

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

class Temperance_List_Request_Delete extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('expire', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 2, 0);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_List_Request_DeleteByValue extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('value', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
        $this->setField('expire', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 3, 0);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_List_Request_Reindex extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
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

class Temperance_List_Response_Status extends PhpBuf_Message_Abstract {
    
    const SUCCESS = 0;
    const ENQUEUE = 1;
    const FAILURE = 10;
    const TIMEOUT = 11;
    
    public static function name(){
        return __CLASS__;
    }
    
    public static function values(){
        return array(
            self::SUCCESS,
            self::ENQUEUE,
            self::FAILURE,
            self::TIMEOUT
        );
    }
}

class Temperance_List_Response_Add extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('status', PhpBuf_Type::ENUM, PhpBuf_Rule::REQUIRED, 1, Temperance_List_Response_Status::values());
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

class Temperance_List_Response_Delete extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('status', PhpBuf_Type::ENUM, PhpBuf_Rule::REQUIRED, 1, Temperance_List_Response_Status::values());
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_List_Response_DeleteByValue extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('status', PhpBuf_Type::ENUM, PhpBuf_Rule::REQUIRED, 1, Temperance_List_Response_Status::values());
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_List_Response_Reindex extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('status', PhpBuf_Type::ENUM, PhpBuf_Rule::REQUIRED, 1, Temperance_List_Response_Status::values());
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

class Temperance_ListService extends PhpBuf_RPC_Service_Client {
    public function __construct(PhpBuf_RPC_Context $context){
        parent::__construct($context);
        $this->setServiceFullQualifiedName('temperance.protobuf.ListService');
        $this->registerMethodResponderClass('add', Temperance_List_Response_Add::name());
        $this->registerMethodResponderClass('get', Temperance_List_Response_Get::name());
        $this->registerMethodResponderClass('count', Temperance_List_Response_Count::name());
        $this->registerMethodResponderClass('delete', Temperance_List_Response_Delete::name());
        $this->registerMethodResponderClass('deleteByValue', Temperance_List_Response_DeleteByValue::name());
        $this->registerMethodResponderClass('reindex', Temperance_List_Response_Reindex::name());
    }
}

//
// }}} Service
//
