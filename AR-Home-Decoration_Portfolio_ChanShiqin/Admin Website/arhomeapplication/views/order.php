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

$statusFilter = isset($_GET['filterStatus']) ? $_GET['filterStatus'] : 'All';

// Retrieve categories from Firebase
$data = $db->retrieve("order");
$data = json_decode($data, true);

if ($data) {
    // Sort data by orderTime in descending order
    usort($data, function ($a, $b) {
        return strtotime($b['orderTime']) - strtotime($a['orderTime']);
    });

    // Filter orders by status if a specific status is selected
    if ($statusFilter !== 'All') {
        $filteredData = array_filter($data, function ($order) use ($statusFilter) {
            if (isset($order['orderStatus'])) {
                $latestStatus = "Unknown";
                $latestTime = "1970-01-01 00:00:00";

                foreach ($order['orderStatus'] as $status) {
                    if (strtotime($status['updateTime']) > strtotime($latestTime)) {
                        $latestStatus = $status['updateStatus'];
                        $latestTime = $status['updateTime'];
                    }
                }

                return $latestStatus === $statusFilter;
            }
            return false;
        });
    } else {
        $filteredData = $data;
    }

    // Pagination logic
    $totalOrders = count($filteredData);
    $itemsPerPage = isset($_GET['itemsPerPage']) ? (int)$_GET['itemsPerPage'] : 10;
    $itemsPerPage = max(1, $itemsPerPage);
    $totalPages = ceil($totalOrders / $itemsPerPage);
    $page = isset($_GET['page']) ? (int)$_GET['page'] : 1;
    $page = max(1, min($page, $totalPages));
    $startIndex = ($page - 1) * $itemsPerPage;

    $ordersToShow = array_slice($filteredData, $startIndex, $itemsPerPage);
} else {
    $ordersToShow = []; // No data found
}

?>

<!DOCTYPE html>
<html lang="en">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Order</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://kit.fontawesome.com/1dd4ddab77.js" crossorigin="anonymous"></script>
    <style>
        <?php include "../css/category.css" ?>
    </style>
