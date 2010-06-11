<?php

class Temperance_FulltextService extends MessagePackRPC_Client {
    
    const SERVICE_NAME = 'temperance.rpc.msgpack.MsgpackFullTextService';

    const PARSER_MECAB = 0;
    const PARSER_BIGRAM = 1;
    const PARSER_PREFIX = 2;
    
    public function __construct($host, $port){
        parent::__construct($host, $port);
    }
    public function search($key, $str, $parser = self::PARSER_MECAB){
        return $this->call(self::SERVICE_NAME . '#search', array($key, $str, $parser));
    }
    public function add($key, $str, $value, $expire = 86400, $parser = self::PARSER_MECAB){
        return $this->call(self::SERVICE_NAME . '#add', array($key, $str, $value, $expire, $parser));
    }
}