<?php
session_start();

include("../config.php");
include("../firebaseRDB.php");

$customerProfilePic = $_FILES['customerProfilePic'];
$customerName = $_POST['customerName'];
$customerPhoneNo = $_POST['customerPhoneNo'];
$customerEmail = $_POST['customerEmail'];
$customerPassword = $_POST['customerPassword'];
$customerAddress = $_POST['customerAddress'];
$customerPoscode = $_POST['customerPoscode'];
$customerState = $_POST['customerState'];

// Validate inputs
if (empty($customerProfilePic)) {
    $_SESSION['error'] = 'Customer profile pic is required';
    header("Location: ../views/addCustomer.php");
    exit();
}

if ($customerProfilePic['error'] !== UPLOAD_ERR_OK) {
    $_SESSION['error'] = 'Error uploading file';
    header("Location: ../views/addCustomer.php");
    exit();
}

// if ($categoryIcon['type'] !== 'image/png') {
//     $_SESSION['error'] = 'Only PNG files are allowed';
//     header("Location: ../views/addCategory.php");
//     exit();
// }

if (empty($customerName)) {
    $_SESSION['error'] = 'Customer name is required';
    header("Location: ../views/addCustomer.php");
    exit();
}

if (empty($customerPhoneNo)) {
    $_SESSION['error'] = 'Customer phone no. is required';
    header("Location: ../views/addCustomer.php");
    exit();
}

if (empty($customerEmail)) {
    $_SESSION['error'] = 'Customer email is required';
    header("Location: ../views/addCustomer.php");
    exit();
}

if (empty($customerPassword)) {
    $_SESSION['error'] = 'Customer account password is required';
    header("Location: ../views/addCustomer.php");
    exit();
}

if (empty($customerAddress)) {
    $_SESSION['error'] = 'Customer address is required';
    header("Location: ../views/addCustomer.php");
    exit();
}

if (empty($customerPoscode)) {
    $_SESSION['error'] = 'Customer account poscode is required';
    header("Location: ../views/addCustomer.php");
    exit();
}

if (empty($customerState)) {
    $_SESSION['error'] = 'Customer account state is required';
    header("Location: ../views/addCustomer.php");
    exit();
}

// Check if the uploaded file is an image
$imageFileType = pathinfo($customerProfilePic['name'], PATHINFO_EXTENSION);
$allowedTypes = ['jpg', 'jpeg', 'png', 'gif']; // Allow common image types

if (!in_array(strtolower($imageFileType), $allowedTypes)) {
    $_SESSION['error'] = 'Only JPG, JPEG, PNG, or GIF files are allowed';
    header("Location: ../views/addCustomer.php");
    exit();
}

// Read file content and convert to Base64
$fileContent = file_get_contents($customerProfilePic['tmp_name']);
$base64Image = base64_encode($fileContent);

// // Read file content and convert to base64
// $fileContent = file_get_contents($categoryIcon['tmp_name']);
// $base64 = base64_encode($fileContent);

// Initialize Firebase
$db = new firebaseRDB($databaseURL);

// Retrieve the current list of categories to determine the next ID
$customers = $db->retrieve("/user");
$data = json_decode($customers, true);

// Determine the new category ID
if ($data === null || empty($data)) {
    // If no data exists, start with C0001
    $newCustomerId = "C00001";
} else {
    // Find the latest category ID and increment it
    $lastCustomerId = null;
    foreach ($data as $key => $value) {
        if (isset($value['id'])) {
            $lastCustomerId = $value['id'];
        }
    }
    
    if ($lastCustomerId !== null) {
        $lastCustomerNumber = (int) substr($lastCustomerId, 1); // Extract number part
        $newCustomerNumber = $lastCustomerNumber + 1; // Increment the number
        $newCustomerId = "C" . str_pad($newCustomerNumber, 5, "0", STR_PAD_LEFT); // Format as CXXXX
    } else {
        $newCustomerId = "C00001"; // Default to C0001 if no ID found
    }
}

// Check if category name already exists
$customerExists = false;
foreach ($data as $key => $value) {
    if ($value['email'] === $customerEmail) {
        $customerExists = true;
        break;
    }
}

if ($customerExists) {
    $_SESSION['error'] = 'Customer email already registered';
    header("Location: ../views/addCustomer.php");
    exit();
} else {
    // Insert data into Firebase with custom ID as a field
    $insert = $db->insert("/user", [
        "id" => $newCustomerId,  // Store the custom ID as a field
        "profilePic" => $base64Image,
        "name" => $customerName,
        "phoneNo" => $customerPhoneNo,
        "email" => $customerEmail,
        "password" => $customerPassword, // Save status as Active or Inactive
        "address" => $customerAddress,
        "poscode" => $customerPoscode,
        "state" => $customerState
    ]);

    if ($insert) {
        $_SESSION['success'] = 'Customer account added successfully';
        header("Location: ../views/customer.php");
    } else {
        $_SESSION['error'] = 'Failed to add customer account';
        header("Location: ../views/addCustomer.php");
    }
}
?>
