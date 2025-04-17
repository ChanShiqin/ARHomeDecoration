<?php
session_start();

include("../config.php");
include("../firebaseRDB.php");

$adminProfilePic = $_FILES['adminProfilePic'];
$adminName = $_POST['adminName'];
$adminPhoneNo = $_POST['adminPhoneNo'];
$adminEmail = $_POST['adminEmail'];
$adminDepartment = $_POST['adminDepartment'];
$adminRoleLevel = $_POST['adminRoleLevel'];
$adminPassword = $_POST['adminPassword'];

// Validate inputs
if (empty($adminProfilePic)) {
    $_SESSION['error'] = 'Admin profile pic is required';
    header("Location: ../views/addAdmin.php");
    exit();
}

if ($adminProfilePic['error'] !== UPLOAD_ERR_OK) {
    $_SESSION['error'] = 'Error uploading file';
    header("Location: ../views/addAdmin.php");
    exit();
}

// if ($categoryIcon['type'] !== 'image/png') {
//     $_SESSION['error'] = 'Only PNG files are allowed';
//     header("Location: ../views/addCategory.php");
//     exit();
// }

if (empty($adminName)) {
    $_SESSION['error'] = 'Admin name is required';
    header("Location: ../views/addAdmin.php");
    exit();
}

if (empty($adminPhoneNo)) {
    $_SESSION['error'] = 'Admin phone no. is required';
    header("Location: ../views/addAdmin.php");
    exit();
}

if (empty($adminEmail)) {
    $_SESSION['error'] = 'Admin email is required';
    header("Location: ../views/addAdmin.php");
    exit();
}

if (empty($adminDepartment)) {
    $_SESSION['error'] = 'Admin department is required';
    header("Location: ../views/addAdmin.php");
    exit();
}

if (empty($adminRoleLevel)) {
    $_SESSION['error'] = 'Admin role level is required';
    header("Location: ../views/addAdmin.php");
    exit();
}

if (empty($adminPassword)) {
    $_SESSION['error'] = 'Admin account password is required';
    header("Location: ../views/addAdmin.php");
    exit();
}

// Check if the uploaded file is an image
$imageFileType = pathinfo($adminProfilePic['name'], PATHINFO_EXTENSION);
$allowedTypes = ['jpg', 'jpeg', 'png', 'gif']; // Allow common image types

if (!in_array(strtolower($imageFileType), $allowedTypes)) {
    $_SESSION['error'] = 'Only JPG, JPEG, PNG, or GIF files are allowed';
    header("Location: ../views/addAdmin.php");
    exit();
}

// Read file content and convert to Base64
$fileContent = file_get_contents($adminProfilePic['tmp_name']);
$base64Image = base64_encode($fileContent);

// // Read file content and convert to base64
// $fileContent = file_get_contents($categoryIcon['tmp_name']);
// $base64 = base64_encode($fileContent);

// Initialize Firebase
$db = new firebaseRDB($databaseURL);

// Retrieve the current list of categories to determine the next ID
$admins = $db->retrieve("/admin");
$data = json_decode($admins, true);

// Determine the new category ID
if ($data === null || empty($data)) {
    // If no data exists, start with C0001
    $newAdminId = "A0001";
} else {
    // Find the latest category ID and increment it
    $lastAdminId = null;
    foreach ($data as $key => $value) {
        if (isset($value['id'])) {
            $lastAdminId = $value['id'];
        }
    }
    
    if ($lastAdminId !== null) {
        $lastAdminNumber = (int) substr($lastAdminId, 1); // Extract number part
        $newAdminNumber = $lastAdminNumber + 1; // Increment the number
        $newAdminId = "C" . str_pad($newAdminNumber, 4, "0", STR_PAD_LEFT); // Format as CXXXX
    } else {
        $newAdminId = "A0001"; // Default to C0001 if no ID found
    }
}

// Check if category name already exists
$adminExists = false;
foreach ($data as $key => $value) {
    if ($value['adminEmail'] === $adminEmail) {
        $adminExists = true;
        break;
    }
}

if ($adminExists) {
    $_SESSION['error'] = 'Admin email already exists';
    header("Location: ../views/addAdmin.php");
    exit();
} else {
    // Insert data into Firebase with custom ID as a field
    $insert = $db->insert("/admin", [
        "id" => $newAdminId,  // Store the custom ID as a field
        "adminProfilePic" => $base64Image,
        "adminName" => $adminName,
        "adminPhoneNo" => $adminPhoneNo,
        "adminEmail" => $adminEmail,
        "adminDepartment" => $adminDepartment,
        "adminRoleLevel" => $adminRoleLevel,
        "adminPassword" => $adminPassword // Save status as Active or Inactive
    ]);

    if ($insert) {
        $_SESSION['success'] = 'Admin account added successfully';
        header("Location: ../views/admin.php");
    } else {
        $_SESSION['error'] = 'Failed to add admin account';
        header("Location: ../views/addAdmin.php");
    }
}
?>
