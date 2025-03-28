<?php
include("../config.php");
include("../firebaseRDB.php");

$name = $_POST['name'];
$email = $_POST['email'];
$username = $_POST['username'];
$password = $_POST['password'];

if($name == ""){
    echo "Name is required";
}else if($email == ""){
    echo "Email is required";
}else if($username == ""){
    echo "Username is required";
}else if($password == ""){
    echo "Password is required";
}else{
    $db = new firebaseRDB($databaseURL);
    $retrieve = $db -> retrieve("/admin", "username", "EQUAL", $username);
    $data = json_decode($retrieve, 1);

    if(isset($data['username'])){
        echo "username already used";
    }else{
        $insert = $db -> insert("/admin", [
            "name" => $name,
            "email" => $email,
            "username" => $username,
            "password" => $password
        ]);

        $result = json_decode($insert, 1);
        if(isset($result['name'])){
            echo "Signup success, please login";
        }else{
            echo "Signup failed";
        }
    }
}