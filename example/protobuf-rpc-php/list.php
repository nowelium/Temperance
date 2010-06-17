<?php

require dirname(__FILE__) . '/lib/php-protobuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/ListService.php';

$service = new Temperance_ListService('localhost', 17001);

{
    $addParam = new Temperance_List_Request_add;
    $addParam->key = 'albums';
    $addParam->value = '12345';

    $service->add($addParam);
}
{
    $addParam = new Temperance_List_Request_add;
    $addParam->key = 'albums';
    $addParam->value = '567890';

    $service->add($addParam);
}
{
    $addParam = new Temperance_List_Request_add;
    $addParam->key = 'albums';
    $addParam->value = 'hello world';

    $service->add($addParam);
}

$countParam = new Temperance_List_Request_Count;
$countParam->key = 'albums';
$result = $service->count($countParam);
var_dump($result->count);

$getParam = new Temperance_List_Request_Get;
$getParam->key = 'albums';
$result = $service->get($getParam);
var_dump($result->values);

$deleteParam = new Temperance_List_Request_Delete;
$deleteParam->key = 'albums';
$service->delete($deleteParam);

$getParam = new Temperance_List_Request_Get;
$getParam->key = 'albums';
$result = $service->get($getParam);
var_dump($result->values);
