<?php
session_start();

// Set the timeout duration (in seconds)
$timeout_duration = 1800; // 30 minutes

// Check if the session is new or if the user has been inactive for too long
if (isset($_SESSION['last_activity']) && (time() - $_SESSION['last_activity']) > $timeout_duration) {
    session_unset();
    session_destroy();
    header("Location: login.php"); // Redirect to login page if timed out
    exit();
}

$_SESSION['last_activity'] = time(); // Update the last activity time

include("../config.php");
include("../firebaseRDB.php");

$db = new firebaseRDB($databaseURL);
$productId = $_GET['id'];

// Check if the category ID is provided in the URL
if ($productId != "") {
    $productId = $_GET['id'];

    // Attempt to delete the category
    $deleteResponse = $db->delete("product" , $productId);

    // Check if delete was successful
    if ($deleteResponse === false || isset($deleteResponse['error'])) {
        $_SESSION['error'] = "Failed to delete the product. Please try again. Error: " . json_encode($deleteResponse);
    } else {
        // $_SESSION['success'] = $categoryId;
        $_SESSION['success'] = "Product deleted successfully.";
    }
} else {
    $_SESSION['error'] = "Product ID is missing.";
}

// Redirect back to category.php after the operation
header('Location: ../views/product.php');
exit;
?>
