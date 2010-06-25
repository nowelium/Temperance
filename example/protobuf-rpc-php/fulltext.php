<?php

require dirname(__FILE__) . '/PhpBuf/lib/PhpBuf.php';
require dirname(__FILE__) . '/proto/FullTextService.php';

$ctx = new PhpBuf_RPC_Context;
$ctx->addServer('localhost', 17001);
$service = new Temperance_FullTextService($ctx);

{
    $setParam = new Temperance_FullText_Request_Add;
    $setParam->key = 'hoge';
    $setParam->str = '本日は晴天なり';
    $setParam->value = 'http://www.google.com/search?q=晴天';
    $setParam->parser = Temperance_FullText_Request_Parser::MECAB;

    $service->add($setParam);
}
{
    $setParam = new Temperance_FullText_Request_Add;
    $setParam->key = 'hoge';
    $setParam->str = '本日は雨です';
    $setParam->value = 'http://www.google.com/search?q=雨';
    $setParam->parser = Temperance_FullText_Request_Parser::MECAB;

    $service->add($setParam);
}
{
    $setParam = new Temperance_FullText_Request_Add;
    $setParam->key = 'hoge';
    $setParam->str = '本日は快晴かも';
    $setParam->value = 'http://www.google.com/search?q=快晴';
    $setParam->parser = Temperance_FullText_Request_Parser::MECAB;

    $service->add($setParam);
}
{
    $setParam = new Temperance_FullText_Request_Add;
    $setParam->key = 'hoge';
    $setParam->str = '昨日は曇りでした';
    $setParam->value = 'http://www.google.com/search?q=曇';
    $setParam->parser = Temperance_FullText_Request_Parser::MECAB;

    $service->add($setParam);
}

echo 'searching: 本日', PHP_EOL;
$getParam = new Temperance_FullText_Request_Search;
$getParam->key = 'hoge';
$getParam->str = '本日';
$getParam->parser = Temperance_FullText_Request_Parser::MECAB;
$response = $service->search($getParam);
var_dump($response->values);

echo 'searching: 昨日', PHP_EOL;
$getParam = new Temperance_FullText_Request_Search;
$getParam->key = 'hoge';
$getParam->str = '昨日';
$getParam->parser = Temperance_FullText_Request_Parser::MECAB;
$response = $service->search($getParam);
var_dump($response->values);