</head>
<body>
    <?php include 'navigationBar.php'; ?>
    <div class="content">
        <div class="content-box">
            <div class="row filterandaddbutton">
                <div class="col-xs-4 col-sm-6 col-md-9 col-lg-2">
                    <!-- <a href="addCategory.php" class="btn add-button" id="add-button">+ Add New</a> -->
                </div>
                <div class="col-xs-8 col-sm-6 col-md-3 col-lg-10 filter-container" >
                    <button class="btn dropdown-toggle filter-button" type="button" id="filterDropdown" data-bs-toggle="dropdown" aria-expanded="false" style="width: 150px;">
                    Order Status
                    </button>
                    <ul class="dropdown-menu" id="dropdown-order-menu" aria-labelledby="filterDropdown">
                    <li class="dropdown-li"><a class="dropdown-item" href="#" onclick="filterTable('All')">All</a></li>
                    <li class="dropdown-li"><a class="dropdown-item" href="#" onclick="filterTable('Paid')">Paid</a></li>
                    <li class="dropdown-li"><a class="dropdown-item" href="#" onclick="filterTable('Processing')">Processing</a></li>
                    <li class="dropdown-li"><a class="dropdown-item" href="#" onclick="filterTable('Shipped')">Shipped</a></li>
                    <li class="dropdown-li"><a class="dropdown-item" href="#" onclick="filterTable('Out for Delivery')">Out for Delivery</a></li>
                    <li class="dropdown-li"><a class="dropdown-item" href="#" onclick="filterTable('Delivered')">Delivered</a></li>
                    </ul>
                </div>
            </div>

            <div class="row">
                <?php if (isset($_SESSION['success'])): ?>
                <div id="successAlert" class="col-12 alert alert-success alert-dismissible fade show" role="alert">
                    <?php echo htmlspecialchars($_SESSION['success']); ?>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <?php unset($_SESSION['success']); // Clear the session message ?>
                <?php endif; ?>
            </div>

            <div class="row greybar">
                <div class="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                <p class="categories"><b><?php echo"$totalOrders" ?> Orders</b></p>
                </div>
                <div class="col-xs-12 col-sm-6 col-md-3 col-lg-3">
                <div class="searchbox">
                    <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-search"></i></span>
                    <input type="text" class="form-control" id="searchBox" placeholder="Search">
                    </div>
                </div>
                </div>
                <div class="col-xs-12 col-sm-6 col-md-4 col-lg-4">
                <div class="perpage">
                    <div>
                    <p class="categories">
                        Total: &nbsp;<b> <?php echo"$totalOrders" ?></b>&nbsp; and showing 
                        <input type="number" id="itemsPerPage" value="<?php echo $itemsPerPage; ?>" min="1" class="form-control">
                        per page
                    </p>
                    </div>
                </div>
                </div>
            </div>

            <div class="row" style="margin-top: 10px;">
                <table class="itemtable" style="padding: 0;">
                    <thead>
                        <tr>
                        <!-- <th scope="col" style="width: 5%;">
                            <input type="checkbox" id="selectAll">
                        </th> -->
                        <!-- <td><input type='checkbox'></td> -->

                        <th scope="col" style="width: 10%;">Order ID</th>
                        <th scope="col" style="width: 15%;">Customer</th>
                        <th scope="col" style="width: 15%;">Order Date</th>
                        <th scope="col" style="width: 15%;">Order Total</th>
                        <th scope="col" style="width: 10%;">Order Status</th>
                        <th scope="col" style="width: 10%;">Actions</th>
                    </thead>

                    <tbody>
                        <?php
                            if (!empty($ordersToShow)) {
                                foreach ($ordersToShow as $id => $order) {

                                    // For each order, we want to determine the latest status
                                    if (!empty($order['orderStatus'])) {
                                        // Initialize variables to hold the latest status and time
                                        $latestStatus = "Unknown";
                                        $latestTime = "1970-01-01 00:00:00";  // Start with a very early date for comparison

                                        // Loop through each status update
                                        foreach ($order['orderStatus'] as $status) {
                                            // Compare the current updateTime with the latestTime
                                            if (strtotime($status['updateTime']) > strtotime($latestTime)) {
                                                // If the current status's updateTime is newer, update the latest status and time
                                                $latestStatus = $status['updateStatus'];
                                                $latestTime = $status['updateTime'];
                                            }
                                        }
                                    } else {
                                        // If no order status exists, set it as unknown
                                        $latestStatus = "Unknown";
                                    }

                                    echo "
                                    <tr>
                                        <td><b>{$order['id']}</b></td>
                                        <td>{$order['userId']}</td>
                                        <td>{$order['orderTime']}</td>
                                        <td>RM " . number_format($order['totalPrice'], 2) . "</td>
                                        <td><b>{$latestStatus}</b></td>";
                                        
                                        $orderid = $order['id'];

                                    echo "
                                        </td>
                                        <td>
                                            <a href='viewOrder.php?id=$orderid' style='text-decoration: none;'>
                                                <img src='../images/view_icon.png' alt='View Icon' class='editicon'>
                                            </a>
                                            &nbsp;
                                            <a href='editOrder.php?id=$orderid' style='text-decoration: none;'>
                                                <img src='../images/edit_icon.png' alt='Edit Icon' class='editicon'>
                                            </a>
                                            &nbsp;
                                           
                                        </td>
                                    </tr>";
                                }
                            }
                            else{
                                echo "<tr><td colspan='6' style='text-align: center; vertical-align: middle;'>No categories found.</td></tr>";
                            }
                        ?>
                    </tbody>
                </table>
            </div>

            <div class="pagination-container">
                <nav aria-label="Page navigation example">
                    <ul class="pagination">
                        <?php if ($page > 1): ?>
                            <li class="page-item"><a class="page-link" href="?page=<?php echo $page - 1; ?>&itemsPerPage=<?php echo $itemsPerPage; ?>">&laquo;</a></li>
                        <?php endif; ?>

                        <?php
                        $visiblePages = 5; // Number of visible page buttons
                        $startPage = max(1, $page - floor($visiblePages / 2));
                        $endPage = min($totalPages, $startPage + $visiblePages - 1);
                        if ($endPage - $startPage + 1 < $visiblePages) {
                            $startPage = max(1, $endPage - $visiblePages + 1);
                        }

                        // Show "..." before the first visible page
                        if ($startPage > 1) {
                            echo '<li class="page-item"><a class="page-link">...</a></li>';
                        }

                        // Display the page numbers with itemsPerPage
                        for ($i = $startPage; $i <= $endPage; $i++) {
                            echo '<li class="page-item ' . ($i == $page ? 'active' : '') . '"><a class="page-link" href="?page=' . $i . '&itemsPerPage=' . $itemsPerPage . '">' . $i . '</a></li>';
                        }

                        // Show "..." after the last visible page
                        if ($endPage < $totalPages) {
                            echo '<li class="page-item"><a class="page-link">...</a></li>';
                        }
                        ?>

                        <?php if ($page < $totalPages): ?>
                            <li class="page-item"><a class="page-link" href="?page=<?php echo $page + 1; ?>&itemsPerPage=<?php echo $itemsPerPage; ?>">&raquo;</a></li>
                        <?php endif; ?>
                    </ul>
                </nav>
            </div>


        </div>
    </div>
