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
$retrieve = $db->retrieve("product/$id");
$data = json_decode($retrieve, 1);

// Check if the category exists
if (!$data) {
    $_SESSION['error'] = "Product not found.";
    header("Location: product.php");
    exit();
}

// Define the current step based on POST data or start at step 1.
$currentStep = isset($_POST['step']) ? (int)$_POST['step'] : 1;

// Fetch categories from the Firebase Realtime Database
$categoriesData = $db->retrieve("category"); 
$categories = json_decode($categoriesData, 1);

// Fetch supplier from the Firebase Realtime Database
$suppliersData = $db->retrieve("supplier"); 
$suppliers = json_decode($suppliersData, 1);

// Retrieve stored images if they exist
$productImages = isset($data['productImages']) ? $data['productImages'] : [];

// Retrieve product specifications from the data
$productSpecs = isset($data['productSpecs']) ? $data['productSpecs'] : [];

$productAR = isset($data['productAR']) ? $data['productAR'] : [];

// Ensure productSpecs is an array
$productDesigns = [];
$productPrices = [];
$productStocks = [];

if (is_array($productSpecs)) {
    foreach ($productSpecs as $spec) {
        $productDesigns[] = $spec['productDesign'] ?? '';
        $productPrices[] = $spec['productPrice'] ?? '';
        $productStocks[] = $spec['productStock'] ?? '';
    }
}

?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>View Product</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <script src="https://kit.fontawesome.com/1dd4ddab77.js" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.6.0/css/all.min.css" integrity="sha512-Kc323vGBEqzTmouAECnVceyQqyqdsSiqLQISBL29aUW4U/M7pSPA/gEUZQqv1cwx4OnYxTxve5UMg5GT6L4JJg==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <script src="https://code.jquery.com/jquery-3.7.1.min.js" integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>

    <style>
        <?php include "../css/addProduct.css" ?>
    </style>

    <script>
        function showStep(step) {
            document.getElementById('step1').style.display = 'none';
            document.getElementById('step2').style.display = 'none';
            document.getElementById('step3').style.display = 'none';
            
            document.getElementById('step' + step).style.display = 'block';
            updateProgress(step);
        }

        function updateProgress(step) {
            const steps = document.querySelectorAll('.circle');
            steps.forEach((circle, index) => {
                if (index < step) {
                    circle.classList.add('active');
                } else {
                    circle.classList.remove('active');
                }
            });
        }
    </script>

