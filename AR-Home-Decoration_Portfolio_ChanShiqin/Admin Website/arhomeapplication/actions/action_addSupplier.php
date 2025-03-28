<?php
// Turn on error reporting
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
?>


<?php
session_start();

include("../config.php");
include("../firebaseRDB.php");

$supplierCompanyName = $_POST['supplierCompanyName'];
$supplierCompanyPhoneNo = $_POST['supplierCompanyPhoneNo'];
$supplierCompanyEmail = $_POST['supplierCompanyEmail'];
$supplierAddress = $_POST['supplierAddress'];
$supplierPICName = $_POST['supplierPICName'];
$supplierPICPhoneNo = $_POST['supplierPICPhoneNo'];
$supplierPICEmail = $_POST['supplierPICEmail'];
$hasContract = $_POST['hasContract'];
$supplierContractStartDate = $_POST['supplierContractStartDate'];
$supplierContractEndDate = $_POST['supplierContractEndDate'];
$supplierRemark = $_POST['supplierRemark'];

// Validate inputs
if (empty($supplierCompanyName)) {
    $_SESSION['error'] = 'supplier company name is required';
    header("Location: ../views/addSupplier.php");
    exit();
}

if (empty($supplierCompanyPhoneNo)) {
    $_SESSION['error'] = 'supplier company phone no. is required';
    header("Location: ../views/addSupplier.php");
    exit();
}

if (empty($supplierPICName)) {
    $_SESSION['error'] = 'supplier PIC name is required';
    header("Location: ../views/addSupplier.php");
    exit();
}

if (empty($supplierPICPhoneNo)) {
    $_SESSION['error'] = 'supplier PIC phone no. is required';
    header("Location: ../views/addSupplier.php");
    exit();
}

if (empty($hasContract)) {
    $_SESSION['error'] = 'supplier contract status is required';
    header("Location: ../views/addSupplier.php");
    exit();
}

// Initialize Firebase
$db = new firebaseRDB($databaseURL);

// Retrieve the current list of supplier to determine the next ID
$suppliers = $db->retrieve("/supplier");
$data = json_decode($suppliers, true);

// Determine the new supplier ID
if ($data === null || empty($data)) {
    // If no data exists, start with C0001
    $newSupplierId = "S0001";
} else {
    // Find the latest category ID and increment it
    $lastSupplieryId = null;
    foreach ($data as $key => $value) {
        if (isset($value['id'])) {
            $lastSupplieryId = $value['id'];
        }
    }
    
    if ($lastSupplieryId !== null) {
        $lastSupplierNumber = (int) substr($lastSupplieryId, 1); // Extract number part
        $newSupplierNumber = $lastSupplierNumber + 1; // Increment the number
        $newSupplierId = "S" . str_pad($newSupplierNumber, 4, "0", STR_PAD_LEFT); // Format as CXXXX
    } else {
        $newSupplierId = "S0001"; // Default to C0001 if no ID found
    }
}

// Check if supplier company name already exists
$supplierExists = false;
foreach ($data as $key => $value) {
    if ($value['supplierCompanyName'] === $supplierCompanyName) {
        $supplierExists = true;
        break;
    }
}

if ($supplierExists) {
    $_SESSION['error'] = 'Supplier company already exists';
    header("Location: ../views/addSupplier.php");
    exit();
} else {
    // Insert data into Firebase with custom ID as a field
    $insert = $db->insert("/supplier", [
        "id" => $newSupplierId,  // Store the custom ID as a field
        "supplierCompanyName" => $supplierCompanyName,
        "supplierCompanyPhoneNo" => $supplierCompanyPhoneNo,
        "supplierCompanyEmail" => $supplierCompanyEmail,
        "supplierAddress" => $supplierAddress,
        "supplierPICName" => $supplierPICName,
        "supplierPICPhoneNo" => $supplierPICPhoneNo,
        "supplierPICEmail" => $supplierPICEmail,
        "hasContract" => $hasContract,
        "supplierContractStartDate" => $supplierContractStartDate,
        "supplierContractEndDate" => $supplierContractEndDate,
        "supplierRemark" => $supplierRemark
    ]);

    if ($insert) {
        $_SESSION['success'] = 'Supplier added successfully';
        header("Location: ../views/supplier.php");
    } else {
        $_SESSION['error'] = 'Failed to add supplier';
        header("Location: ../views/addSupplier.php");
    }
}

?>