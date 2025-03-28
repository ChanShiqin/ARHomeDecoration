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
$retrieve = $db->retrieve("user/$id");
$data = json_decode($retrieve, 1);

// Check if the admin exists
if (!$data) {
    $_SESSION['error'] = "Customer account not found.";
    header("Location: customer.php");
    exit();
}

?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Customer</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://kit.fontawesome.com/1dd4ddab77.js" crossorigin="anonymous"></script>
    <style>
        <?php include "../css/addAdmin.css" ?>
    </style>
</head>
<body>
    <?php include 'navigationBar.php'; ?>   
    <div class="content">
        <div class="content-box">
            <div class="row filterandaddbutton">
                <div class="col-xs-4 col-sm-6 col-md-9 col-lg-2">
                    <a href="customer.php" class="btn add-button" id="add-button">< &nbsp Back</a>
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

            <form action="../actions/action_editCustomer.php" method="post" enctype="multipart/form-data">
                <div class="row formframe">
                    <div class="col-12">
                        <p class="bigtitle"><b>Edit Customer</b></p>
                        <p class="formtitle"><b>Personal Informatiom</b></p>
                    </div>

                    <div class="col-12">
                        <hr class="line">
                    </div>

                    <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                        <!-- <p class="formtitle"><b>General Informatiom</b></p> -->
                        <p class="formtitledescription">Update the customer personal details</p>
                    </div>

                    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                                <p class="col-12 title" style="padding-left: 0;"><b>CUSTOMER PROFILE PIC</b></p>
                            </div>  
                            <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                                <!-- Image preview element -->
                                <!-- <img id="customerProfilePicPreview" src="../images/default_admin.jpeg" alt="Profile Pic Preview" style="display: block; margin-top: 10px; max-width: 100px; border-radius: 50%; margin-bottom: 10px;"> -->
                                <!-- Image preview element -->
                                <?php if (!empty($data['profilePic'])): ?>
                                    <!-- Display the current icon -->
                                    <img id="customerProfilePicPreview" src="data:image/png;base64,<?php echo $data['profilePic'] ?>" alt="Customer Profile Pic" style="margin-top: 10px; max-width: 100px;">
                                <?php else: ?>
                                    <img id="customerProfilePicPreview" src="../images/default_admin.jpeg" alt="Profile Pic Preview" style="display: block; margin-top: 10px; max-width: 100px; border-radius: 50%; margin-bottom: 10px;">
                                <?php endif; ?>

                                <input type="file" name="profilePic" class="form-control inputfield" accept="image/png, image/jpeg, image/gif, image/jpg" id="customerProfilePicUpload">
                            </div>

                        </div>

                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                                <p class="col-12 title" style="padding-left: 0;"><b>CUSTOMER FULL NAME</b></p>
                                <input type="text" name="customerName" class="form-control inputfield" placeholder="Enter Customer Full Name" value="<?php echo $data['name'] ?>" required>
                            </div>
                            <!-- <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                                <p class="col-12 title" style="padding-left: 0;"><b>DEPARTMENT</b></p>

                            </div> -->
                        </div>

                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>CUSTOMER PHONE NO.</b></p>
                                <input type="text" name="customerPhoneNo" class="form-control inputfield" placeholder="Example: 0123456789" value="<?php echo $data['phoneNo'] ?>" required
                                pattern="^0[1-9][0-9]{7,9}$" title="Please enter a valid Malaysian phone number (e.g., 0123456789 or +60123456789)">
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>CUSTOMER EMAIL (use as username to login)</b></p>
                                <!-- <input type="email" name="customerEmail" class="form-control inputfield" placeholder="Enter Customer Email" value="<?php echo $data['email'] ?>" required> -->
                                <input type="email" name="customerEmail" class="form-control inputfield" placeholder="Enter Customer Email" value="<?php echo $data['email'] ?>" required disabled>

                            </div>
                        </div>

                        <!-- <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>PASSWORD</b></p>
                                <input type="password" name="customerPassword" id="customerPassword" class="form-control inputfield" placeholder="Enter Password" value="<?php echo $data['password'] ?>" required disabled
                                    pattern="(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}" 
                                    title="Password must be at least 8 characters long and include at least one uppercase letter, one number, and one special character.">
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>PASSWORD CONFIRM</b></p>
                                <input type="password" name="customerPasswordConfirm" id="customerPasswordConfirm" class="form-control inputfield" placeholder="Confirm Password" value="<?php echo $data['password'] ?>" required disabled>
                                <span id="passwordError" style="color: red; display: none;">Passwords do not match. Please try again.</span>
                            </div>
                        </div> -->

                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                <p class="col-12 title" style="padding-left: 0;"><b>USER ADDRESS</b></p>
                                <input type="text" name="customerAddress" class="form-control inputfield" placeholder="Enter customer address" value="<?php echo $data['address'] ?>" required >
                            </div>
                        </div>

                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>POSCODE</b></p>
                                <input type="text" name="customerPoscode" class="form-control inputfield" placeholder="Example: 12400" value="<?php echo $data['poscode'] ?>" required
                                pattern="^[0-9]{5}$" title="Please enter a valid 5-digit Malaysian poscode (e.g., 12300)">
                            </div>
                            <!-- <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>STATE</b></p>
                                <input type="text" name="customerState" class="form-control inputfield" placeholder="Enter Customer State">
                            </div> -->
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>STATE</b></p>
                                <select name="customerState" class="form-control inputfield custom-select"
                                    style="background-color: #f0f0f0; border-radius: 5px; border: 1px solid #dcdcdc; padding: 8px 12px; font-size: 14px;" required>
                                    
                                    <option value="" disabled <?php echo empty($data['state']) ? 'selected' : ''; ?>>-- Select State --</option>
                                    <option value="Johor" <?php echo ($data['state'] == 'Johor') ? 'selected' : ''; ?>>Johor</option>
                                    <option value="Kedah" <?php echo ($data['state'] == 'Kedah') ? 'selected' : ''; ?>>Kedah</option>
                                    <option value="Kelantan" <?php echo ($data['state'] == 'Kelantan') ? 'selected' : ''; ?>>Kelantan</option>
                                    <option value="Melaka" <?php echo ($data['state'] == 'Melaka') ? 'selected' : ''; ?>>Melaka</option>
                                    <option value="Negeri Sembilan" <?php echo ($data['state'] == 'Negeri Sembilan') ? 'selected' : ''; ?>>Negeri Sembilan</option>
                                    <option value="Pahang" <?php echo ($data['state'] == 'Pahang') ? 'selected' : ''; ?>>Pahang</option>
                                    <option value="Perak" <?php echo ($data['state'] == 'Perak') ? 'selected' : ''; ?>>Perak</option>
                                    <option value="Perlis" <?php echo ($data['state'] == 'Perlis') ? 'selected' : ''; ?>>Perlis</option>
                                    <option value="Pulau Pinang" <?php echo ($data['state'] == 'Pulau Pinang') ? 'selected' : ''; ?>>Pulau Pinang</option>
                                    <option value="Sabah" <?php echo ($data['state'] == 'Sabah') ? 'selected' : ''; ?>>Sabah</option>
                                    <option value="Sarawak" <?php echo ($data['state'] == 'Sarawak') ? 'selected' : ''; ?>>Sarawak</option>
                                    <option value="Selangor" <?php echo ($data['state'] == 'Selangor') ? 'selected' : ''; ?>>Selangor</option>
                                    <option value="Terengganu" <?php echo ($data['state'] == 'Terengganu') ? 'selected' : ''; ?>>Terengganu</option>
                                    <option value="Kuala Lumpur" <?php echo ($data['state'] == 'Kuala Lumpur') ? 'selected' : ''; ?>>Kuala Lumpur</option>
                                    <option value="Labuan" <?php echo ($data['state'] == 'Labuan') ? 'selected' : ''; ?>>Labuan</option>
                                    <option value="Putrajaya" <?php echo ($data['state'] == 'Putrajaya') ? 'selected' : ''; ?>>Putrajaya</option>
                                </select>
                            </div>

                        </div>

                        <div class="col-12 text-center" style="margin-top: 20px;">
                            <input type="hidden" name="id" value="<?php echo $id ?>">
                            <button type="submit" class="btn btn-submit" id="btn-submit">Submit</button>
                        </div>

                    </div>
                </div>
            </form>


        </div>
    </div>

    <script>
        document.getElementById('customerProfilePicUpload').addEventListener('change', function(event) {
            const fileInput = event.target;
            const file = fileInput.files[0];
            const preview = document.getElementById('customerProfilePicPreview');

            // Check if file is selected
            if (file && (file.type === "image/png" || file.type === "image/jpeg" || file.type === "image/gif" || file.type === "image/jpg")) {
                const reader = new FileReader();

                reader.onload = function(e) {
                    preview.src = e.target.result; // Set the image source to the uploaded image
                    preview.style.display = 'block'; // Show the image preview
                }

                reader.readAsDataURL(file); // Convert the file to a base64 string
            } else {
                // Reset to default image if no valid image file is selected
                // preview.src = "default-profile.png"; 
                preview.src = "../images/default_admin.jpeg";
            }
        });
    </script>

    <!-- Script for password confirmation validation -->
    <script>
        document.querySelector('form').addEventListener('submit', function(event) {
            const password = document.getElementById('customerPassword').value;
            const confirmPassword = document.getElementById('customerPasswordConfirm').value;
            const errorMessage = document.getElementById('passwordError');
            
            if (password !== confirmPassword) {
                errorMessage.style.display = 'block'; // Show the error message
                event.preventDefault(); // Prevent form submission
            } else {
                errorMessage.style.display = 'none'; // Hide the error message
            }
        });
    </script>
    
</body>
</html>