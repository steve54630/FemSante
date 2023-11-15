<?

header('Content-Type: application/json');
include_once '../config/Database.php';

if (isset($json['email']) and isset($json['answer'])) {
    $email = htmlspecialchars($json['email']);
    $answer = htmlspecialchars($json['answer']);
    
    if ($email == "" or $answer == "") {
        $result["success"] = false;
        $result["error"] = "Le nouveau mot de passe et/ou la réponse n'est pas renseigné";
    }
    else {
        $checkIfEmailExists = $bdd->prepare('SELECT id FROM USERS WHERE email = ?');
        $checkIfEmailExists->execute(array($email));

        if ($checkIfEmailExists->rowCount() > 0) {
            $result["success"] = false;
            $result["error"] = "Cet utilisateur n'existe pas";
        }
        else {
            $user = $checkIfEmailExists->fetch();
            if ($user['quest_id'] != $json['id']){
                $result["success"] = false;
                $result["error"] = "Utilisateur non vérifié";
            }
            else {
                if (!password_verify($answer, $user['quest_answer'])) {
                    $result["success"] = false;
                    $result["error"] = "Réponse incorrecte";
                }
                else {
                    try {
                        $password = htmlspecialchars($json['$password']);
                        $passwordHashed = password_hash($password, PASSWORD_DEFAULT);
                        $updatePassword = $bdd->prepare('UPDATE USERS SET password = ? where email = ?');
                        $updatePassword->execute(array($passwordHashed,$email));
                        $result["success"] = true;
                    }
                    catch (Exception $e) {
                        $result["success"] = false;
                        $result["error"] = "Erreur lié à la base de données";
                    }
                }
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