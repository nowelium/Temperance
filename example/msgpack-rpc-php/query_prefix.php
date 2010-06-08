<?php

require dirname(__FILE__) . '/lib/MessagePackRPC/Client.php';
require dirname(__FILE__) . '/msgpack/FullTextService.php';
require dirname(__FILE__) . '/msgpack/QueryService.php';

$service = new Temperance_FullTextService('localhost', 17001);
$service->set('hoge', '本日は晴天なり', 'value-1', Temperance_FulltextService::PARSER_PREFIX);
$service->set('hoge', '本日は雨天なり', 'value-2', Temperance_FulltextService::PARSER_PREFIX);

$service = new Temperance_QueryService('localhost', 17001);
$result = $service->select('FROM hoge IN PREFIX("本日は")');
var_dump($result);

$result = $service->select('FROM hoge IN PREFIX("本日は晴天")');
var_dump($result);
