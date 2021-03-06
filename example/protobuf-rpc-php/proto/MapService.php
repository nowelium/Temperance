<?php

//
// {{{ Request
//

class Temperance_Map_Request_Set extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('value', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
        $this->setField('expire', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 3, 86400);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Map_Request_Get extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Map_Request_GetValues extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('keys', PhpBuf_Type::STRING, PhpBuf_Rule::REPEATED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Map_Request_Delete extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('expire', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 2, 0);
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

class Temperance_Map_Response_Entry extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('value', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
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

class Temperance_Map_Response_GetValues extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('values', PhpBuf_Type::MESSAGE, PhpBuf_Rule::REPEATED, 1, Temperance_Map_Response_Entry::name());
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_Map_Response_Delete extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('succeed', PhpBuf_Type::BOOL, PhpBuf_Rule::REQUIRED, 1);
    }
    public static function name(){
        return __CLASS__;
    }
}

//
// }}} Response
//

class Temperance_MapService extends PhpBuf_RPC_Service_Client {
    public function __construct(PhpBuf_RPC_Context $context){
        parent::__construct($context);
        $this->setServiceFullQualifiedName('temperance.protobuf.MapService');
        $this->registerMethodResponderClass('set', Temperance_Map_Response_Set::name());
        $this->registerMethodResponderClass('get', Temperance_Map_Response_Get::name());
        $this->registerMethodResponderClass('getValues', Temperance_Map_Response_GetValues::name());
        $this->registerMethodResponderClass('delete', Temperance_Map_Response_Delete::name());
    }
}
