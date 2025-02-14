<?php

header('Content-Type: application/json');
include_once '../config/Database.php';

if (isset($json['email']) and isset($json['password'])) {
    $email = htmlspecialchars($json['email']);
    $password = htmlspecialchars($json['password']);

    $getUser = $bdd->prepare("SELECT * FROM USERS where email = :email");
    $getUser->execute(array("email"=>$email));

    if ($getUser->rowCount() > 0) {
        $user = $getUser->fetch();

        if (password_verify($password, $user[3])) {
            $date = date('Y-m-d');
            if ($user[4] == null or $user[4] <= $date) {
                $result["success"] = true;
                if ($user[4] == null) {
                    $result["A vie"] = true;
                }
                else {
                    $result["A vie"] = false;
                }
            }
            else {
                $result["success"] = false;
                $result["error"] = "Veuillez renouveler votre abonnement";
                $result["repay"] = true;
            }
        } else {
            $result["success"] = false;
            $result["error"] = "Mot de passe incorrect";
        }
    }
    else {
        $result["success"] = false;
        $result["error"] = "Utilisateur non inscrit";
    }
}
else {
    $result["success"] = false;
    $result["error"] = "Champs vides";
}

echo json_encode($result);

?>