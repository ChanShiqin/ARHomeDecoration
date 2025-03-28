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
    <title>Edit Product</title>
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

    <script type="module">
        import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.2/firebase-app.js";
        import { getStorage, ref, uploadBytes, getDownloadURL } from "https://www.gstatic.com/firebasejs/11.0.2/firebase-storage.js";

        const firebaseConfig = {
        apiKey: "AIzaSyBnX_a75nqKIuQI7FLjaUWBWj7CCsyz17Q",
        authDomain: "arhomedecorationapplication.firebaseapp.com",
        databaseURL: "https://arhomedecorationapplication-default-rtdb.asia-southeast1.firebasedatabase.app",
        projectId: "arhomedecorationapplication",
        storageBucket: "arhomedecorationapplication.appspot.com",
        messagingSenderId: "497447156897",
        appId: "1:497447156897:web:7a3b0f198f395e9165c4e0",
        measurementId: "G-J9C7MGQJNH"
        };

        // Initialize Firebase
        const app = initializeApp(firebaseConfig);

        // Initialize Firebase Storage
        const storage = getStorage(app);

        document.getElementById('arFileUpload').addEventListener('change', function(event) {
            const fileInput = document.querySelector("#arFileUpload");
            const file = fileInput.files[0];

            if (file) {
                console.log("File selected:", file.name);


                const name = new Date().getTime() + '-' + file.name; // Use timestamp to ensure unique file names
                const metadata = {
                    contentType: file.type
                };

                // Create a reference to the storage location
                const storageRef = ref(storage, name);

                // Upload the file to Firebase Storage
                console.log("Uploading file to Firebase Storage...");
                uploadBytes(storageRef, file, metadata)
                    .then(snapshot => {
                        console.log("Upload complete, snapshot:", snapshot);
                        return getDownloadURL(snapshot.ref); // Retrieve the download URL after upload
                    })
                    .then(url => {
                        console.log("File uploaded successfully, download URL:", url);
                        if (url) {
                            document.getElementById('productARURL').value = url; // Set URL to hidden field
                            alert("AR File Upload Successful!, download URL:", url);

                            // Log to confirm the button is re-enabled
                            console.log("Submit button re-enabled after file upload.");
                        } else {
                            console.error("Download URL is empty.");
                        }
                    })
                    .catch(error => {
                        console.error("Error uploading file:", error);
                        alert("Upload failed: " + error.message);

                    });
            } else {
                console.log("No file selected");
                alert("Please select a file to upload.");

            }
        });

    </script>

    <script>
        function toggleWarrantyDuration(show) {
            const warrantyDurationRow = document.getElementById('warrantyDuration');
            const warrantyDurationValue = document.querySelector('[name="warrantyDurationValue"]');
            const warrantyDurationUnit = document.querySelector('[name="warrantyDurationUnit"]');
            
            // If warranty is "Yes", show the duration fields and set them as required
            if (show) {
                warrantyDurationRow.style.display = 'block';
                warrantyDurationValue.required = true;
                warrantyDurationUnit.required = true;
            } else {
                // If warranty is "No", hide the duration fields and remove the required validation
                warrantyDurationRow.style.display = 'none';
                warrantyDurationValue.required = false;
                warrantyDurationUnit.required = false;
            }
        }

        function validateStep(step) {
            let valid = true;
            let stepInputs;

            if (step === 1) {
                stepInputs = document.querySelectorAll('#step1 input[required], #step1 textarea[required], #step1 select[required]');
            } else if (step === 2) {
                stepInputs = document.querySelectorAll('#step2 input[required], #step2 textarea[required], #step2 select[required]');
                
                // Validate product size fields (width, height, depth) and product weight
                const sizeFields = ['productSizeWidth', 'productSizeHeight', 'productSizeDepth', 'productWeight'];
                let invalidFields = [];  // Array to store the names of invalid fields
                
                sizeFields.forEach(fieldName => {
                    const input = document.querySelector(`[name="${fieldName}"]`);
                    const value = parseFloat(input.value);
                    if (isNaN(value) || value <= 0) { // Ensure the value is a number and greater than 0
                        valid = false;
                        input.classList.add('is-invalid');
                        invalidFields.push(input.name);  // Add invalid field to the array
                    } else {
                        input.classList.remove('is-invalid');
                    }
                });

                // If there are any invalid size or weight fields, show a single alert
                if (invalidFields.length > 0) {
                    let message = 'Please enter valid numbers for the following fields: ';
                    message += invalidFields.join(', '); // Join the invalid fields into a string
                    alert(message);
                }

                // Check if the user selected a warranty option (yes or no)
                const warrantyOptions = document.querySelectorAll('input[name="hasWarranty"]:checked');
                if (warrantyOptions.length === 0) {
                    valid = false;
                    alert('Please choose a product warranty option.');
                }

                // Additional validation for warranty duration if warranty is "Yes"
                const warrantyYes = document.querySelector('#warrantyYes').checked;
                if (warrantyYes) {
                    const warrantyValue = document.querySelector('[name="warrantyDurationValue"]');
                    const warrantyUnit = document.querySelector('[name="warrantyDurationUnit"]');
                    if (!warrantyValue.value || !warrantyUnit.value) {
                        valid = false;
                        warrantyValue.classList.add('is-invalid');
                        warrantyUnit.classList.add('is-invalid');
                        alert('Please fill in the warranty duration.');
                    } else {
                        warrantyValue.classList.remove('is-invalid');
                        warrantyUnit.classList.remove('is-invalid');
                    }
                }

                // Check if at least one product spec has been added
                if (productDesignArray.length === 0 || productPriceArray.length === 0 || productStockArray.length === 0) {
                    valid = false;
                    alert('Please add at least one product specification.');
                }

                // Validate price and stock for each product specification
                productPriceArray.forEach((price, index) => {
                    if (isNaN(price) || price <= 0) { // Validate price is a positive number
                        valid = false;
                        alert(`Please enter a valid price for product spec #${index + 1}.`);
                    }
                });

                productStockArray.forEach((stock, index) => {
                    if (!Number.isInteger(stock) || stock < 0) { // Validate stock is a non-negative integer
                        valid = false;
                        alert(`Please enter a valid stock quantity for product spec #${index + 1}.`);
                    }
                });
            } else if (step === 3) {
                // Add validation for step 3 if needed
                stepInputs = document.querySelectorAll('#step3 input[required], #step3 textarea[required], #step3 select[required]');
            }

            stepInputs.forEach(input => {
                if (!input.value.trim()) {
                    valid = false;
                    input.classList.add('is-invalid'); // Highlight invalid fields
                } else {
                    input.classList.remove('is-invalid'); // Remove invalid class if filled
                }
            });

            return valid;
        }

        function showStep(step) {
            // Validate current step before navigating
            if (step === 2 && !validateStep(1)) {
                alert('Please fill out all required fields in Step 1 before proceeding.');
                return;
            }

            if (step === 3 && !validateStep(2)) {
                alert('Please fill out all required fields in Step 2 before proceeding.');
                return;
            }

            // Show the appropriate step
            document.getElementById('step1').style.display = (step === 1) ? 'block' : 'none';
            document.getElementById('step2').style.display = (step === 2) ? 'block' : 'none';
            document.getElementById('step3').style.display = (step === 3) ? 'block' : 'none';
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

            <div class="row">
                <?php if (isset($_SESSION['error'])): ?>
                    <div id="errorAlert" class="col-12 alert alert-danger alert-dismissible fade show" role="alert">
                        <?php echo htmlspecialchars($_SESSION['error']); ?>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    <?php unset($_SESSION['error']); // Clear the session message ?>
                <?php endif; ?>
            </div>

            <form action="../actions/action_editProduct.php" id="productForm" method="post" enctype="multipart/form-data">
                <div class="row formframe">
                    <div class="col-12" style="margin-bottom: 10px;">
                        <p class="bigtitle"><b>Edit Product</b></p>
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
                                        <input type="text" name="productName" class="form-control inputfield" placeholder="Enter Product Name" value="<?php echo $data['productName'] ?>" required>
                                    </div>
                                </div>
                                
                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                        <p class="title"><b>PRODUCT DESCRIPTION</b></p>
                                        <textarea name="productDescription" class="form-control inputfield" rows="2" placeholder="Enter Product Description" required><?php echo $data['productDescription']; ?></textarea>
                                    </div>
                                </div>

                                <!-- Product Category -->
                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                        <p class="title"><b>PRODUCT CATEGORY</b></p>
                                        <select id="productCategory" name="productCategory" class="form-control inputfield" required>
                                            <option value="" disabled>-- Select Product Category --</option>
                                            <?php foreach ($categories as $category): ?>
                                                <option value="<?= htmlspecialchars($category['categoryName']); ?>" 
                                                        <?= isset($data['productCategory']) && $data['productCategory'] == $category['categoryName'] ? 'selected' : '' ?>>
                                                    <?= htmlspecialchars($category['categoryName']); ?>
                                                </option>
                                            <?php endforeach; ?>
                                        </select>
                                    </div>

                                    <!-- Product Supplier -->
                                    <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                        <p class="title"><b>PRODUCT SUPPLIER</b></p>
                                        <select id="productSupplier" name="productSupplier" class="form-control inputfield" required>
                                            <option value="" disabled>-- Select Product Supplier --</option>
                                            <?php foreach ($suppliers as $supplier): ?>
                                                <option value="<?= htmlspecialchars($supplier['supplierCompanyName']); ?>" 
                                                        <?= isset($data['productSupplier']) && $data['productSupplier'] == $supplier['supplierCompanyName'] ? 'selected' : '' ?>>
                                                    <?= htmlspecialchars($supplier['supplierCompanyName']); ?>
                                                </option>
                                            <?php endforeach; ?>
                                        </select>
                                    </div>
                                </div>

                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                                        <p class="title"><b>BRAND NAME</b></p>
                                        <input type="text" name="productBrandName" class="form-control inputfield" placeholder="Enter Brand Name" value="<?php echo $data['productBrandName'] ?>" required>
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
                                    <!-- <div class="col-xs-12 col-sm-6 col-sm-6 col-lg-4">
                                        <p class="title"><b>Length</b></p>
                                        <input type="number" name="productSizeLength" class="form-control inputfield" placeholder="Enter Product Lenght (cm)" value="<?php echo $data['productSizeLength'] ?>" required>
                                    </div> -->
                                    <div class="col-xs-12 col-sm-6 col-sm-6 col-lg-4">
                                        <p class="title"><b>Width</b></p>
                                        <input type="number" name="productSizeWidth" class="form-control inputfield" placeholder="Enter Product Width (cm)" value="<?php echo $data['productSizeWidth'] ?>" required>
                                    </div>
                                    <div class="col-xs-12 col-sm-6 col-sm-6 col-lg-4">
                                        <p class="title"><b>Height</b></p>
                                        <input type="number" name="productSizeHeight" class="form-control inputfield" placeholder="Enter Product Height (cm)" value="<?php echo $data['productSizeHeight'] ?>" required>
                                    </div>
                                    <div class="col-xs-12 col-sm-6 col-sm-6 col-lg-4">
                                        <p class="title"><b>Depth</b></p>
                                        <input type="number" name="productSizeDepth" class="form-control inputfield" placeholder="Enter Product Depth (cm)" value="<?php echo $data['productSizeDepth'] ?>" required>
                                    </div>
                                </div>

                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-lg-6">
                                        <p class="title"><b>PRODUCT WEIGHT (In KG)</b></p>
                                        <input type="number" name="productWeight" class="form-control inputfield" placeholder="Enter Product Weight (kg)" value="<?php echo $data['productWeight'] ?>" required>
                                    </div>
                                    <div class="col-lg-6">
                                        <p class="title"><b>PRODUCT MATERIAL</b></p>
                                        <input type="text" name="productMaterial" class="form-control inputfield" placeholder="Enter Product Material" value="<?php echo $data['productMaterial'] ?>" required>
                                    </div>
                                </div>

                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-lg-4">
                                        <p class="title"><b>PRODUCT WARRANTY</b></p>
                                        <div class="warranty-options">
                                            <!-- Yes Option -->
                                            <div class="form-check form-check-inline">
                                                <input class="form-check-input custom-radio" type="radio" name="hasWarranty" id="warrantyYes" 
                                                    value="yes" 
                                                    onchange="toggleWarrantyDuration(true)" 
                                                    <?php echo (isset($data['hasWarranty']) && $data['hasWarranty'] === 'yes') ? 'checked' : ''; ?> required>
                                                <label class="form-check-label" for="warrantyYes">Yes</label>
                                            </div>
                                            <!-- No Option -->
                                            <div class="form-check form-check-inline">
                                                <input class="form-check-input custom-radio" type="radio" name="hasWarranty" id="warrantyNo" 
                                                    value="no" 
                                                    onchange="toggleWarrantyDuration(false)" 
                                                    <?php echo (isset($data['hasWarranty']) && $data['hasWarranty'] === 'no') ? 'checked' : ''; ?> required>
                                                <label class="form-check-label" for="warrantyNo">No</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Warranty Duration (hidden by default) -->
                                <div class="row" id="warrantyDuration" style="display: none; margin-bottom: 10px;">
                                    <div class="col-lg-8">
                                        <p class="title"><b>WARRANTY DURATION</b></p>
                                        <div class="input-group">
                                        <select class="form-select inputfield" id="warrantyDurationUnit" name="warrantyDurationUnit" style="font-size: 12px;" value="<?php echo $data['warrantyDurationUnit'] ?>">
                                            <option value="days">Days</option>
                                            <option value="months">Months</option>
                                            <option value="years">Years</option>
                                        </select>
                                        <input class="form-control inputfield" type="number" name="warrantyDurationValue" placeholder="Enter Warranty Duration" min="1" value="<?php echo $data['warrantyDurationValue'] ?>">
                                        </div>
                                    </div>
                                </div>

                                <!-- Product Color, Price, and Stock -->
                                <!-- Display added product specs -->
                                <div class="row" style="margin-bottom: 10px;">
                                    <div class="col-lg-12">
                                        <p class="title"><b>ADD PRODUCT SPECIFICATIONS</b></p>
                                        <div id="productSpecList" class="product-specs-list">
                                            <!-- Added product specs will be displayed here in small boxes -->
                                        </div>
                                    </div>
                                </div>

                                <div class="row" id="productSpecsContainer" style="margin-bottom: 10px;">
                                    <div class="col-lg-6">
                                        <p class="title"><b>Product Color / Design</b></p>
                                        <input type="text" name="color" class="form-control inputfield" id="product_Color" placeholder="Enter Product Color">
                                    </div>
                                    <div class="col-lg-6">
                                        <p class="title"><b>Product Price (RM)</b></p>
                                        <input type="number" name="price" class="form-control inputfield" id="product_Price" placeholder="Enter Product Price">
                                    </div>
                                    <div class="col-lg-6">
                                        <p class="title"><b>Stock Availability (Units)</b></p>
                                        <input type="number" name="availability" class="form-control inputfield" id="product_Stock" placeholder="Enter Stock Availability">
                                    </div>
                                    <div class="col-lg-6">
                                        <p class="title"><b>&nbsp;</b></p>
                                        <button type="button" class="btn btn-secondary" id="addSpecButton" onclick="addProductSpec()">
                                            <i class="fas fa-plus"></i> <!-- Font Awesome + sign -->
                                        </button>
                                    </div>
                                </div>

                                <input type="hidden" id="productPrices" name="productPrices">
                                <input type="hidden" id="productStocks" name="productStocks">


                                <input type="hidden" id="productDesign" name="productDesign">
                                <input type="hidden" id="productPrice" name="productPrice">
                                <input type="hidden" id="productStock" name="productStock">

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
                                                    <div class="upload-area">
                                                        <div class="upload-area-img">
                                                            <img src="../images/upload.png" alt="">
                                                        </div>
                                                        <p class="upload-area-text">Select image or <span>browse</span>.</p>
                                                    </div>
                                                    <!-- <input type="file" name="productImages" class="visibility-hidden" id="upload-input" multiple> -->
                                                    <input type="file" id="upload-input" class="visibility-hidden" name="productImages[]" multiple accept="image/*" >
                                                    <!-- <input type="file" id="upload-input" class="visibility-hidden" name="productImages[]" multiple accept="image/*" required> -->
                                                    <input type="hidden" name="base64Images" id="base64Images">
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
                                            <input type="hidden" name="existingProductAR" value="<?php echo htmlspecialchars($productAR); ?>">
                                        <?php endif; ?>
                                        <input type="file" name="productAR" class="form-control inputfield" id="arFileUpload" accept=".glb, .gltf">
                                        <input type="hidden" name="productARURL" id="productARURL" />
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
                                <button type="button" class="remove-btn">
                                    <i class="fas fa-times"></i>
                                </button>
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

            // Handle image removal
            $(window).on("click", function(event) {
                if ($(event.target).hasClass('remove-btn') || $(event.target).parent().hasClass('remove-btn')) {
                    // Remove the image element
                    const imgElement = $(event.target).closest('.uploaded-img');
                    const index = imgElement.index();

                    imgElement.remove();
                    base64Images.splice(index, 1); // Remove from the Base64 array

                    // Update the file count
                    let currentCount = parseInt($('.upload-info-value').text());
                    if (currentCount > 0) {
                        $('.upload-info-value').text(currentCount - 1);
                    }

                    // Check if there are no more uploaded images
                    if ($('.upload-img').children().length === 0) {
                        $('.upload-info-value').text(0);
                        $('.upload-img').css('padding', "0"); // Remove padding when empty
                    }
                }
            });

            // Before form submission, set the Base64 images in the hidden input
            $('.btn-submit').click(function(event) {
                if (base64Images.length === 0) { // Check if no images are uploaded
                    alert("Please upload at least one image.");
                    event.preventDefault(); // Prevent form submission
                    return false; // Stop the function from proceeding
                }

                // Only set Base64 images if there are any uploaded
                $('#base64Images').val(JSON.stringify(base64Images)); // Store the raw Base64 array as a JSON string
                return true; // Allow form submission
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
                    warrantyDurationValue.value = '';  // Clear the value field
                    warrantyDurationUnit.value = 'days';  // Reset the unit field to 'days' (or any default you prefer)
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

        // function displayProductSpecs() {
        //     const productSpecList = document.getElementById('productSpecList');
        //     productSpecList.innerHTML = '';

        //     // Loop through the specs arrays and display each specification
        //     productDesignArray.forEach((design, index) => {
        //         const price = productPriceArray[index] || '';
        //         const stock = productStockArray[index] || '';

        //         const div = document.createElement('div');
        //         div.className = 'product-spec-box';
        //         div.innerHTML = `
        //             <p style="font-size: 16px;"><b>${design}</b></p>
        //             <p>Price: <span class="spec-detail">RM ${price}</span></p>
        //             <p>Stock: <span class="spec-detail">${stock}</span></p>
        //             <button class="remove-btn" onclick="removeProductSpec(${index})">
        //                 <i class="fas fa-minus"></i>
        //             </button>
        //         `;
        //         productSpecList.appendChild(div);
        //     });
        // }

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
            <p>Price: <input type="number" class="spec-detail" value="${price}" onchange="updateProductSpec(${index}, 'price', this.value)" /></p>
            <p>Stock: <input type="number" class="spec-detail" value="${stock}" onchange="updateProductSpec(${index}, 'stock', this.value)" /></p>
            <button class="remove-btn" onclick="removeProductSpec(${index})">
                <i class="fas fa-minus"></i>
            </button>
        `;
        productSpecList.appendChild(div);
    });
}

function updateProductSpec(index, type, value) {
    if (type === 'price') {
        productPriceArray[index] = parseFloat(value).toFixed(2); // Update price as a number with 2 decimals
    } else if (type === 'stock') {
        productStockArray[index] = parseInt(value, 10); // Update stock as an integer
    }

    // After updating the arrays, update the hidden input values
    document.getElementById('productPrices').value = productPriceArray.join(',');
    document.getElementById('productStocks').value = productStockArray.join(',');
}


function addProductSpec() {
    const productColor = document.getElementById('product_Color').value;
    const productPrice = document.getElementById('product_Price').value;
    const productStock = document.getElementById('product_Stock').value;

    if (productColor && productPrice && productStock) {
        productDesignArray.push(productColor);
        productPriceArray.push(parseFloat(productPrice).toFixed(2)); // Format price as 2 decimal places
        productStockArray.push(parseInt(productStock, 10)); // Ensure stock is an integer

        // Call function to update the displayed specs
        displayProductSpecs();

        // Clear input fields
        document.getElementById('product_Color').value = '';
        document.getElementById('product_Price').value = '';
        document.getElementById('product_Stock').value = '';
    }
}

        // function addProductSpec() {
        //     const color = document.getElementById('product_Color').value;
        //     const price = document.getElementById('product_Price').value; // Ensure it's a number
        //     const stock = document.getElementById('product_Stock').value;   // Ensure it's a number

        //     console.log('Color:', color);
        //     console.log('Price:', price);
        //     console.log('Stock:', stock);

        //     // Validate all fields and check for valid number inputs
        //     if (color && !isNaN(price) && parseFloat(price) >= 0 && !isNaN(stock) && Number.isInteger(parseFloat(stock)) && parseFloat(stock) >= 0) {
        //         productDesignArray.push(color);
        //         productPriceArray.push(parseFloat(price).toFixed(2)); // Ensure price is in monetary format (e.g., 12.00)
        //         productStockArray.push(parseInt(stock)); // Ensure stock is an integer

        //         console.log('Updated Product Design Array:', productDesignArray);
        //         console.log('Updated Product Price Array:', productPriceArray);
        //         console.log('Updated Product Stock Array:', productStockArray);

        //         displayProductSpecs();

        //         // Clear the input fields
        //         document.getElementById('product_Color').value = '';
        //         document.getElementById('product_Price').value = '';
        //         document.getElementById('product_Stock').value = '';
        //     } else {
        //         alert('Please fill out all fields correctly.\nEnsure price is a number and stock is an integer.');
        //     }
        // }

        function removeProductSpec(index) {
            productDesignArray.splice(index, 1);
            productPriceArray.splice(index, 1);
            productStockArray.splice(index, 1);

            displayProductSpecs();
        }

        $('form').on('submit', function() {
            $('#productDesign').val(JSON.stringify(productDesignArray));
            $('#productPrice').val(JSON.stringify(productPriceArray));
            $('#productStock').val(JSON.stringify(productStockArray));

            // Update hidden inputs with current values before submitting
document.getElementById('productPrices').value = productPriceArray.join(',');
document.getElementById('productStocks').value = productStockArray.join(',');

        });

    </script>

    <script>
    $(document).ready(function() {
        // Existing upload area click and image handling code...

        // Handle product specifications
        $('#addSpecBtn').click(function() {
            const specKey = $('#specKey').val();
            const specValue = $('#specValue').val();
            
            if (specKey && specValue) {
                let specsArray = $('#productSpecs').val() ? JSON.parse($('#productSpecs').val()) : [];
                specsArray.push({ key: specKey, value: specValue });

                $('#productSpecs').val(JSON.stringify(specsArray));
                $('#specsList').append(`<li>${specKey}: ${specValue}</li>`);
                $('#specKey').val('');
                $('#specValue').val('');
            }
        });
    });
    </script>

    <!-- JavaScript to handle selected class -->
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const categorySelect = document.getElementById('productCategory');
            const supplierSelect = document.getElementById('productSupplier');

            function updateSelectColor(selectElement) {
                if (selectElement.value) {
                    selectElement.classList.add('option-selected');
                } else {
                    selectElement.classList.remove('option-selected');
                }
            }

            categorySelect.addEventListener('change', function() {
                updateSelectColor(categorySelect);
            });

            supplierSelect.addEventListener('change', function() {
                updateSelectColor(supplierSelect);
            });

            // Initial call to set color on load if value exists
            updateSelectColor(categorySelect);
            updateSelectColor(supplierSelect);
        });
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

</body>
</html>