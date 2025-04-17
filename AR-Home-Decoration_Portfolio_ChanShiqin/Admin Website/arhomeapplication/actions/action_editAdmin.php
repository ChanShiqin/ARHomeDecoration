<?php
session_start(); 

include("../config.php");
include("../firebaseRDB.php");

$db = new firebaseRDB($databaseURL);

// Retrieve the submitted data
$adminId = $_POST['id'];
$adminProfilePic = isset($_FILES['adminProfilePic']) ? $_FILES['adminProfilePic'] : null;
$adminName = $_POST['adminName'];
$adminPhoneNo = $_POST['adminPhoneNo'];
$adminEmail = $_POST['adminEmail'];
$adminDepartment = $_POST['adminDepartment'];
$adminRoleLevel = $_POST['adminRoleLevel'];
$adminPassword = $_POST['adminPassword'];

// Prepare data to update
$updatedData = [
    'adminName' => $adminName,
    'adminPhoneNo' => $adminPhoneNo,
    'adminEmail' => $adminEmail,
    'adminDepartment' => $adminDepartment,
    'adminRoleLevel' => $adminRoleLevel,
    'adminPassword' => $adminPassword
];

// If a new profile picture is uploaded, handle the file upload and update icon data
if ($adminProfilePic && $adminProfilePic['size'] > 0) {
    $picData = base64_encode(file_get_contents($adminProfilePic['tmp_name']));
    $updatedData['adminProfilePic'] = $picData;
}

// Update admin data in Firebase
$updateResult = $db->update("admin", $adminId, $updatedData);

if ($updateResult) {
    $_SESSION['success'] = "Admin updated successfully!";
} else {
    $_SESSION['error'] = "Failed to update admin.";
}

header("Location: ../views/admin.php");
exit;
