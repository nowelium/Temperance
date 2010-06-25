<?php

require dirname(__FILE__) . '/PhpBuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/FullTextService.php';
require dirname(__FILE__) . '/proto/QueryService.php';

$ctx = new PhpBuf_RPC_Context;
$ctx->addServer('localhost', 17001);
$service = new Temperance_FullTextService($ctx);
{
    $setParam = new Temperance_FullText_Request_Add;
    $setParam->key = 'hoge';
    $setParam->str = '本日は晴天なり';
    $setParam->value = 'value-1';
    $setParam->parser = Temperance_FullText_Request_Parser::MECAB;

    $service->add($setParam);
}
{
    $setParam = new Temperance_FullText_Request_Add;
    $setParam->key = 'hoge';
    $setParam->str = '本日は雨天なり';
    $setParam->value = 'value-2';
    $setParam->parser = Temperance_FullText_Request_Parser::MECAB;

    $service->add($setParam);
}

$total = array();
$service = new Temperance_QueryService($ctx);
while(true){
    $elapsed = microtime(true);

    $getParam = new Temperance_Query_Request_Select;
    $getParam->query = 'FROM hoge IN MECAB("本日")';
    $result = $service->select($getParam);

    $getParam = new Temperance_Query_Request_Select;
    $getParam->query = 'FROM hoge NOT MECAB("昨日")';
    $result = $service->select($getParam);

    $diff = microtime(true) - $elapsed;
    $total[] = $diff;
    echo $diff, PHP_EOL;
    echo 'avg: ', array_sum($total) / count($total), PHP_EOL;
}
