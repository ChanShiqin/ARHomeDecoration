<?php
// Turn on error reporting
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

session_start();

include("../config.php");
include("../firebaseRDB.php");

$db = new firebaseRDB($databaseURL);

date_default_timezone_set('Asia/Kuala_Lumpur');

// Get POST data
$orderId = $_POST['orderId'] ?? null;

if (!$orderId) {
    $_SESSION['error'] = "Invalid order ID.";
    header("Location: ../views/order.php");
    exit();
}

// Retrieve the order data
$orderDataJson = $db->retrieve("order/$orderId");
$orderData = json_decode($orderDataJson, true);

if (!$orderData) {
    $_SESSION['error'] = "Order not found.";
    header("Location: ../views/order.php");
    exit();
}

// Define the shipment steps
$shipmentSteps = ["Paid", "Processing", "Shipped", "Out for Delivery", "Delivered"];

// Get the current order status
$orderStatus = $orderData['orderStatus'] ?? []; // Ensure $orderStatus is a variable
$currentStatus = end($orderStatus); // Pass $orderStatus by reference
$currentStep = $currentStatus['updateStatus'] ?? null;

// Determine the next status
$nextStepIndex = array_search($currentStep, $shipmentSteps) + 1;

if ($nextStepIndex >= count($shipmentSteps)) {
    $_SESSION['message'] = "The order is already completed.";
    header("Location: ../views/editOrder.php?id=$orderId");
    exit();
}

// Update the order status
$newStatus = [
    "updateStatus" => $shipmentSteps[$nextStepIndex],
    "updateTime" => date("Y-m-d H:i:s"), // Current timestamp
];

// Append the new status to the order data
$orderData['orderStatus'][] = $newStatus;

// Save the updated data back to the database
$updateResult = $db->update("order", $orderId, $orderData);

if ($updateResult) {
    $_SESSION['message'] = "Order status updated successfully.";
} else {
    $_SESSION['error'] = "Failed to update order status.";
}

header("Location: ../views/editOrder.php?id=$orderId");
exit();
?>
