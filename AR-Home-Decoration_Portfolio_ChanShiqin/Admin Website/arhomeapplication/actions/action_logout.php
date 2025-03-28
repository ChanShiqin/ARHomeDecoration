<?php
session_start();

// Destroy all session data
session_unset();
session_destroy();

// Also delete the "remember me" cookies if they exist
setcookie('email', '', time() - 3600, '/');
setcookie('password', '', time() - 3600, '/');

// $logoutTime = date('Y-m-d H:i:s');
// $db->update("admin/$adminId", ['lastOrderCheck' => $logoutTime]);
// session_destroy();
// header("Location: login.php");


// Redirect to the login page or another desired page
header("Location: ../views/login.php"); // Adjust the path to your login page
exit();
?>
