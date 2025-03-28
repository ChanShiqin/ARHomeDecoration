<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

session_start();
include("../config.php");
include("../firebaseRDB.php");

$email = strtolower($_POST['email']);
$password = $_POST['password'];

if ($email == "") {
    echo "Email is required";
} elseif ($password == "") {
    echo "Password is required";
} else {
    $db = new firebaseRDB($databaseURL);
    $retrieve = $db->retrieve("/admin", "adminEmail", "EQUAL", $email);
    $data = json_decode($retrieve, true);

    if (isset($data['error'])) {
        echo $data['error'];
        exit();
    }

    if (!is_array($data) || count($data) == 0) {
        $_SESSION['error_message'] = "Email not registered";
        header("Location: ../views/login.php");  // Redirect back to login page with error
        exit();
        echo "Email not registered";
    } else {
        $id = array_keys($data)[0];
        if ($data[$id]['adminPassword'] == $password) {
            $_SESSION['admin'] = [
                'id' => $id,
                'adminName' => $data[$id]['adminName'] ?? 'Admin',
            ];

            if (isset($_POST['remember_me'])) {
                setcookie('email', $email, time() + (86400 * 30), "/");  // Save for 30 days
                setcookie('password', $password, time() + (86400 * 30), "/");
            } else {
                setcookie('email', '', time() - 3600, "/");
                setcookie('password', '', time() - 3600, "/");
            }

            // if (isset($_POST['remember_me'])) {
            //     setcookie('email', $email, time() + (86400 * 30), "/");
            //     setcookie('password', $password, time() + (86400 * 30), "/");
            // } else {
            //     setcookie('email', '', time() - 3600, "/");
            //     setcookie('password', '', time() - 3600, "/");
            // }

            header("location: ../views/dashboard.php");
            exit();
        } else {
            $_SESSION['error_message'] = "Invalid password, please try again.";
            header("Location: ../views/login.php");  // Redirect back to login page with error
            exit();
            echo "Login failed: Incorrect password";
        }
    }
}

?>