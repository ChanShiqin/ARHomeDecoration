<?php
session_start(); 

include("../config.php");
include("../firebaseRDB.php");

$db = new firebaseRDB($databaseURL);

// Retrieve the submitted data
$supplierId = $_POST['id'];
// $supplierCompanyName = $_POST['supplierCompanyName'];
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

// Prepare data to update
$updatedData = [
    // 'supplierCompanyName' => $supplierCompanyName,
    'supplierCompanyPhoneNo' => $supplierCompanyPhoneNo,
    'supplierCompanyEmail' => $supplierCompanyEmail,
    'supplierAddress' => $supplierAddress,
    'supplierPICName' => $supplierPICName,
    'supplierPICPhoneNo' => $supplierPICPhoneNo,
    'supplierPICEmail' => $supplierPICEmail,
    'hasContract' => $hasContract,
    'supplierContractStartDate' => $supplierContractStartDate,
    'supplierContractEndDate' => $supplierContractEndDate,
    'supplierRemark' => $supplierRemark
];


// Update category in Firebase
$updateResult = $db->update("supplier", $supplierId, $updatedData);

if ($updateResult) {
    $_SESSION['success'] = "Supplier updated successfully!";
} else {
    $_SESSION['error'] = "Failed to update supplier.";
}

header("Location: ../views/supplier.php");
exit;
