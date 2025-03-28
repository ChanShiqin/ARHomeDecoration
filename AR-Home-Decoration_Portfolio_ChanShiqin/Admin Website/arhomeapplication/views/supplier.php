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

// Retrieve suppliers from Firebase
$data = $db->retrieve("supplier");
$data = json_decode($data, 1);

$totalSupplier = ($data) ? count($data) : 0;

// Pagination logic
$itemsPerPage = isset($_GET['itemsPerPage']) ? (int)$_GET['itemsPerPage'] : 10;
$itemsPerPage = max(1, $itemsPerPage); // Ensure at least 1 item per page

$totalPages = ceil($totalSupplier / $itemsPerPage);
$page = isset($_GET['page']) ? (int)$_GET['page'] : 1;
$page = max(1, min($page, $totalPages)); // Ensure the page is within bounds
$startIndex = ($page - 1) * $itemsPerPage;

// Slice the data to display only the current page's suppliers
$suppliersToShow = ($data) ? array_slice($data, $startIndex, $itemsPerPage) : [];
?>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Suppliers</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
<script src="https://kit.fontawesome.com/1dd4ddab77.js" crossorigin="anonymous"></script>
<style>
    <?php include "../css/category.css" ?> /* Ensure you update this for suppliers */
</style>
</head>

<body>
    <?php include 'navigationBar.php'; ?>
    <div class="content">
        <div class="content-box">
            <div class="row filterandaddbutton">
                <div class="col-xs-4 col-sm-6 col-md-9 col-lg-2">
                    <a href="addSupplier.php" class="btn add-button" id="add-button">+ Add New</a>
                </div>
            </div>        

            <div class="row">

                <?php if (isset($_SESSION['error'])): ?>
                    <div id="errorAlert" class="col-12 alert alert-danger alert-dismissible fade show" role="alert">
                        <?php echo htmlspecialchars($_SESSION['error']); ?>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    <?php unset($_SESSION['error']); // Clear the session message ?>
                <?php endif; ?>

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
                <p class="categories"><b><?php echo"$totalSupplier" ?> Suppliers</b></p>
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
                                Total: &nbsp;<b><?php echo"$totalSupplier" ?></b>&nbsp; and showing 
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
                            <th scope="col" style="width: 15%;">Company Name</th>
                            <th scope="col" style="width: 15%;">Company Phone</th>
                            <th scope="col" style="width: 15%;">Company Email</th>
                            <th scope="col" style="width: 15%;">PIC Name</th>
                            <th scope="col" style="width: 10%;">PIC Phone</th>
                            <th scope="col" style="width: 15%;">PIC Email</th>
                            <th scope="col" style="width: 10%;">Actions</th>
                        </tr>
                    </thead>

                    <!-- <td><input type='checkbox'></td> -->


                    <tbody>
                        <?php
                        if (!empty($suppliersToShow)) {
                            foreach ($suppliersToShow as $id => $supplier) {
                                echo "
                                <tr>
                                    <td><b>{$supplier['supplierCompanyName']}</b></td>
                                    <td>{$supplier['supplierCompanyPhoneNo']}</td>
                                    <td>{$supplier['supplierCompanyEmail']}</td>
                                    <td>{$supplier['supplierPICName']}</td>
                                    <td>{$supplier['supplierPICPhoneNo']}</td>
                                    <td>{$supplier['supplierPICEmail']}</td>
                                    <td>
                                        <a href='editSupplier.php?id=$id' style='text-decoration: none;'>
                                            <img src='../images/edit_icon.png' alt='Edit Icon' class='editicon'>
                                        </a>
                                        &nbsp;
                                        <a href='deleteSupplier.php?id=$id' class='delete-link' onclick='return confirm(\"Are you sure you want to delete this supplier?\");'>
                                            <img src='../images/delete_icon.png' alt='Delete Icon' class='editicon'>
                                        </a>
                                    </td>
                                </tr>";
                            }
                        } else {
                            echo "<tr><td colspan='7' style='text-align: center; vertical-align: middle;'>No suppliers found.</td></tr>";
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
                        $visiblePages = 5;
                        $startPage = max(1, $page - floor($visiblePages / 2));
                        $endPage = min($totalPages, $startPage + $visiblePages - 1);
                        if ($endPage - $startPage + 1 < $visiblePages) {
                            $startPage = max(1, $endPage - $visiblePages + 1);
                        }

                        if ($startPage > 1) {
                            echo '<li class="page-item"><a class="page-link">...</a></li>';
                        }

                        for ($i = $startPage; $i <= $endPage; $i++) {
                            echo '<li class="page-item ' . ($i == $page ? 'active' : '') . '"><a class="page-link" href="?page=' . $i . '&itemsPerPage=' . $itemsPerPage . '">' . $i . '</a></li>';
                        }

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

    if (successAlert) {
        setTimeout(function () {
            var bsAlert = new bootstrap.Alert(successAlert);
            bsAlert.close();
        }, 5000); // 5000 milliseconds = 5 seconds
    }
});
</script>

<script>
document.getElementById('searchBox').addEventListener('input', function() {
    var query = this.value.toLowerCase(); // Get the search query in lowercase
    var rows = document.querySelectorAll('tbody tr'); // Get all rows in the table

    rows.forEach(function(row) {
        var companyName = row.querySelector('td:nth-child(2)').textContent.toLowerCase(); // Get the company name from the second column
        if (companyName.includes(query)) {
            row.style.display = ''; // Show the row if the query matches
        } else {
            row.style.display = 'none'; // Hide the row if the query doesn't match
        }
    });
});

document.getElementById('itemsPerPage').addEventListener('change', function () {
    var newItemsPerPage = this.value;
    window.location.href = "?page=1&itemsPerPage=" + newItemsPerPage;
});
</script>

</html>
