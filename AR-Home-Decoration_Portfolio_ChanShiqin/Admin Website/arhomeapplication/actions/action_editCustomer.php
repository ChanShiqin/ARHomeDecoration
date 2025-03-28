<?php
// Turn on error reporting
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

session_start(); 

include("../config.php");
include("../firebaseRDB.php");

$db = new firebaseRDB($databaseURL);

// Retrieve the submitted data
$customerId = $_POST['id'];
$profilePic = isset($_FILES['profilePic']) ? $_FILES['profilePic'] : null;
$customerName = $_POST['customerName'];
$customerPhoneNo = $_POST['customerPhoneNo'];
// $customerEmail = $_POST['customerEmail'];
// $customerPassword = $_POST['customerPassword'];
$customerAddress = $_POST['customerAddress'];
$customerPoscode = $_POST['customerPoscode'];
$customerState = $_POST['customerState'];

// Prepare data to update
$updatedData = [
    'name' => $customerName,
    'phoneNo' => $customerPhoneNo,
    // 'email' => $customerEmail,
    // 'password' => $customerPassword,
    'address' => $customerAddress,
    'poscode' => $customerPoscode,
    'state' => $customerState
];

// If a new icon is uploaded, handle the file upload and update icon data
if ($profilePic && $profilePic['size'] > 0) {
    $picData = base64_encode(file_get_contents($profilePic['tmp_name']));
    $updatedData['profilePic'] = $picData;
}

// Update category in Firebase
$updateResult = $db->update("user", $customerId, $updatedData);

if ($updateResult) {
    $_SESSION['success'] = "Customer updated successfully!";
} else {
    $_SESSION['error'] = "Failed to update customer.";
}

header("Location: ../views/customer.php");
exit;
