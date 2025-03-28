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

// Check if user is logged in
if (!isset($_SESSION['admin'])) {
    header("Location: login.php"); // Redirect to login page if not logged in
    exit();
}

$db = new firebaseRDB($databaseURL);

// Check if session contains admin data
if (isset($_SESSION['admin'])) {
    $adminName = $_SESSION['admin']['adminName'] ?? 'Admin'; // Default to "Admin" if name is missing
} else {
    $adminName = 'Guest'; // Default to "Guest" if not logged in
}

// Fetch total products
$products = $db->retrieve("product");
$products = json_decode($products, true);
$totalProducts = is_array($products) ? count($products) : 0;

// Fetch total categories
$categories = $db->retrieve("category");
$categories = json_decode($categories, true);
$totalCategories = is_array($categories) ? count($categories) : 0;

// Fetch total orders
$orders = $db->retrieve("order");
$orders = json_decode($orders, true);
$totalOrders = is_array($orders) ? count($orders) : 0;

// Fetch total users
$users = $db->retrieve("user");
$users = json_decode($users, true);
$totalUsers = is_array($users) ? count($users) : 0;

// Fetch total users
$admins = $db->retrieve("admin");
$admins = json_decode($admins, true);

// // Process sales data
// $salesData = [];
// if (is_array($orders)) {
//     foreach ($orders as $order) {
//         if (isset($order['orderTime'], $order['totalPrice'])) {
//             // Format orderTime to get the month (e.g., January, February)
//             $month = date('F', strtotime($order['orderTime']));
//             if (!isset($salesData[$month])) {
//                 $salesData[$month] = 0;
//             }
//             $salesData[$month] += $order['totalPrice']; // Aggregate totalPrice
//         }
//     }
// }

// // Convert to JSON for Chart.js
// $salesDataJson = json_encode($salesData);

// Initialize an empty array for sales data
$salesData = [];

// Get the current month and year
$currentMonth = date('Y-m');

// Loop through the past 12 months
for ($i = 11; $i >= 0; $i--) {
    // Get the month-year label (e.g., "2023-12" for December 2023)
    $monthLabel = date('Y-m', strtotime("-$i month"));

    // Initialize sales data for the month
    $salesData[$monthLabel] = 0;
}

// Process orders to accumulate sales data
if (is_array($orders)) {
    foreach ($orders as $order) {
        if (isset($order['orderTime'], $order['totalPrice'])) {
            // Format order time to get the year-month (e.g., "2023-12")
            $orderMonth = date('Y-m', strtotime($order['orderTime']));
            
            // Check if the order is within the last 12 months
            if (array_key_exists($orderMonth, $salesData)) {
                // Add to the sales data for that month
                $salesData[$orderMonth] += $order['totalPrice'];
            }
        }
    }
}

// Convert to JSON for Chart.js
$salesDataJson = json_encode($salesData);

?>

<?php
// Initialize arrays for category sales count
$categorySalesCount = [];

// Fetch orders
$orders = $db->retrieve("order");
$orders = json_decode($orders, true);

// Fetch products
$products = $db->retrieve("product");
$products = json_decode($products, true);

// Fetch categories
$categories = $db->retrieve("category");
$categories = json_decode($categories, true);

// Create a mapping for product IDs to categories
$productCategoryMap = [];
if (is_array($products)) {
    foreach ($products as $product) {
        $productCategoryMap[$product['id']] = $product['productCategory'];
    }
}

// Create a mapping for category IDs to names
$categoryNameMap = [];
if (is_array($categories)) {
    foreach ($categories as $category) {
        $categoryNameMap[$category['categoryName']] = $category['categoryName'];
    }
}

