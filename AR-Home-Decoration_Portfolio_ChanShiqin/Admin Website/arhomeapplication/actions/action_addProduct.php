<?php
// Turn on error reporting
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

session_start();

include("../config.php");
include("../firebaseRDB.php");

$db = new firebaseRDB($databaseURL);

// Retrieve form data
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

// if (empty($productARURL)) {
//     $_SESSION['error'] = 'AR file URL is required';
//     header("Location: ../views/addProduct.php");
//     exit();
// }

// Check if arrays are not null or empty
if (!is_array($productDesign) || !is_array($productPrice) || !is_array($productStock)) {
    $_SESSION['error'] = 'Invalid product specification data';
    header("Location: ../views/addProduct.php");
    exit();
}

// Initialize productSpecs as an array of associative arrays
$productSpecs = [];
$maxLength = max(count($productDesign), count($productPrice), count($productStock));

// Loop through the product specs and construct the desired structure
for ($i = 0; $i < $maxLength; $i++) {
    $productSpecs[] = [
        "productDesign" => $productDesign[$i] ?? null,
        "productPrice" => isset($productPrice[$i]) ? floatval($productPrice[$i]) : null,
        "productStock" => isset($productStock[$i]) ? intval($productStock[$i]) : null
    ];
}

// Retrieve the current list of products to determine the next ID
$products = $db->retrieve("/product");
$data = json_decode($products, true);

// Determine the new product ID
if ($data === null || empty($data)) {
    $newProductId = "P00001";
} else {
    $lastProductId = null;
    foreach ($data as $key => $value) {
        if (isset($value['id'])) {
            $lastProductId = $value['id'];
        }
    }
    if ($lastProductId !== null) {
        $lastProductNumber = (int) substr($lastProductId, 1);
        $newProductNumber = $lastProductNumber + 1;
        $newProductId = "P" . str_pad($newProductNumber, 5, "0", STR_PAD_LEFT);
    } else {
        $newProductId = "P00001";
    }
}

// Check if product name already exists
$productExists = false;
foreach ($data as $key => $value) {
    if ($value['productName'] === $productName) {
        $productExists = true;
        break;
    }
}

if ($productExists) {
    $_SESSION['error'] = 'Product already exists';
    header("Location: ../views/addProduct.php");
    exit();
} else {
    // Prepare data to insert
    $insertData = [
        "id" => $newProductId,
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

    // Insert data into Firebase with custom ID as a field
    $insert = $db->insert("/product", $insertData);

    if ($insert) {
        $_SESSION['success'] = 'Product added successfully';
        header("Location: ../views/product.php");
    } else {
        $_SESSION['error'] = 'Failed to add product';
        header("Location: ../views/addProduct.php");
    }
}
?>
