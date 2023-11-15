<?php

header('Content-Type: application/json');
include_once '../config/Database.php';

$ch = curl_init();
$clientId = $json['clientId'];
$secret = "EMWwSDZmOFLzCzUV9niuRm6YraGC5QxSw76xy50j8O0swg6jfACINQGDELOLxvjH2b5dQkmyEI3IFLlJ";

curl_setopt($ch, CURLOPT_URL, "https://api.sandbox.paypal.com/v1/oauth2/token");
curl_setopt($ch, CURLOPT_HEADER, false);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true); 
curl_setopt($ch, CURLOPT_USERPWD, $clientId.":".$secret);
curl_setopt($ch, CURLOPT_POSTFIELDS, "grant_type=client_credentials");

$resultRequest = curl_exec($ch);
$err = curl_error($ch);

$access_token="";
if ($err) {
    $result["success"] = false;
    $result["error"] = "cURL Error #:".$err;
}
else
{
    $jsonAccess = json_decode($resultRequest);
    $access_token = $jsonAccess->access_token;

    curl_close($ch);

    $ch = curl_init();

    curl_setopt($ch, CURLOPT_URL, 'https://api-m.sandbox.paypal.com/v2/checkout/orders/');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, 
        "{\n      \"intent\": \"CAPTURE|AUTHORIZE\",\n      \"purchase_units\"".
        ": [\n          {\n              \"amount\": {\n                  \"".
        "currency_code\": \"EUR\",\n                  \"".
        "value\": \"".htmlspecialchars($json['price'])
        ."\"\n              }\n          }\n      ]\n  }");

    $headers = array();
    $headers[] = 'Content-Type: application/json';
    $headers[] = 'Authorization: Bearer '.$access_token;
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

    if (curl_errno($ch)) {
    
    }

$result = curl_exec($ch);
curl_close($ch);
}

echo json_encode($result);

?>