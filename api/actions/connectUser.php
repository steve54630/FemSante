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

        if (password_verify($password, $user['password'])) {
            $result["success"] = true;
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