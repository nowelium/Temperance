<?php

require dirname(__FILE__) . '/PhpBuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/MapService.php';

$ctx = new PhpBuf_RPC_Context;
$ctx->addServer('localhost', 17001);
$service = new Temperance_MapService($ctx);

{
    $setParam = new Temperance_Map_Request_Set;
    $setParam->key = 'key-1';
    $setParam->value = 'value-1';

    $service->set($setParam);
}
{
    $setParam = new Temperance_Map_Request_Set;
    $setParam->key = 'key-2';
    $setParam->value = 'value-2';

    $service->set($setParam);
}
{
    $setParam = new Temperance_Map_Request_Set;
    $setParam->key = 'key-3';
    $setParam->value = 'value-3';

    $service->set($setParam);
}

$getParam = new Temperance_Map_Request_Get;
$getParam->key = 'key-1';
$result = $service->get($getParam);
var_dump($result->value);

$getParam = new Temperance_Map_Request_Get;
$getParam->key = 'key-3';
$result = $service->get($getParam);
var_dump($result->value);
