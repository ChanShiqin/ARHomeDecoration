<?php
session_start();

include("../config.php");
include("../firebaseRDB.php");

$categoryName = $_POST['categoryName'];
$categoryDescription = $_POST['categoryDescription'];
$categoryIcon = $_FILES['categoryIcon'];
// $categoryStatus = isset($_POST['categoryStatus']) ? $_POST['categoryStatus'] : 'Inactive'; // Default to "Inactive" if not set

// Validate inputs
if (empty($categoryName)) {
    $_SESSION['error'] = 'Category name is required';
    header("Location: ../views/addCategory.php");
    exit();
}

if ($categoryIcon['error'] !== UPLOAD_ERR_OK) {
    $_SESSION['error'] = 'Error uploading file';
    header("Location: ../views/addCategory.php");
    exit();
}

if ($categoryIcon['type'] !== 'image/png') {
    $_SESSION['error'] = 'Only PNG files are allowed';
    header("Location: ../views/addCategory.php");
    exit();
}

// Read file content and convert to base64
$fileContent = file_get_contents($categoryIcon['tmp_name']);
$base64 = base64_encode($fileContent);

// Initialize Firebase
$db = new firebaseRDB($databaseURL);

// Retrieve the current list of categories to determine the next ID
$categories = $db->retrieve("/category");
$data = json_decode($categories, true);

// Determine the new category ID
if ($data === null || empty($data)) {
    // If no data exists, start with C0001
    $newCategoryId = "C0001";
} else {
    // Find the latest category ID and increment it
    $lastCategoryId = null;
    foreach ($data as $key => $value) {
        if (isset($value['id'])) {
            $lastCategoryId = $value['id'];
        }
    }
    
    if ($lastCategoryId !== null) {
        $lastCategoryNumber = (int) substr($lastCategoryId, 1); // Extract number part
        $newCategoryNumber = $lastCategoryNumber + 1; // Increment the number
        $newCategoryId = "C" . str_pad($newCategoryNumber, 4, "0", STR_PAD_LEFT); // Format as CXXXX
    } else {
        $newCategoryId = "C0001"; // Default to C0001 if no ID found
    }
}

// Check if category name already exists
$categoryExists = false;
foreach ($data as $key => $value) {
    if ($value['categoryName'] === $categoryName) {
        $categoryExists = true;
        break;
    }
}

if ($categoryExists) {
    $_SESSION['error'] = 'Category already exists';
    header("Location: ../views/addCategory.php");
    exit();
} else {
    // Insert data into Firebase with custom ID as a field
    $insert = $db->insert("/category", [
        "id" => $newCategoryId,  // Store the custom ID as a field
        "categoryName" => $categoryName,
        "categoryDescription" => $categoryDescription,
        "categoryIcon" => $base64
        // "categoryStatus" => $categoryStatus // Save status as Active or Inactive
    ]);

    if ($insert) {
        $_SESSION['success'] = 'Category added successfully';
        header("Location: ../views/category.php");
    } else {
        $_SESSION['error'] = 'Failed to add category';
        header("Location: ../views/addCategory.php");
    }
}
?>
