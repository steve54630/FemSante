<?php

header('Content-Type: application/json');
include_once '../config/Database.php';

$ch = curl_init();
$orderid = $json['orderId'];

curl_setopt($ch, CURLOPT_URL, 'https://api-m.sandbox.paypal.com/v2/checkout/orders/'.$orderid.'/capture');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'POST');
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type: application/json',
    'Authorization: Bearer ACCESS_TOKEN',
]);
curl_setopt($ch, CURLOPT_POSTFIELDS, '');
curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);

$err = curl_error($ch);

$status="";
if ($err) {
    $result["success"] = false;
    $result["error"] = "cURL Error #:".$err;
}
else
{
    $jsonAccess = json_decode($resultRequest);
    $status = $jsonAccess->status;

    if ($status == "COMPLETED") {
        $result["success"] = true;
    }
    else {
        $result["success"] = false;
        $result["error"] = "Erreur lors de la validation du paiement";
    }

}

echo json_encode($result);

?>