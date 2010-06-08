<?php

require dirname(__FILE__) . '/lib/MessagePackRPC/Client.php';
require dirname(__FILE__) . '/msgpack/MecabService.php';

$service = new Temperance_MecabService('localhost', 17001);
$nodes = $service->parse('本日は晴天なり');
var_dump($nodes);
