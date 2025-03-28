<?php
// Turn on error reporting
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

session_start(); 

include("../config.php");
include("../firebaseRDB.php");

$db = new firebaseRDB($databaseURL);

// Retrieve the submitted data for updating the product
$productId = $_POST['id'];
$productName = $_POST['productName'];
$productDescription = $_POST['productDescription'];
$productBrandName = $_POST['productBrandName'];
$productCategory = $_POST['productCategory'];
$productSupplier = $_POST['productSupplier'];
// $productSizeLength = $_POST['productSizeLength'];
$productSizeWidth = $_POST['productSizeWidth'];
$productSizeHeight = $_POST['productSizeHeight'];
$productSizeDepth = $_POST['productSizeDepth'];
$productWeight = $_POST['productWeight'];
$productMaterial = $_POST['productMaterial'];
$hasWarranty = $_POST['hasWarranty'];
$warrantyDurationUnit = $_POST['warrantyDurationUnit'];
$warrantyDurationValue = $_POST['warrantyDurationValue'];
$productImages = json_decode($_POST['base64Images'], true); 

// Decode JSON-encoded arrays for product specs
$productDesign = json_decode($_POST['productDesign'], true) ?? [];
$productPrice = json_decode($_POST['productPrice'], true) ?? [];
$productStock = json_decode($_POST['productStock'], true) ?? [];

$productARURL = $_POST['productARURL'];

// Check if arrays are not null or empty
if (!is_array($productDesign) || !is_array($productPrice) || !is_array($productStock)) {
    $_SESSION['error'] = 'Invalid product specification data';
    header("Location: ../views/editProduct.php?id=" . $productId);
    exit();
}

// Initialize productSpecs array
$productSpecs = [];
$maxLength = max(count($productDesign), count($productPrice), count($productStock));

// Loop through to structure product specs array
for ($i = 0; $i < $maxLength; $i++) {
    $productSpecs[] = [
        "productDesign" => $productDesign[$i] ?? null,
        "productPrice" => isset($productPrice[$i]) ? floatval($productPrice[$i]) : null,
        "productStock" => isset($productStock[$i]) ? intval($productStock[$i]) : null
    ];
}

// Prepare data to update
$updatedData = [
    "productName" => $productName,
    "productDescription" => $productDescription,
    "productBrandName" => $productBrandName,
    "productCategory" => $productCategory,
    "productSupplier" => $productSupplier,
    // "productSizeLength" => $productSizeLength,
    "productSizeWidth" => $productSizeWidth,
    "productSizeHeight" => $productSizeHeight,
    "productSizeDepth" => $productSizeDepth,
    "productWeight" => $productWeight,
    "productMaterial" => $productMaterial,
    "hasWarranty" => $hasWarranty,
    "warrantyDurationUnit" => $warrantyDurationUnit,
    "warrantyDurationValue" => $warrantyDurationValue,
    "productImages" => $productImages,
    "productAR" => $productARURL,
    "productSpecs" => $productSpecs
];

// Update product data in Firebase
$updateResult = $db->update("product", $productId, $updatedData);

if ($updateResult) {
    $_SESSION['success'] = "Product updated successfully!";
} else {
    $_SESSION['error'] = "Failed to update product.";
}

// Redirect back to the product page
header("Location: ../views/product.php");
exit;
?>
