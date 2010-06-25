<?php

class Temperance_FulltextService extends MessagePackRPC_Client {
    
    const SERVICE_NAME = 'temperance.rpc.msgpack.MsgpackFullTextService';

    const PARSER_MECAB = 0;
    const PARSER_BIGRAM = 1;
    const PARSER_PREFIX = 2;
    const PARSER_HASH_CSV = 3;
    const PARSER_HASH_TSV = 4;
    const PARSER_HASH_SSV = 5;
    
    const STATUS_FAILURE = 0;
    const STATUS_SUCCESS = 1;
    const STATUS_ENQUEUE = 2;
    
    public function __construct($host, $port){
        parent::__construct($host, $port);
    }
    public function search($key, $str, $parser = self::PARSER_MECAB){
        return $this->call(self::SERVICE_NAME . '#search', array($key, $str, $parser));
    }
    public function add($key, $str, $value, $expire = 86400, $parser = self::PARSER_MECAB){
        return $this->call(self::SERVICE_NAME . '#add', array($key, $str, $value, $expire, $parser));
    }
    public function delete($key, $expire = 0){
        return $this->call(self::SERVICE_NAME . '#delete', array($key, $expire));
    }
    public function deleteByValue($key, $value, $expire = 0){
        return $this->call(self::SERVICE_NAME . '#deleteByValue', array($key, $value, $expire));
    }
}