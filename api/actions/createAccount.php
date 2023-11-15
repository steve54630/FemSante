<?php

header('Content-Type: application/json');
include_once '../config/Database.php';

if (isset($json['email']) and isset($json['password'])) {
    $email = htmlspecialchars($json['email']);
    $password = htmlspecialchars($json['password']);
    $passwordHashed = password_hash($password, PASSWORD_DEFAULT);
        
    if ($email == "" or $password == "") {
        $result["success"] = false;
        $result["error"] = "Le mot de passe et/ou l'email n'est pas renseigné";
    }
    else {
        $checkIfEmailExists = $bdd->prepare('SELECT id FROM USERS WHERE email = ?');
        $checkIfEmailExists->execute(array($email));

        if ($checkIfEmailExists->rowCount() > 0) {
            $result["success"] = false;
            $result["error"] = "Cet utilisateur existe déjà";
        }
        else {
            try{
                $createAccount = $bdd->prepare("INSERT INTO USERS (name, email, password)"
                ." VALUES (:name, :mail, :password)");
                $createAccount->execute(
                    array("name"=>$name,
                        "mail"=>$email, 
                        "password"=>$passwordHashed
                ));
                $result["success"] = true;
            }
            catch (Exception $e){
                $result["success"] = false;
                $result["error"] = "Erreur lié à la base de données";
            }

        }
    }
}
else {
        $result["success"] = false;
        $result["error"] = "Veuillez complétez tous les champs demandés";
}

echo json_encode($result);

?>
