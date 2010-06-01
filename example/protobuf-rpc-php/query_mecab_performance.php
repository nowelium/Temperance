<?php

require dirname(__FILE__) . '/lib/php-protobuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/FullTextService.php';
require dirname(__FILE__) . '/proto/QueryService.php';

$service = new Temperance_FullTextService('localhost', 17001);
{
    $setParam = new Temperance_FullText_Request_Set;
    $setParam->key = 'hoge';
    $setParam->str = '本日は晴天なり';
    $setParam->value = 'value-1';
    $setParam->parser = Temperance_FullText_Request_Parser::MECAB;

    $service->set($setParam);
}
{
    $setParam = new Temperance_FullText_Request_Set;
    $setParam->key = 'hoge';
    $setParam->str = '本日は雨天なり';
    $setParam->value = 'value-2';
    $setParam->parser = Temperance_FullText_Request_Parser::MECAB;

    $service->set($setParam);
}

$total = array();
while(true){
    $elapsed = microtime(true);

    $service = new Temperance_QueryService('localhost', 17001);
    $getParam = new Temperance_Query_Request_Get;
    $getParam->query = 'FROM hoge IN MECAB("本日")';
    $result = $service->get($getParam);

    $getParam = new Temperance_Query_Request_Get;
    $getParam->query = 'FROM hoge NOT MECAB("昨日")';
    $result = $service->get($getParam);

    $diff = microtime(true) - $elapsed;
    $total[] = $diff;
    echo $diff, PHP_EOL;
    echo 'avg: ', array_sum($total) / count($total), PHP_EOL;
}
