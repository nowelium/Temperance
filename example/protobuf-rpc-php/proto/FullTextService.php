<?php

class Temperance_FullText_Request_Parser extends PhpBuf_Message_Abstract {
    const MECAB = 0;
    const BIGRAM = 1;
    const PREFIX = 2;
    public static function name(){
        return __CLASS__;
    }
    public static function values(){
        return array(
            self::MECAB,
            self::BIGRAM,
            self::PREFIX
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

class Temperance_FullText_Response_Add extends PhpBuf_Message_Abstract {
    public function __construct(){
        $this->setField('succeed', PhpBuf_Type::BOOL, PhpBuf_Rule::REQUIRED, 1);
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

class Temperance_FullTextService extends PhpBuf_RPC_Socket_Service_Client {
    public function __construct($host, $port){
        parent::__construct($host, $port);
        $this->setServiceFullQualifiedName('temperance.protobuf.FullTextService');
        $this->registerMethodResponderClass('add', Temperance_FullText_Response_Add::name());
        $this->registerMethodResponderClass('search', Temperance_FullText_Response_Search::name());
    }
}
