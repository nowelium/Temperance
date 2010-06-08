<?php

require dirname(__FILE__) . '/lib/MessagePackRPC/Client.php';
require dirname(__FILE__) . '/msgpack/ListService.php';
require dirname(__FILE__) . '/msgpack/QueryService.php';

$service = new Temperance_ListService('localhost', 17001);
echo 'add key: hoge', PHP_EOL;
$service->add('hoge', 'value-1');
$service->add('hoge', 'value-2');

echo 'add key: foo', PHP_EOL;
$service->add('foo', 'value-1');
$service->add('foo', 'value-3');

$service = new Temperance_QueryService('localhost', 17001);

$result = $service->select('FROM hoge IN DATA(foo)');
var_dump($result);

$result = $service->select('FROM hoge NOT DATA(foo)');
var_dump($result);

