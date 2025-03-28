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
$id = $_GET['id'];

$retrieve = $db->retrieve("order/$id");

$orderData = json_decode($retrieve, 1);

// Check if the category exists
if (!$orderData) {
    $_SESSION['error'] = "Order not found.";
    header("Location: order.php");
    exit();
}

// Retrieve the user data
$userId = $orderData['userId'];
$userList = json_decode($db->retrieve("user"), true);

$userData = null;
foreach ($userList as $user) {
    if ($user['id'] === $userId) {
        $userData = $user;
        break;
    }
}

// Handle case where user is not found
if (!$userData) {
    $_SESSION['error'] = "User not found.";
    header("Location: order.php");
    exit();
}


// Calculate total items
$totalItems = 0;
foreach ($orderData['items'] as $item) {
    $totalItems += $item['productQuantity'];
}

// Format order date
$orderDate = date("l, F d, Y", strtotime($orderData['orderTime']));

// Total price
$totalPrice = $orderData['totalPrice'];
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Update Order</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <script src="https://kit.fontawesome.com/1dd4ddab77.js" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.6.0/css/all.min.css" integrity="sha512-Kc323vGBEqzTmouAECnVceyQqyqdsSiqLQISBL29aUW4U/M7pSPA/gEUZQqv1cwx4OnYxTxve5UMg5GT6L4JJg==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <script src="https://code.jquery.com/jquery-3.7.1.min.js" integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>

    <style>
        <?php include "../css/viewOrder.css" ?>
    </style>
