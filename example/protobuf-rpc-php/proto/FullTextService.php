<?php

//
// {{{ Request
//

class Temperance_FullText_Request_Parser extends PhpBuf_Message_Abstract {
    
    const MECAB = 0;
    const BIGRAM = 1;
    const PREFIX = 2;
    const HASH_CSV = 3;
    const HASH_TSV = 4;
    const HASH_SSV = 5;
    
    public static function name(){
        return __CLASS__;
    }
    
    public static function values(){
        return array(
            self::MECAB,
            self::BIGRAM,
            self::PREFIX,
            self::HASH_CSV,
            self::HASH_TSV,
            self::HASH_SSV
        );
    }
}

class Temperance_FullText_Request_Add extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('str', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
        $this->setField('value', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 3);
        $this->setField('expire', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 4, 86400);
        $this->setField('parser', PhpBuf_Type::ENUM, PhpBuf_Rule::OPTIONAL, 5, Temperance_FullText_Request_Parser::values());
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_FullText_Request_Delete extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('expire', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 2, 0);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_FullText_Request_DeleteByValue extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('value', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
        $this->setField('expire', PhpBuf_Type::INT, PhpBuf_Rule::OPTIONAL, 3, 0);
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_FullText_Request_Search extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('key', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 1);
        $this->setField('str', PhpBuf_Type::STRING, PhpBuf_Rule::REQUIRED, 2);
        $this->setField('parser', PhpBuf_Type::ENUM, PhpBuf_Rule::OPTIONAL, 3, Temperance_FullText_Request_Parser::values());
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

class Temperance_FullText_Response_Status extends PhpBuf_Message_Abstract {
    
    const FAILURE = 0;
    const SUCCESS = 1;
    const ENQUEUE = 2;
    
    public static function name(){
        return __CLASS__;
    }
    
    public static function values(){
        return array(
            self::FAILURE,
            self::SUCCESS,
            self::ENQUEUE
        );
    }
}

class Temperance_FullText_Response_Add extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('status', PhpBuf_Type::ENUM, PhpBuf_Rule::REQUIRED, 1, Temperance_FullText_Response_Status::values());
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_FullText_Response_Delete extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('status', PhpBuf_Type::ENUM, PhpBuf_Rule::REQUIRED, 1, Temperance_FullText_Response_Status::values());
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_FullText_Response_DeleteByValue extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('status', PhpBuf_Type::ENUM, PhpBuf_Rule::REQUIRED, 1, Temperance_FullText_Response_Status::values());
    }
    public static function name(){
        return __CLASS__;
    }
}

class Temperance_FullText_Response_Search extends PhpBuf_Message_Abstract {
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

class Temperance_FullTextService extends PhpBuf_RPC_Service_Client {
    public function __construct(PhpBuf_RPC_Context $context){
        parent::__construct($context);
        $this->setServiceFullQualifiedName('temperance.protobuf.FullTextService');
        $this->registerMethodResponderClass('add', Temperance_FullText_Response_Add::name());
        $this->registerMethodResponderClass('delete', Temperance_FullText_Response_Delete::name());
        $this->registerMethodResponderClass('deleteByValue', Temperance_FullText_Response_DeleteByValue::name());
        $this->registerMethodResponderClass('search', Temperance_FullText_Response_Search::name());
    }
}

//
// }}} Service
//
