<?php

require dirname(__FILE__) . '/PhpBuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/MecabService.php';

$ctx = new PhpBuf_RPC_Context;
$ctx->addServer('localhost', 17001);
$service = new Temperance_MecabService($ctx);

$parseParam = new Temperance_Mecab_Request_Parse;
$parseParam->str = '本日は晴天なり';

$result = $service->parse($parseParam);
var_dump($result->nodes);
