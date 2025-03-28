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
$categoryId = $_GET['id']; // Get the categoryId from the URL

// Retrieve category data based on categoryId
$retrieve = $db->retrieve("category/$categoryId");
$data = json_decode($retrieve, 1);

// Check if the category ID is provided in the URL
if ($categoryId != "") {
    // Retrieve all products to check if any are using this category
    $products = $db->retrieve("product");
    $products = json_decode($products, 1);

    $categoryInUse = false;

    // Loop through the products to check if any are using this category
    foreach ($products as $product) {
        if (isset($product['productCategory']) && $product['productCategory'] == $data['categoryName']) {
            $categoryInUse = true;
            break; // Exit the loop if a product is using this category
        }
    }

    if ($categoryInUse) {
        // The category is being used by a product, so do not delete
        $_SESSION['error'] = "This category is being used by a product and cannot be deleted.";
    } else {
        // Attempt to delete the category
        $deleteResponse = $db->delete("category", $categoryId);

        // Check if delete was successful
        if ($deleteResponse === false || isset($deleteResponse['error'])) {
            $_SESSION['error'] = "Failed to delete the category. Please try again. Error: " . json_encode($deleteResponse);
        } else {
            $_SESSION['success'] = "Category deleted successfully.";
        }
    }
} else {
    $_SESSION['error'] = "Category ID is missing.";
}

// Redirect back to category.php after the operation
header('Location: ../views/category.php');
exit;
?>
