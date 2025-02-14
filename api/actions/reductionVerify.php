<?php

header('Content-Type: application/json');
include_once '../config/Database.php';

if (isset($json['reductionCode'])) {
    $reducCode = htmlspecialchars($json['reductionCode']);

    if ($reducCode == '') {
        $result["success"] = false;
        $result["error"] = "Veuillez saisir un code de réduction";
    }
    else {
        $verifyCode = $bdd->prepare('SELECT REDUC_VALUE FROM REDUCTIONS WHERE REDUC_CODE LIKE ?');
        $checkIfEmailExists->execute(array($reducCode));

        if ($verifyCode->rowCount() == 1) {
            
            try {
                $reduction = $verifyCode->fetch();
                $result['success'] = true;
                $result['reduction'] = $reduction['REDUC_VALUE'];
            }
           catch (Exception $e) {
                $result["success"] = false;
                $result["error"] = "Erreur lié à la base de données";
           }
        }
        else {
            $result['success'] = false;
            $result['error'] = "Le code de réduction saisie n'existe pas.";
        }
    }
}
else {
    $result["success"] = false;
    $result["error"] = "Erreur de connexion avec la base de données";
}

echo json_encode($result);

?>