// Process orders to count sales per category
if (is_array($orders)) {
    foreach ($orders as $order) {
        if (isset($order['items'])) {
            foreach ($order['items'] as $item) {
                $productId = $item['productId'];
                $categoryId = $productCategoryMap[$productId] ?? null;

                if ($categoryId) {
                    $categoryName = $categoryNameMap[$categoryId] ?? 'Unknown';
                    if (!isset($categorySalesCount[$categoryName])) {
                        $categorySalesCount[$categoryName] = 0;
                    }
                    $categorySalesCount[$categoryName]++;
                }
            }
        }
    }
}

// Calculate total orders
$totalOrders = array_sum($categorySalesCount);

// Calculate sales percentage
$categorySalesPercentage = [];
foreach ($categorySalesCount as $categoryName => $count) {
    $categorySalesPercentage[$categoryName] = round(($count / $totalOrders) * 100, 2);
}

// Convert to JSON for Chart.js
$categorySalesPercentageJson = json_encode($categorySalesPercentage);


// Fetch pending orders in "Paid" status
$paidOrders = [];
if (is_array($orders)) {
    $count = 0;
    foreach ($orders as $order) {
        $latestStatus = end($order['orderStatus']);
        if ($latestStatus['updateStatus'] === 'Paid') {
            $paidOrders[] = $order;
            $count++;
        }
        if ($count >= 15) {
            break; // Limit to 15 orders
        }
    }
}


?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://kit.fontawesome.com/1dd4ddab77.js" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        <?php include "../css/dashboard.css" ?>
    </style>
</head>
<body>
    <?php include 'navigationBar.php'; ?>

    <div class="content">
        <div class="content-box">
        <p class="bigtitle"><b>Welcome Back, 
            <?php
                if (isset($_SESSION['admin'])) {
                    echo htmlspecialchars($_SESSION['admin']['adminName']); 
                } else {
                    echo "Guest";
                }
            ?>
        </b></p>
        
            <!-- Dashboard Overview Section -->
            <div class="row mt-2">
                <!-- Metrics Section -->
                <div class="col-xs-12 col-sm-6 col-md-6 col-lg-3">
                    <a href="../views/product.php" class="navigate">
                        <div class="card metrics-card p-3 text-center">
                            <div class="iconBox">
                                <img src="../images/box.png" class="icon" />
                            </div>
                            <p class="formtitle">Total Products</p>
                            <p class="formData"><b><?php echo $totalProducts; ?></b></p>
                        </div>
                    </a>
                </div>
                <div class="col-xs-12 col-sm-6 col-md-6 col-lg-3">
                    <a href="../views/category.php" class="navigate">
                        <div class="card metrics-card p-3 text-center">
                            <div class="iconBox">
                                <img src="../images/categorization.png" class="icon" />
                            </div>
                            <p class="formtitle">Total Categories</p>
                            <p class="formData"><b><?php echo $totalCategories; ?></b></p>
                        </div>
                    </a>
                </div>
                <div class="col-xs-12 col-sm-6 col-md-6 col-lg-3">
                    <a href="../views/order.php" class="navigate">
                        <div class="card metrics-card p-3 text-center">
                            <div class="iconBox">
                                <img src="../images/shopping-list.png" class="icon" />
                            </div>
                            <p class="formtitle">Total Orders</p>
                            <p class="formData"><b><?php echo $totalOrders; ?></b></p>
                        </div>
                    </a>
                </div>
                <div class="col-xs-12 col-sm-6 col-md-6 col-lg-3">
                    <a href="../views/customer.php" class="navigate">
                        <div class="card metrics-card p-3 text-center">
                            <div class="iconBox">
                                <img src="../images/customer.png" class="icon" />
                            </div>
                            <p class="formtitle">Total Customers</p>
                            <p class="formData"><b><?php echo $totalUsers; ?></b></p>
                        </div>
                    </a>
                </div>
            </div>

            <!-- Sales Chart Section -->
            <div class="row mt-3">
                <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                    <div class="salesOverviewChart">
                        <p class="chartTitle"><b>Sales Overview (12 Months)</b></p>
                        <canvas id="salesChart"></canvas>
                    </div>
                </div>
                <div class="col-xs-8 col-sm-8 col-4 col-lg-4">
                    <div class="salesOverviewChart">
                        <p class="chartTitle"><b>Category Sales</b></p>
                        <canvas id="categorySalesChart"></canvas>
                    </div>
                </div>
            </div>

            <div class="row mt-3">
                <div class="col-12">
                    <div class="pending-orders-section">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h3 class="chartTitle"><i class="fas fa-clock me-2"></i>Paid Orders</h3>
                            <a href="order.php" class="btn btn-primary btn-sm" style="background-color: #5B68C5;">View More Orders</a>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>Order ID</th>
                                        <th>Customer ID</th>
                                        <th>Total Price</th>
                                        <th>Order Time</th>
                                        <th>Latest Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <?php if (!empty($paidOrders)) : ?>
                                        <?php foreach ($paidOrders as $order) : ?>
                                            <tr>

                                                <td>
                                                    <a style="text-decoration-line: none;" href="viewOrder.php?id=<?php echo urlencode($order['id']); ?>" class="order-link">
                                                        <?php echo htmlspecialchars($order['id']); ?>
                                                    </a>
                                                </td>
                                                <td><?php echo htmlspecialchars($order['userId']); ?></td>
                                                <td>RM <?php echo htmlspecialchars($order['totalPrice']); ?></td>
                                                <td><?php echo htmlspecialchars($order['orderTime']); ?></td>
                                                <td>
                                                    <span class="badge 
                                                        <?php echo htmlspecialchars(end($order['orderStatus'])['updateStatus']) === 'Paid' ? 'bg-success' : ''; ?>">
                                                        <?php echo htmlspecialchars(end($order['orderStatus'])['updateStatus']); ?>
                                                    </span>
                                                </td>
                                            </tr>
                                        <?php endforeach; ?>
                                    <?php else : ?>
                                        <tr>
                                            <td colspan="5" class="text-center text-muted">No paid orders</td>
                                        </tr>
                                    <?php endif; ?>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>


        </div>
    </div>