</head>
<body>
    <?php include 'navigationBar.php'; ?>
    <div class="content">
    <div class="content-box">
        <!-- Back button -->
        <div class="row filterandaddbutton">
            <div class="col-lg-2">
                <a href="order.php" class="btn add-button" id="add-button">< &nbsp; Back</a>
            </div>
        </div>

        <!-- Container with left and right sections -->
        <div class="row">
            <!-- Left Section -->
            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8 part-container" style="margin-top: 15px;">
                <div class="inner-content">
                    <div class="row margin-setup">
                        <div class="col-12">
                            <p class="bigtitle"><b>Update Order Details</b></p>
                            <p class="formtitle"><b>Order - O00001</b></p>
                            <hr class="line">
                            <div class="row">
                                <div class="col-7">
                                    <p class="billTitle"><b>BILL TO</b></p>
                                    <p class="billName"><b><?= $userData['name'] ?></b></p>
                                    <p class="billAddress"><?= $userData['address'] ?>, <br/> <?= $userData['poscode'] ?>, <?= $userData['state'] ?></p>

                                    <div class="space"></div>

                                   <p class="billTitle"><b>SHIP TO</b>
                                        <img src="../images/edit_icon_black.png" alt="Edit" id="editShipToIcon" style="cursor: pointer; margin-left: 10px; height: 15px; width: 15px;" />
                                    </p>
                                    <div id="shipToView">
                                        <p class="billName"><b><?= $userData['name'] ?></b></p>
                                        <p class="billAddress"><?= $userData['address'] ?>, <br/> <?= $userData['poscode'] ?>, <?= $userData['state'] ?></p>
                                    </div>
                                    <div id="shipToEdit" style="display: none;">
                                        <input type="text" style="font-decoration: bold;" class="form-control mb-2" id="shipToName" value="<?= $userData['name'] ?>" placeholder="Name">
                                        <textarea class="form-control mb-2" id="shipToAddress" placeholder="Address"><?= $userData['address'] ?></textarea>
                                        <input type="text" class="form-control mb-2" id="shipToPoscode" value="<?= $userData['poscode'] ?>" placeholder="Poscode">
                                        <input type="text" class="form-control mb-2" id="shipToState" value="<?= $userData['state'] ?>" placeholder="State">
                                        <button class="btn btn-primary btn-sm" id="saveShipTo">Save</button>
                                        <button class="btn btn-secondary btn-sm" id="cancelShipTo">Cancel</button>
                                    </div>
                                </div>
                                <div class="col-5">
                                    <p class="billTitle"><b>ORDER DATE</b></p>
                                    <p class="billAddress"><?= $orderDate ?></p>

                                    <div class="space"></div>

                                    <p class="billTitle"><b>TOTAL ORDER</b></p>
                                    <p class="billAddress"><?= $totalItems ?> units </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div style="height: 20px;"></div>

                    <div class="row" style="margin-left: 0px; margin-right: 0px;">
                        <div class="col-12">
                            <div class="row tableTitle">
                                <div class="col-6">
                                    <p class="tableTitleWord"><b>Items Order</b></p>
                                </div>
                                <div class="col-2">
                                    <p class="tableTitleWord"><b>Price</b></p>
                                </div>
                                <div class="col-2">
                                    <p class="tableTitleWord"><b>Quantity</b></p>
                                </div>
                                <div class="col-2">
                                    <p class="tableTitleWord"><b>Total</b></p>
                                </div>
                            </div>

                            <?php foreach ($orderData['items'] as $item): 

                                $productId = $item['productId'];
                                $productList = json_decode($db->retrieve("product"), true);

                                $productData = null;
                                foreach ($productList as $product) {
                                    if ($product['id'] === $productId) {
                                        $productData = $product;
                                        break;
                                    }
                                }

                                // Extract necessary details
                                $productName = $productData['productName'] ?? 'Unknown Product';
                                $productImages = $productData['productImages'] ?? [];
                                $productImage = !empty($productImages) ? $productImages[0] : 'product1.jpeg';
                                $productDesign = $item['productDesign'] ?? '-';
                                $productPrice = $item['productPrice'] ?? 0;
                                $productQuantity = $item['productQuantity'] ?? 1;
                                $productTotalPrice = $productPrice * $productQuantity;
                            ?>

                            <div class="row margin-items">
                                <div class="col-6">
                                    <div class="row">
                                        <div class="col-3">
                                            <img class="productImage" src="data:image/png;base64,<?= htmlspecialchars($productImage) ?>" alt="<?= htmlspecialchars($productName) ?>" />
                                        </div>
                                        <div class="col-9">
                                            <div class="row">
                                                <div class="col-12 ">
                                                    <p class="tableTitleWord">
                                                        <?= htmlspecialchars($productName) ?> - <?= htmlspecialchars($productDesign) ?>
                                                    </p>
                                                </div>

                                                <div class="col-12"><p class="tableTitleWord">#<?= htmlspecialchars($productId) ?></p></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-2">
                                    <p class="tableTitleWord">RM <?= number_format($productPrice, 2) ?></p>
                                </div>
                                <div class="col-2">
                                    <p class="tableTitleWord"><?= htmlspecialchars($productQuantity) ?> units</p>
                                </div>
                                <div class="col-2">
                                    <p class="tableTitleWord">RM <?= number_format($productTotalPrice, 2) ?></p>
                                </div>
                            </div>

                            <hr class="itemLine">

                            <?php endforeach; ?>

                            <div class="row margin-items">
                                <div class="col-7" style="margin: 0%; padding-bottom: 0; padding-top: 0; padding-right: 10px;">
                            
                                    <p class="tableTitleWord" style="margin-bottom:0; margin-top: 0; padding-top: 0; padding-bottom: 5px;"><b>Remarks</b></p>
                                    <p class="tableTitleWord"><?= $orderData['remark'] ?? '-' ?></p>
                                </div>
                                <div class="col-5">
                                    <div class="row">
                                        <div class="col-5">
                                            <p class="tableTitleWord"><b>Total Items</b></p>
                                        </div>
                                        <div class="col-7">
                                            <p class="tableTitleWord"><?= $totalItems ?> units</p>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-5">
                                            <p class="tableTitleWord"><b>Total Price</b></p>
                                        </div>
                                        <div class="col-7">
                                            <p class="tableTitleWord">RM <?= number_format($totalPrice, 2) ?></p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div style="height: 10px;"></div>

                        </div>
                    </div>
                   
                </div>
            </div>

            <!-- Right Section -->
            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-4 part-container" style="margin-bottom: 10px; margin-top: 15px; margin-left: 0;">
                <div class="inner-content">
                    <div class="row margin-setup">
                        <div class="col-12">
                            <p class="bigtitle"><b>Shipment</b></p>
                            <p class="formtitle"><b>Status & Notes</b></p>
                            <hr class="line">

                            <div class="row">
                                <div class="col-12 shipment-status">
                                    <?php 
                                    // Define all possible shipment steps
                                    $shipmentSteps = ["Paid", "Processing", "Shipped", "Out for Delivery", "Delivered"];

                                    // Fetch order status data from the database
                                    $orderStatuses = $orderData['orderStatus'];
                                    $completedStatuses = [];
                                    foreach ($orderStatuses as $status) {
                                        $completedStatuses[$status['updateStatus']] = $status['updateTime'];
                                    }

                                    foreach ($shipmentSteps as $step) {
                                        // Check if the current step is in completedStatuses
                                        $isCompleted = isset($completedStatuses[$step]);
                                        $statusClass = $isCompleted ? "completed" : "pending";
                                        $timestamp = $isCompleted ? $completedStatuses[$step] : null;

                                        echo '
                                        <div class="shipment-step">
                                            <div class="status-indicator ' . $statusClass . '"></div>
                                            <div class="status-details">
                                                <p class="status-title">' . $step . '</p>';
                                        if ($isCompleted) {
                                            echo '<p class="status-timestamp">' . htmlspecialchars($timestamp) . '</p>';
                                        }
                                        echo '
                                            </div>
                                        </div>';
                                    }
                                    ?>
                                </div>

                                <div class="row">
                                    <div class="col-12 text-center mt-4">
                                        <form action="../actions/action_updateOrderStatus.php" method="POST">
                                            <input type="hidden" name="orderId" value="<?= $id ?>">
                                            <button type="submit" id="updateStatusButton" class="btn btn-success">Update Status</button>
                                        </form>
                                    </div>
                                </div>


                                <div style="height: 20px;"></div>
                            </div>

                        </div>
                       
                    </div>

                </div>
            </div>
        </div>
    </div>
</div>

</body>

<script>
    $(document).ready(function () {
        // Toggle to edit mode
        $("#editShipToIcon").click(function () {
            $("#shipToView").hide();
            $("#shipToEdit").show();
        });

        // Save changes
        $("#saveShipTo").click(function () {
            const name = $("#shipToName").val();
            const address = $("#shipToAddress").val();
            const poscode = $("#shipToPoscode").val();
            const state = $("#shipToState").val();

            // Update the view mode with new values
            $("#shipToView .billName").text(name);
            $("#shipToView .billAddress").html(`${address}, <br/> ${poscode}, ${state}`);
            
            // Save changes to the database (AJAX call or form submission)
            // Example: 
            // $.post('save_ship_to.php', { name, address, poscode, state }, function(response) { ... });

            // Toggle back to view mode
            $("#shipToEdit").hide();
            $("#shipToView").show();
        });

        // Cancel editing
        $("#cancelShipTo").click(function () {
            $("#shipToEdit").hide();
            $("#shipToView").show();
        });
    });

</script>

</html>