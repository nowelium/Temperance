<?php

require dirname(__FILE__) . '/lib/MessagePackRPC/Client.php';
require dirname(__FILE__) . '/msgpack/MapService.php';

$service = new Temperance_MapService('localhost', 17001);
$service->set('key-1', 'value-1');
$service->set('key-2', 'value-2');
$service->set('key-3', 'value-3');

$value = $service->get('key-1');
var_dump($value);

$value = $service->get('key-3');
var_dump($value);
