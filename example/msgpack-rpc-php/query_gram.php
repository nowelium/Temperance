<?php

require dirname(__FILE__) . '/lib/MessagePackRPC/Client.php';
require dirname(__FILE__) . '/msgpack/FullTextService.php';
require dirname(__FILE__) . '/msgpack/QueryService.php';

$service = new Temperance_FullTextService('localhost', 17001);
$service->add('hoge', '本日は晴天なり', 'value-1', Temperance_FulltextService::PARSER_BIGRAM);
$service->add('hoge', '本日は雨天なり', 'value-2', Temperance_FulltextService::PARSER_BIGRAM);

$service = new Temperance_QueryService('localhost', 17001);

$result = $service->select('FROM hoge IN GRAM("寒天などを食べた")');
var_dump($result);

$result = $service->select('FROM hoge IN GRAM("運動会は雨天決行です")');
var_dump($result);
