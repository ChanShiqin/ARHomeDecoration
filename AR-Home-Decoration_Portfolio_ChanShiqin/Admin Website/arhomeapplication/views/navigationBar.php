<?php
session_start();
include_once("../config.php");
include_once("../firebaseRDB.php");

$db = new firebaseRDB($databaseURL);

    $adminId = $_SESSION['admin']['id'];
    
    $adminData = $db->retrieve("admin/$adminId");
    $adminData = json_decode($adminData, true);

    if ($adminData) {
        $_SESSION['admin'] = $adminData; // Store full admin data in session
    } 

//     if ($adminData) {
//         $_SESSION['admin'] = $adminData;
    
//         if (!isset($adminData['lastOrderCheck'])) {
//             $_SESSION['admin']['lastOrderCheck'] = '1970-01-01 00:00:00'; // Default to epoch time
//         } else {
//             $_SESSION['admin']['lastOrderCheck'] = $adminData['lastOrderCheck'];
//         }
//     }
    

//     // Retrieve all orders from Firebase
// $orderData = $db->retrieve("order");
// $orderData = json_decode($orderData, true);

// $newOrderCount = 0;

// if ($orderData) {
//     $lastOrderCheck = $_SESSION['admin']['lastOrderCheck'];
    
//     foreach ($orderData as $order) {
//         if (strtotime($order['orderTime']) > strtotime($lastOrderCheck)) {
//             $newOrderCount++;
//         }
//     }
// }

?>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Navigation Bar</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <script src="https://kit.fontawesome.com/1dd4ddab77.js" crossorigin="anonymous"></script>
    <style>
      <?php include "../css/navigationBar.css" ?>
    </style>
</head>
<body>
    <div class="navbar" id="sidebar">
        <div class="logo" id="logo">
            <a href="#" target="_top" id="logo-text">AuraDecor.</a>
        </div>

        <div class="sidenav">
            <a href="dashboard.php" target="_top"><i class="bi bi-columns-gap"></i> <span class="nav-text">Dashboard</span></a>
            <a href="category.php" target="_top"><i class="bi bi-diagram-3"></i> <span class="nav-text">Category</span></a>
            <a href="supplier.php" target="_top" class="suppliericon"><i class="fas fa-handshake"></i><span class="nav-text">Supplier</span></a>
            <a href="product.php" target="_top"><i class="bi bi-box-seam"></i> <span class="nav-text">Product</span></a>
            <!-- <a href="#" target="_top"><i class="bi bi-megaphone"></i> <span class="nav-text">Promotion</span></a> -->
            <a href="customer.php" target="_top"><i class="bi bi-person-square"></i> <span class="nav-text">Customer</span></a>
            <a href="order.php" target="_top"><i class="bi bi-card-checklist"></i> <span class="nav-text">Order</span></a>
            <a href="admin.php" target="_top"><i class="bi bi-person-gear"></i> <span class="nav-text">Admin</span></a>
        </div>

        <div class="settingbottom">
            <a href="../actions/action_logout.php" target="_top"><i class="bi bi-box-arrow-left"></i> <span class="nav-text">Logout</span></a>
        </div>
        <?php
            session_start();
            if (!isset($_SESSION['admin'])) {
                header("Location: login.php"); // Redirect to login page if not logged in
                exit();
            }
        ?>
    </div>

    <div class="maincontent">
        <div class="header">
            <i class="bi bi-list" id="toggle-btn"></i>
            <h3 id="page-title">Title</h3>
            <div class="header-right">
				<!-- <div class="notification-bell">
                    <i class="bi bi-bell"></i>
                </div>                -->
<!-- 
                <div class="notification-bell">
                    <i class="bi bi-bell"></i>
                    <?php if ($newOrderCount > 0): ?>
                        <span class="badge bg-danger"><?php echo $newOrderCount; ?></span>
                    <?php endif; ?>
                </div> -->

                <!-- <img src="../images/profile_pic.jpg" alt="Profile Picture" class="profile-pic"> -->

                <?php 
                    if (isset($_SESSION['admin']['adminProfilePic'])) {
                        $profilePic = $_SESSION['admin']['adminProfilePic'];
                        echo "<img src='data:image/png;base64,$profilePic' alt='Profile Picture' class='profile-pic'>";
                    } else {
                        echo "<img src='../images/default_profile_pic.jpg' alt='Default Profile Picture' class='profile-pic'>";
                    }
                ?>

                <!-- <span class="username">Username</span> -->
                <span class="username">
                    <?php 
                        if (isset($_SESSION['admin'])) {
                            echo $_SESSION['admin']['adminName'];
                        } else {
                            echo "Guest";
                        }
                    ?>
                </span>
            </div>
        </div>
        <div class="content">
	        <!-- Placeholder for page-specific content -->
	    </div>
    </div>

    <script>
        document.getElementById('toggle-btn').addEventListener('click', function() {
            let sidebar = document.getElementById('sidebar');
            let navText = document.querySelectorAll('.nav-text');
            let logoText = document.getElementById('logo-text');

            sidebar.classList.toggle('minimized');

            if (sidebar.classList.contains('minimized')) {
                navText.forEach(text => text.style.display = 'none');
                logoText.textContent = 'AD.';
            } else {
                navText.forEach(text => text.style.display = 'inline');
                logoText.textContent = 'AuraDecor.';
            }
        });

        // Function to set the title based on the current page
        function setTitle() {
            const path = window.location.pathname;
            const page = path.split("/").pop().split(".")[0]; // Get the page name without extension
            const pageTitleMap = {
                'dashboard': 'Dashboard',
                'category': 'Category',
                'addCategory': 'Add Category',
                'editCategory': 'Edit Category',
                'product': 'Product',
                'addProduct': 'Add Product',
                'editProduct': 'Edit Product',
                'viewProduct': 'View Product',
                'promotion': 'Promotion',
                'order': 'Order',
                'viewOrder': 'View Order',
                'editOrder': 'Edit Order',
                'supplier': 'Supplier',
                'addSupplier': 'Add Supplier',
                'editSupplier': 'Edit Supplier',
                'customer': 'Customer',
                'addCustomer': 'Add Customer',
                'editCustomer': 'Edit Customer',
                'admin': 'Admin',
                'addAdmin': 'Add Admin',
                'editAdmin': 'Edit Admin'
            };
            const titleElement = document.getElementById('page-title');
            titleElement.textContent = pageTitleMap[page] || 'Title';
        }

        // Set the initial title when the page loads
        setTitle();
    </script>
</body>
</html>