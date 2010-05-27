<?php

require dirname(__FILE__) . '/lib/php-protobuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/QueryService.php';

$service = new Temperance_QueryService('localhost', 17001);

$getParam = new Temperance_Query_Request_Get;
$getParam->query = "FROM hoge IN MECAB('本日')";
$result = $service->get($getParam);
var_dump($result->values);

$getParam = new Temperance_Query_Request_Get;
$getParam->query = "FROM hoge IN MECAB('昨日')";
$result = $service->get($getParam);
var_dump($result->values);
