<?php

header('Content-Type: application/json');
include_once '../config/Database.php';

if (isset($json['email']) and isset($json['password'])) {
    $email = htmlspecialchars($json['email']);
    $password = htmlspecialchars($json['password']);
    $numberdays = htmlspecialchars($json['days']);

    $getUser = $bdd->prepare("SELECT * FROM USERS where email = :email");
    $getUser->execute(array("email"=>$email));

    if ($getUser->rowCount() > 0) {
        $user = $getUser->fetch();

        if (password_verify($password, $user['password'])) {
            try {
                if ($numberdays == "A vie") {
                    $date = null;
                }
                else {
                    $date = date_add($user['verif_date'],date_interval_create_from_date_string($numberdays." days"));
                    $date = date_format($date,"Y-m-d");
                }
                $updateUser = $bdd -> prepare("UPDATE USERS SET valid_date = :date WHERE email = :email");
                $updateUser->execute(
                    array("date" => $date,
                        "email" => $email)
                );
            }
            catch (Exception $e) {
                $result["success"] = false;
                $result["error"] = "Erreur système : Veuillez contacter le développeur";
            }
        } else {
            $result["success"] = false;
            $result["error"] = "Erreur système : Veuillez contacter le développeur";
        }
    }
    else {
        $result["success"] = false;
        $result["error"] = "Erreur système : Veuillez contacter le développeur";
    }
}
else {
    $result["success"] = false;
    $result["error"] = "Erreur système : Veuillez contacter le développeur";
}

echo json_encode($result);

?>
