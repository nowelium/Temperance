<?php

require dirname(__FILE__) . '/lib/php-protobuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/FullTextService.php';
require dirname(__FILE__) . '/proto/QueryService.php';

$service = new Temperance_FullTextService('localhost', 17001);
{
    $setParam = new Temperance_FullText_Request_Set;
    $setParam->key = 'hoge';
    $setParam->str = '本日は晴天なり';
    $setParam->value = 'value-1';
    $setParam->parser = Temperance_FullText_Request_Parser::BIGRAM;

    $service->set($setParam);
}
{
    $setParam = new Temperance_FullText_Request_Set;
    $setParam->key = 'hoge';
    $setParam->str = '本日は雨天なり';
    $setParam->value = 'value-2';
    $setParam->parser = Temperance_FullText_Request_Parser::BIGRAM;

    $service->set($setParam);
}

$service = new Temperance_QueryService('localhost', 17001);

$getParam = new Temperance_Query_Request_Select;
$getParam->query = 'FROM hoge IN GRAM("寒天などを食べた")';
$result = $service->select($getParam);
var_dump($result->values);

$getParam = new Temperance_Query_Request_Select;
$getParam->query = 'FROM hoge IN GRAM("運動会は雨天決行です")';
$result = $service->select($getParam);
var_dump($result->values);
