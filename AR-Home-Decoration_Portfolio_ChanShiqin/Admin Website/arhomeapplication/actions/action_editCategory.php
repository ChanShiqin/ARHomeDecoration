<?php
session_start(); 

include("../config.php");
include("../firebaseRDB.php");

$db = new firebaseRDB($databaseURL);

// Retrieve the submitted data
$categoryId = $_POST['id'];
// $categoryName = $_POST['categoryName'];
$categoryDescription = $_POST['categoryDescription'];
// $categoryStatus = $_POST['categoryStatus'];
$categoryIcon = isset($_FILES['categoryIcon']) ? $_FILES['categoryIcon'] : null;

// Prepare data to update
$updatedData = [
    // 'categoryName' => $categoryName,
    'categoryDescription' => $categoryDescription,
    // 'categoryStatus' => $categoryStatus,
];

// If a new icon is uploaded, handle the file upload and update icon data
if ($categoryIcon && $categoryIcon['size'] > 0) {
    $iconData = base64_encode(file_get_contents($categoryIcon['tmp_name']));
    $updatedData['categoryIcon'] = $iconData;
}

// Update category in Firebase
$updateResult = $db->update("category", $categoryId, $updatedData);

if ($updateResult) {
    $_SESSION['success'] = "Category updated successfully!";
} else {
    $_SESSION['error'] = "Failed to update category.";
}

header("Location: ../views/category.php");
exit;