</head>
<body>
    <?php include 'navigationBar.php'; ?>

    <div class="content">
        <div class="content-box">
            <div class="row filterandaddbutton">
                <div class="col-xs-4 col-sm-6 col-md-9 col-lg-2">
                    <a href="product.php" class="btn add-button" id="add-button">< &nbsp Back</a>
                </div>
                <div class="col-xs-8 col-sm-6 col-md-3 col-lg-10">
                    <!-- Existing space or button -->
                </div>
            </div>

            <form action="../actions/action_editProduct.php" id="productForm" method="post" enctype="multipart/form-data">
                <div class="row formframe">
                    <div class="col-12" style="margin-bottom: 10px;">
                        <p class="bigtitle"><b>View Product</b></p>
                    </div>
                    
                    <!-- Step Progress Bar -->
                    <div class="step-progress">
                        <div class="step">
                            <div class="circle <?= ($currentStep >= 1) ? 'active' : '' ?>">1</div>
                            <p>General Info</p>
                        </div>
                        <div class="step">
                            <div class="circle <?= ($currentStep >= 2) ? 'active' : '' ?>">2</div>
                            <p>Pricing & Specs</p>
                        </div>
                        <div class="step">
                            <div class="circle <?= ($currentStep >= 3) ? 'active' : '' ?>">3</div>
                            <p>Images & AR</p>
                        </div>
                    </div>

                    <!-- Step 1: General Info -->
                    <div id="step1" style="display: <?= ($currentStep == 1) ? 'block' : 'none'; ?>">

                        <div class="col-12">
                            <p class="formtitle"><b>General Information</b></p>
                        </div>
                        <div class="col-12">
                            <hr class="line">
                        </div>

                        <div class="row">

                            <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                                <p class="formtitledescription">Update the product general information, then navigate to next pricing and spec page</p>
                            </div>

                            <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">

                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                                        <p class="col-12 title"><b>PRODUCT NAME</b></p>
                                        <p class="billAddress"><?php echo htmlspecialchars($data['productName']); ?></p>
                                    </div>
                                </div>
                                
                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                        <p class="title"><b>PRODUCT DESCRIPTION</b></p>
                                        <p class="billAddress"><?php echo htmlspecialchars($data['productDescription']); ?></p>
                                    </div>
                                </div>

                                <!-- Product Category -->
                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                        <p class="title"><b>PRODUCT CATEGORY</b></p>
                                        <p class="billAddress">
                                            <?php 
                                            $selectedCategory = isset($data['productCategory']) ? $data['productCategory'] : 'Not Selected';
                                            echo htmlspecialchars($selectedCategory);
                                            ?>
                                        </p>
                                    </div>

                                    <!-- Product Supplier -->
                                    <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                        <p class="title"><b>PRODUCT SUPPLIER</b></p>
                                        <p class="billAddress">
                                            <?php 
                                            $selectedSupplier = isset($data['productSupplier']) ? $data['productSupplier'] : 'Not Selected';
                                            echo htmlspecialchars($selectedSupplier);
                                            ?>
                                        </p>
                                    </div>
                                </div>

                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                                        <p class="title"><b>BRAND NAME</b></p>
                                        <p class="billAddress"><?php echo htmlspecialchars($data['productBrandName']); ?></p>
                                    </div>
                                </div>


                                <div class="row">
                                    <div class="col-lg-6"></div>
                                    <div class="col-lg-6">
                                        <button type="button" class="btn btn-primary" id="btn-submit" onclick="showStep(2)">Next &nbsp ></button>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>

                    <!-- Step 2: Pricing & Specs -->
                    <div id="step2" style="display: <?= ($currentStep == 2) ? 'block' : 'none'; ?>;">
                        <div class="col-12">
                            <p class="formtitle"><b>Pricing, Specifications, and Stock</b></p>
                        </div>
                        <div class="col-12">
                            <hr class="line">
                        </div>

                        <div class="row">
                            
                            <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                                <p class="formtitledescription">Enter the product pricing, specifications, and stock details, then navigate to the next step to add images and AR.</p>
                            </div>

                            <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">

                                <div class="row" style="margin-bottom: 0; padding-bottom: 0;">
                                    <p class="title"><b>PRODUCT SIZE / DIMENSIONS (In CM)</b></p>
                                </div>
                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-xs-12 col-sm-6 col-sm-6 col-lg-4">
                                        <p class="title"><b>Width</b></p>
                                        <p class="billAddress"><?php echo $data['productSizeWidth']; ?> cm</p>
                                    </div>
                                    <div class="col-xs-12 col-sm-6 col-sm-6 col-lg-4">
                                        <p class="title"><b>Height</b></p>
                                        <p class="billAddress"><?php echo $data['productSizeHeight']; ?> cm</p>
                                    </div>
                                    <div class="col-xs-12 col-sm-6 col-sm-6 col-lg-4">
                                        <p class="title"><b>Depth</b></p>
                                        <p class="billAddress"><?php echo $data['productSizeDepth']; ?> cm</p>
                                    </div>
                                </div>

                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-lg-6">
                                        <p class="title"><b>PRODUCT WEIGHT (In KG)</b></p>
                                        <p class="billAddress"><?php echo $data['productWeight']; ?> kg</p>
                                        </div>
                                    <div class="col-lg-6">
                                        <p class="title"><b>PRODUCT MATERIAL</b></p>
                                        <p class="billAddress"><?php echo $data['productMaterial']; ?></p>
                                    </div>
                                </div>

                                <!-- Product Warranty -->
                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-lg-6">
                                        <p class="title"><b>PRODUCT WARRANTY</b></p>
                                        <p class="billAddress"><?php echo (isset($data['hasWarranty']) && $data['hasWarranty'] === 'yes') ? 'Yes' : 'No'; ?></p>
                                    </div>

                                     <!-- Warranty Duration -->
                                    <?php if (isset($data['hasWarranty']) && $data['hasWarranty'] === 'yes'): ?>
                                        <div class="col-lg-6" style="margin-bottom: 10px;">
                                            <p class="title"><b>WARRANTY DURATION</b></p>
                                            <p class="billAddress"><?php echo $data['warrantyDurationValue'] . ' ' . ucfirst($data['warrantyDurationUnit']); ?></p>
                                        </div>
                                    <?php endif; ?>
                                </div>
                                
                                <!-- Product Specifications -->
                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-lg-12">
                                        <p class="title"><b>PRODUCT SPECIFICATIONS</b></p>
                                        <div id="productSpecList" class="product-specs-list">
                                            <?php
                                            // Loop through the specs and display them
                                            if (!empty($data['productDesign']) && !empty($data['productPrice']) && !empty($data['productStock'])) {
                                                foreach ($data['productDesign'] as $index => $design) {
                                                    echo "<div class='product-spec'>";
                                                    echo "<p><b>Design:</b> " . htmlspecialchars($design) . "</p>";
                                                    echo "<p><b>Price (RM):</b> " . htmlspecialchars($data['productPrice'][$index]) . "</p>";
                                                    echo "<p><b>Stock:</b> " . htmlspecialchars($data['productStock'][$index]) . " units</p>";
                                                    
                                                    echo "</div>";
                                                }
                                            } else {
                                                echo "<p>No specifications available.</p>";
                                            }
                                            ?>
                                        </div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-lg-6">
                                        <button type="button" class="btn btn-secondary" id="btn-back" onclick="showStep(1)">< &nbsp Back</button>
                                    </div>
                                    <div class="col-lg-6">
                                        <button type="button" class="btn btn-primary" id="btn-submit" onclick="showStep(3)">Next &nbsp ></button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Step 3: Images & AR -->
                    <div id="step3" style="display: <?= ($currentStep == 3) ? 'block' : 'none'; ?>;">
                        <div class="col-12">
                            <p class="formtitle"><b>Images and AR</b></p>
                        </div>
                        <div class="col-12">
                            <hr class="line">
                        </div>

                        <div class="row">
                            <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                                <p class="formtitledescription">Enter the product pricing, specifications, and stock details, then navigate to the next step to add images and AR.</p>
                            </div>

                            <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">

                                <div class="row" style="margin-bottom: 10px;">
                                    <p class="title"><b>PRODUCT IMAGES </b></p>
                                    <div  class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                        <div class="wrapper">
                                            <div class="upload">
                                                <div class="upload-wrapper">
                                                    <div class="upload-img">
                                                        <!-- Images will be added here -->
                                                        <?php if (!empty($productImages)): ?>
                                                            <?php foreach ($productImages as $index => $base64Image): ?>
                                                                <div class="uploaded-img">
                                                                    <img src="data:image/jpeg;base64,<?php echo htmlspecialchars($base64Image); ?>">
                                                                    <button type="button" class="remove-btn">
                                                                        <i class="fas fa-times"></i>
                                                                    </button>
                                                                </div>
                                                            <?php endforeach; ?>
                                                        <?php endif; ?>
                                                    </div>
                                                    <div class="upload-info">
                                                        <p>
                                                            <span class="upload-info-value"><?php echo count($productImages); ?></span> file(s) uploaded.
                                                        </p>
                                                    </div>
                                                  
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- AR File Section -->
                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                        <p class="title"><b>AR FILE</b> (Optional)</p>
                                        <?php if (!empty($productAR)): ?>
                                            <!-- Display the existing AR file name if present -->
                                            <p class="form-control inputfield">
                                                Current File: <a href="path_to_AR_file/<?php echo htmlspecialchars($productAR); ?>" target="_blank"><?php echo htmlspecialchars(basename($productAR)); ?></a>
                                            </p>
                                        <?php endif; ?>
                                       </div>
                                </div>

                                <div class="row">
                                    <div class="col-lg-6">
                                        <button type="button" class="btn btn-secondary" id="btn-back" onclick="showStep(2)">< &nbsp Back</button>
                                    </div>
                                    <div class="col-lg-6">
                                         <!-- Hidden fields for storing images and specs -->
                                        <input type="hidden" name="productSpecs" id="productSpecs" />
                                        <input type="hidden" name="id" value="<?php echo $id ?>">
                                        <button type="submit" class="btn btn-submit" id="btn-submit">Submit</button>
                                    </div>
                                </div>

                                <div id="priceDisplay"></div>

                            </div>
                        </div>
                        
                    </div>

                </div>
            </form>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.7.1.min.js" integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>
    <script>
        $(document).ready(function() {
            const base64Images = <?php echo json_encode($productImages); ?>; // Load initial images from PHP

            $(".upload-area").click(function() {
                $('#upload-input').trigger('click');
            });

            $('#upload-input').change(function(event) {
                const files = event.target.files;
                const filesAmount = files.length;
                $('.upload-img').html(""); // Clear previous images

                base64Images.length = 0; // Reset the array for new uploads

                for (let i = 0; i < filesAmount; i++) {
                    let reader = new FileReader();
                    reader.onload = function(event) {
                        let rawBase64 = event.target.result.split(',')[1]; // Remove the prefix
                        let html = `
                            <div class="uploaded-img">
                                <img src="${event.target.result}">
                            </div>
                        `;
                        $(".upload-img").append(html);
                        base64Images.push(rawBase64); // Add the raw Base64 string to the array
                    };
                    reader.readAsDataURL(files[i]);
                }

                $('.upload-info-value').text(filesAmount);
                $('.upload-img').css('padding', "20px");

                // Clear the file input to prevent duplicate uploads
                $('#upload-input').val("");
            });

        });
    </script>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        function toggleWarrantyDuration(show) {
                const warrantyDuration = document.getElementById('warrantyDuration');
                const warrantyDurationValue = document.querySelector('input[name="warrantyDurationValue"]');
                const warrantyDurationUnit = document.getElementById('warrantyDurationUnit');

                if (show) {
                    // Show the warranty duration fields
                    warrantyDuration.style.display = 'block';
                } else {
                    // Hide the warranty duration fields
                    warrantyDuration.style.display = 'none';
                    // Clear the warranty duration input fields
                    warrantyDurationValue.value = '';  
                    warrantyDurationUnit.value = 'days'; 
                }
            }

        // Call toggleWarrantyDuration on page load based on the current warranty status
        document.addEventListener('DOMContentLoaded', function() {
            const hasWarrantyYes = document.getElementById('warrantyYes');
            if (hasWarrantyYes.checked) {
                toggleWarrantyDuration(true);
            } else {
                toggleWarrantyDuration(false);
            }
        });
    </script>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
        // Initialize product spec arrays from PHP data
        let productDesignArray = <?php echo json_encode($productDesigns); ?>;
        let productPriceArray = <?php echo json_encode($productPrices); ?>;
        let productStockArray = <?php echo json_encode($productStocks); ?>;

        document.addEventListener('DOMContentLoaded', function() {
            displayProductSpecs(); // Display existing specs on page load
        });

        function displayProductSpecs() {
            const productSpecList = document.getElementById('productSpecList');
            productSpecList.innerHTML = '';

            // Loop through the specs arrays and display each specification
            productDesignArray.forEach((design, index) => {
                const price = productPriceArray[index] || '';
                const stock = productStockArray[index] || '';

                const div = document.createElement('div');
                div.className = 'product-spec-box';
                div.innerHTML = `
                    <p style="font-size: 16px;"><b>${design}</b></p>
                    <p>Price: <span class="spec-detail">RM ${price}</span></p>
                    <p>Stock: <span class="spec-detail">${stock}</span></p>
                `;
                productSpecList.appendChild(div);
            });
        }
    </script>

    <script type="module" src="https://cdn.jsdelivr.net/npm/@google/model-viewer"></script>

    <script>
        document.getElementById('arFileUpload').addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file) {
                alert(`Selected AR File: ${file.name}`);
            }
        });
    </script>

    <script>
        function showStep(step) {
            document.getElementById('step1').style.display = 'none';
            document.getElementById('step2').style.display = 'none';
            document.getElementById('step3').style.display = 'none';
            
            document.getElementById('step' + step).style.display = 'block';
            updateProgress(step);
        }

        function updateProgress(step) {
            const steps = document.querySelectorAll('.circle');
            steps.forEach((circle, index) => {
                if (index < step) {
                    circle.classList.add('active');
                } else {
                    circle.classList.remove('active');
                }
            });
        }
    </script>

</body>
</html>