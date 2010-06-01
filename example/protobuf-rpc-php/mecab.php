<?php

require dirname(__FILE__) . '/lib/php-protobuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/MecabService.php';

$service = new Temperance_MecabService('localhost', 17001);

$parseParam = new Temperance_Mecab_Request_Parse;
$parseParam->str = '本日は晴天なり';

$result = $service->parse($parseParam);
var_dump($result->nodes);
