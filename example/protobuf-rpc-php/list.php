<?php

require dirname(__FILE__) . '/lib/php-protobuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/FullTextService.php';
require dirname(__FILE__) . '/proto/ListService.php';
require dirname(__FILE__) . '/proto/MapService.php';

$service = new Temperance_ListService('localhost', 17001);

{
    $addParam = new Temperance_List_Request_add;
    $addParam->namespace = 'hoge';
    $addParam->key = 'albums';
    $addParam->value = '12345';

    $service->add($addParam);
}
{
    $addParam = new Temperance_List_Request_add;
    $addParam->namespace = 'hoge';
    $addParam->key = 'albums';
    $addParam->value = '567890';

    $service->add($addParam);
}
{
    $addParam = new Temperance_List_Request_add;
    $addParam->namespace = 'hoge';
    $addParam->key = 'albums';
    $addParam->value = 'hello world';

    $service->add($addParam);
}

$countParam = new Temperance_List_Request_Count;
$countParam->namespace = 'hoge';
$countParam->key = 'albums';
$result = $service->count($countParam);
var_dump($result->count);

$getParam = new Temperance_List_Request_Get;
$getParam->namespace = 'hoge';
$getParam->key = 'albums';
$result = $service->get($getParam);
var_dump($result->values);
