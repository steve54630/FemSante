<?php

header('Content-Type: application/json');
include_once '../config/Database.php';

if (isset($json['email']) and isset($json['password'])) {
    $email = htmlspecialchars($json['email']);
    $password = htmlspecialchars($json['password']);
    $passwordHashed = password_hash($password, PASSWORD_DEFAULT);
    $answer = htmlspecialchars($json['answer']);
    $answerHashed = password_hash($answer, PASSWORD_DEFAULT);
    $numberdays = htmlspecialchars($json['days']);
    if ($numberdays == "A vie") {
        $date = null;
    }
    else {
        $today = new DateTime();
        $date = date_add($today,date_interval_create_from_date_string($numberdays." days"));
        $date = date_format($date,"Y-m-d");
    }
    $name = htmlspecialchars($json['name']); 
    
    if ($answer != "") {
        $answer = password_hash($answer, PASSWORD_DEFAULT);
    }

    if ($email == "" or $password == "") {
        $result["success"] = false;
        $result["error"] = "Erreur système : Veuillez contacter le développeur";
    }
    else {
        try{
            $createAccount = $bdd->prepare("INSERT INTO USERS (name, email, password, " 
            ."quest_id, quest_answer, valid_date) VALUES (:name, :mail, :password, :id, :answer, :date)");
            $createAccount->execute(
                    array("name"=>$name,
                        "mail"=>$email, 
                        "password"=>$passwordHashed,
                        "id"=> htmlspecialchars($json['id']),
                        "answer"=> $answer,
                        "date"=> $date
                ));
                $result["success"] = true;
            }
            catch (Exception $e){
                $result["success"] = false;
                $result["error"] = "Erreur système : Veuillez contacter le développeur";
            }

        }
    }
else {
        $result["success"] = false;
        $result["error"] = "Erreur système : Veuillez contacter le développeur";
}

echo json_encode($result);

?>