</body>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  // Fetch sales data from PHP
const salesData = <?php echo $salesDataJson; ?>;

// Generate chart data
const labels = Object.keys(salesData); // Month-Year labels (e.g., "2023-12")
const data = Object.values(salesData); // Sales amounts for each month

// Initialize Chart.js
const ctx = document.getElementById('salesChart').getContext('2d');
const salesChart = new Chart(ctx, {
    type: 'bar', // You can change this to 'line' or another chart type
    data: {
        labels: labels, // Month-Year labels
        datasets: [{
            label: 'Sales Overview (by Month)',
            data: data, // Sales data
            backgroundColor: '#5B68C5',
            borderColor: '#3e95cd',
            borderWidth: 1
        }]
    },
    options: {
        responsive: true,
        plugins: {
            legend: {
                position: 'top'
            },
            tooltip: {
                callbacks: {
                    label: function(tooltipItem) {
                        return 'RM ' + tooltipItem.raw.toFixed(2); // Format sales value
                    }
                }
            }
        },
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Month-Year'
                }
            },
            y: {
                title: {
                    display: true,
                    text: 'Sales (RM)'
                },
                ticks: {
                    beginAtZero: true
                }
            }
        }
    }
});

</script>

<script>
    const categorySalesData = <?php echo $categorySalesPercentageJson; ?>;

    const categoryLabels = Object.keys(categorySalesData);
    const categoryPercentages = Object.values(categorySalesData);

    const categorySalesCtx = document.getElementById('categorySalesChart').getContext('2d');
    const categorySalesChart = new Chart(categorySalesCtx, {
        type: 'pie',
        data: {
            labels: categoryLabels,
            datasets: [{
                label: 'Category Sales (%)',
                data: categoryPercentages,
                backgroundColor: [
                    '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'
                ]
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                }
            }
        }
    });
</script>
</html>
