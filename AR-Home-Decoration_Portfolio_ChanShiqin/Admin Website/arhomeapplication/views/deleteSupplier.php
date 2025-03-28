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
$supplierId = $_GET['id']; // Get the supplierId from the URL

$retrieve = $db->retrieve("supplier/$supplierId");
$data = json_decode($retrieve, 1);

// Check if the supplier ID is provided in the URL
if ($supplierId != "") {
    // Retrieve all products to check if any are using this supplier
    $products = $db->retrieve("product");
    $products = json_decode($products, 1);

    $supplierInUse = false;

    // Loop through the products to check if any are using this supplierId
    foreach ($products as $product) {
        if (isset($product['productSupplier']) && $product['productSupplier'] == $data['supplierCompanyName']) {
            $supplierInUse = true;

            // $_SESSION['error'] = "The supplier is in use." . json_encode($deleteResponse);

            break; // Exit the loop if a product is using the supplierId
        }
    }

    if ($supplierInUse) {
        // The supplier is being used by a product, so do not delete
        $_SESSION['error'] = "This supplier is being used by a product and cannot be deleted.";
        header("Location: supplier.php");

    } else {
        // Attempt to delete the supplier
        $deleteResponse = $db->delete("supplier", $supplierId);

        // Check if delete was successful
        if ($deleteResponse === false || isset($deleteResponse['error'])) {
            $_SESSION['error'] = "Failed to delete the supplier. Please try again. Error: " . json_encode($deleteResponse);
        } else {
            $_SESSION['success'] = "Supplier deleted successfully.";
        }
    }
} else {
    $_SESSION['error'] = "Supplier ID is missing.";
}

// Redirect back to supplier.php after the operation
header('Location: ../views/supplier.php');
exit;
?>
