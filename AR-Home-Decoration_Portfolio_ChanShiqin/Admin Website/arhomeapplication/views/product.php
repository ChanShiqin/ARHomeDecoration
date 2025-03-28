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

// Retrieve products from Firebase
$data = $db->retrieve("product");
$data = json_decode($data, 1) ?? [];

$totalProducts = ($data) ? count($data) : 0;

// Pagination logic
$itemsPerPage = isset($_GET['itemsPerPage']) ? (int)$_GET['itemsPerPage'] : 10;
$itemsPerPage = max(1, $itemsPerPage); // Ensure at least 1 item per page

$totalProducts = ($data) ? count($data) : 0;
$totalPages = ceil($totalProducts / $itemsPerPage);
$page = isset($_GET['page']) ? (int)$_GET['page'] : 1;
$page = max(1, min($page, $totalPages)); // Ensure the page is within bounds
$startIndex = ($page - 1) * $itemsPerPage;

// Collect unique productCategory and productSupplier values
$categories = [];
$suppliers = [];
if (!empty($data)) {
    foreach ($data as $product) {
        if (isset($product['productCategory']) && !in_array($product['productCategory'], $categories)) {
            $categories[] = $product['productCategory'];
        }
        if (isset($product['productSupplier']) && !in_array($product['productSupplier'], $suppliers)) {
            $suppliers[] = $product['productSupplier'];
        }
    }
}

// Apply category filter
$categoryFilter = isset($_GET['categoryFilter']) ? strtolower(trim($_GET['categoryFilter'])) : 'all';
if ($categoryFilter !== 'all') {
    $data = array_filter($data, function ($product) use ($categoryFilter) {
        return strtolower($product['productCategory']) === $categoryFilter;
    });
}

// Apply supplier filter
$supplierFilter = isset($_GET['supplierFilter']) ? strtolower(trim($_GET['supplierFilter'])) : 'all';
if ($supplierFilter !== 'all') {
    $data = array_filter($data, function ($product) use ($supplierFilter) {
        return strtolower($product['productSupplier']) === $supplierFilter;
    });
}

// $totalProducts = count($data);
$totalProducts = ($data && is_array($data)) ? count($data) : 0;
$totalPages = ceil($totalProducts / $itemsPerPage);
$startIndex = ($page - 1) * $itemsPerPage;
$productsToShow = array_slice($data, $startIndex, $itemsPerPage);

?>

<!DOCTYPE html>
<html lang="en">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>Product</title>
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
                    <a href="addProduct.php" class="btn add-button" id="add-button">+ Add New</a>
                </div>
                <div class="col-xs-8 col-sm-6 col-md-3 col-lg-10 filter-container" >
                    <button class="btn dropdown-toggle filter-button" style="margin-left: 10px;" type="button" id="filterDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                    Supplier
                    </button>
                    <ul class="dropdown-menu" aria-labelledby="filterDropdown">
                        <li class="dropdown-li"><a class="dropdown-item" href="#" onclick="filterTable('All')">All</a></li>

                        <?php foreach ($suppliers as $supplier): ?>
                            <li><a class="dropdown-item" href="#" onclick="filterTable('<?php echo htmlspecialchars($supplier); ?>')"><?php echo htmlspecialchars($supplier); ?></a></li>
                        <?php endforeach; ?>
                    </ul>

                    <button class="btn dropdown-toggle filter-button" style="margin-left: 10px;" type="button" id="filterDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                    Category
                    </button>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="?categoryFilter=all">All</a></li>

                        <?php foreach ($categories as $category): ?>
                            <li><a class="dropdown-item" href="?categoryFilter=<?php echo urlencode($category); ?>"><?php echo htmlspecialchars($category); ?></a></li>
                        <?php endforeach; ?>
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
                <p class="categories"><b><?php echo"$totalProducts" ?> Products</b></p>
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
                        Total: &nbsp;<b> <?php echo"$totalProducts" ?></b>&nbsp; and showing 
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
                            </th> 
                                                                    <td><input type='checkbox'></td>
-->
                            <th scope="col" style="width: 10%;">Product Id</th>
                            <th scope="col" style="width: 25%;">Name</th>
                            <th scope="col" style="width: 15%;">Images</th>
                            <th scope="col" style="width: 20%;">Category</th>
                            <th scope="col" style="width: 20%;">Supplier</th>
                            <th scope="col" style="width: 10%;">Actions</th>
                        </tr>
                    </thead>

                    <tbody>
                        <?php
                            if (!empty($productsToShow)) {
                                foreach ($productsToShow as $id => $product) {
                                    echo "
                                    <tr>
                                        <td><b>{$product['id']}</b></td>
                                        <td><b>{$product['productName']}</b></td>
                                        <td>
                                            <img src='data:image/png;base64,{$product['productImages'][0]}' class='icons'>
                                        </td>
                                        <td>{$product['productCategory']}</td>
                                        <td>{$product['productSupplier']}</td>
                                        <td>
                                            <a href='viewProduct.php?id=$id' style='text-decoration: none;'>
                                                <img src='../images/view_icon.png' alt='Edit Icon' class='editicon'>
                                            </a>
                                            &nbsp;
                                            <a href='editProduct.php?id=$id' style='text-decoration: none;'>
                                                <img src='../images/edit_icon.png' alt='Edit Icon' class='editicon'>
                                            </a>
                                            &nbsp;
                                            <a href='deleteProduct.php?id=$id' class='delete-link' onclick='return confirm(\"Are you sure you want to delete this product ?\");'>
                                                <img src='../images/delete_icon.png' alt='Delete Icon' class='editicon'>
                                            </a>
                                        </td>
                                    </tr>";
                                }
                            }
                            else {
                                echo "<tr><td colspan='6' style='text-align: center; vertical-align: middle;'>No products found.</td></tr>";
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
        function filterTable(status) {
            var table, rows, i, statusText;
            table = document.querySelector("table.itemtable tbody");
            rows = table.getElementsByTagName("tr");

            for (i = 0; i < rows.length; i++) {
                // Get the status column in each row
                statusText = rows[i].getElementsByClassName("status-text")[0].textContent;
                
                // Show/Hide rows based on filter selection
                if (status === "All" || statusText === status) {
                    rows[i].style.display = "";
                } else {
                    rows[i].style.display = "none";
                }
            }
        }
    </script>

    <script>
        function filterTable(supplier) {
            const urlParams = new URLSearchParams(window.location.search);
            urlParams.set('supplierFilter', supplier.toLowerCase());
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

</body>
</html>