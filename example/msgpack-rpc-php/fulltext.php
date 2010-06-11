<?php

require dirname(__FILE__) . '/lib/MessagePackRPC/Client.php';
require dirname(__FILE__) . '/msgpack/FullTextService.php';

$service = new Temperance_FullTextService('localhost', 17001);

$service->add('hoge', '本日は晴天なり', 'http://www.google.com/search?q=晴天');
$service->add('hoge', '本日は雨です', 'http://www.google.com/search?q=雨');
$service->add('hoge', '本日は快晴かも', 'http://www.google.com/search?q=快晴');
$service->add('hoge', '昨日は曇りでした', 'http://www.google.com/search?q=曇');

echo 'searching: 本日', PHP_EOL;
$values = $service->search('hoge', '本日');
var_dump($values);

echo 'searching: 昨日', PHP_EOL;
$values = $service->search('hoge', '昨日');
var_dump($values);
