<?php

require dirname(__FILE__) . '/PhpBuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/ListService.php';
require dirname(__FILE__) . '/proto/QueryService.php';

$ctx = new PhpBuf_RPC_Context;
$ctx->addServer('localhost', 17001);
$service = new Temperance_ListService($ctx);
echo 'add key: hoge', PHP_EOL;
{
    $addParam = new Temperance_List_Request_Add;
    $addParam->key = 'hoge';
    $addParam->value = 'value-1';
    $service->add($addParam);
    echo 'hoge: value-1', PHP_EOL;
}
{
    $addParam = new Temperance_List_Request_Add;
    $addParam->key = 'hoge';
    $addParam->value = 'value-2';
    $service->add($addParam);
    echo 'hoge: value-2', PHP_EOL;
}
echo 'add key: foo', PHP_EOL;
{
    $addParam = new Temperance_List_Request_Add;
    $addParam->key = 'foo';
    $addParam->value = 'value-1';
    $service->add($addParam);
    echo 'foo: value-1', PHP_EOL;
}
{
    $addParam = new Temperance_List_Request_Add;
    $addParam->key = 'foo';
    $addParam->value = 'value-3';
    $service->add($addParam);
    echo 'foo: value-3', PHP_EOL;
}

$service = new Temperance_QueryService($ctx);

$getParam = new Temperance_Query_Request_Select;
$getParam->query = 'FROM hoge IN DATA(foo)';
$result = $service->select($getParam);
var_dump($result->values);

$getParam = new Temperance_Query_Request_Select;
$getParam->query = "FROM hoge NOT DATA(foo)";
$result = $service->select($getParam);
var_dump($result->values);

