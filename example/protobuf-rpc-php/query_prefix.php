<?php

require dirname(__FILE__) . '/lib/php-protobuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/FullTextService.php';
require dirname(__FILE__) . '/proto/QueryService.php';

$service = new Temperance_FullTextService('localhost', 17001);
{
    $setParam = new Temperance_FullText_Request_Add;
    $setParam->key = 'hoge';
    $setParam->str = '本日は晴天なり';
    $setParam->value = 'value-1';
    $setParam->parser = Temperance_FullText_Request_Parser::PREFIX;

    $service->add($setParam);
}
{
    $setParam = new Temperance_FullText_Request_Add;
    $setParam->key = 'hoge';
    $setParam->str = '本日は雨天なり';
    $setParam->value = 'value-2';
    $setParam->parser = Temperance_FullText_Request_Parser::PREFIX;

    $service->add($setParam);
}

$service = new Temperance_QueryService('localhost', 17001);
$getParam = new Temperance_Query_Request_Select;
$getParam->query = 'FROM hoge IN PREFIX("本日は")';
$result = $service->select($getParam);
var_dump($result->values);

$service = new Temperance_QueryService('localhost', 17001);
$getParam = new Temperance_Query_Request_Select;
$getParam->query = 'FROM hoge IN PREFIX("本日は晴天")';
$result = $service->select($getParam);
var_dump($result->values);
