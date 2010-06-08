<?php

require dirname(__FILE__) . '/lib/MessagePackRPC/Client.php';
require dirname(__FILE__) . '/msgpack/ListService.php';

$service = new Temperance_ListService('localhost', 17001);
$service->add('albums', 12345);
$service->add('albums', 567890);
$service->add('albums', 'hello world');

$count = $service->count('albums');
var_dump($count);

$values = $service->get('albums');
var_dump($values);