</body>

<script>
    document.getElementById('selectAll').addEventListener('change', function() {
        var checkboxes = document.querySelectorAll('tbody input[type="checkbox"]');
        for (var checkbox of checkboxes) {
            checkbox.checked = this.checked;
        }
    });
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<script>
    document.addEventListener('DOMContentLoaded', function () {
    var successAlert = document.getElementById('successAlert');
    var errorAlert = document.getElementById('errorAlert');

    if (successAlert) {
        setTimeout(function () {
        var bsAlert = new bootstrap.Alert(successAlert);
        bsAlert.close();
        }, 5000); // 5000 milliseconds = 5 seconds
    }

    if (errorAlert) {
        setTimeout(function () {
        var bsAlert = new bootstrap.Alert(errorAlert);
        bsAlert.close();
        }, 5000); // 5000 milliseconds = 5 seconds
    }
    });
</script>

<script>
    document.getElementById('searchBox').addEventListener('input', function() {
      var query = this.value.toLowerCase(); // Get the search query in lowercase
      var rows = document.querySelectorAll('tbody tr'); // Get all table rows

      rows.forEach(function(row) {
          var categoryName = row.querySelector('td:nth-child(2) b').textContent.toLowerCase(); // Get the category name in lowercase
          
          // Check if the category name matches the search query
          if (categoryName.includes(query)) {
              row.style.display = ''; // Show the row if it matches
          } else {
              row.style.display = 'none'; // Hide the row if it doesn't match
          }
      });
    });
</script>

<script>
    // function filterTable(status) {
    //     var table, rows, i, statusText;
    //     table = document.querySelector("table.itemtable tbody");
    //     rows = table.getElementsByTagName("tr");

    //     for (i = 0; i < rows.length; i++) {
    //         // Get the status column in each row
    //         statusText = rows[i].getElementsByClassName("status-text")[0].textContent;
            
    //         // Show/Hide rows based on filter selection
    //         if (status === "All" || statusText === status) {
    //             rows[i].style.display = "";
    //         } else {
    //             rows[i].style.display = "none";
    //         }
    //     }
    // }

    function filterTable(status) {
        const urlParams = new URLSearchParams(window.location.search);
        urlParams.set('filterStatus', status);
        urlParams.set('page', 1); // Reset to first page when filtering
        window.location.search = urlParams.toString();
    }

</script>

  <script>
    document.getElementById('itemsPerPage').addEventListener('change', function () {
        var itemsPerPage = this.value;
        var currentPage = <?php echo $page; ?>;
        
        // Redirect to the first page with the new itemsPerPage value
        window.location.href = '?page=' + currentPage + '&itemsPerPage=' + itemsPerPage;
    });
  </script>

</html>

<!-- <a href='deleteOrder.php?id=$orderid' class='delete-link' onclick='return confirm(\"Are you sure you want to delete this category ?\");'>
                                                <img src='../images/delete_icon.png' alt='Delete Icon' class='editicon'>
                                            </a> -->