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
$retrieve = $db->retrieve("admin/$id");
$data = json_decode($retrieve, 1);

// Check if the admin exists
if (!$data) {
    $_SESSION['error'] = "Admin not found.";
    header("Location: admin.php");
    exit();
}

?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Admin</title>
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
                    <a href="admin.php" class="btn add-button" id="add-button">< &nbsp Back</a>
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

            <form action="../actions/action_editAdmin.php" method="post" enctype="multipart/form-data">
                <div class="row formframe">
                    <div class="col-12">
                        <p class="bigtitle"><b>Edit Admin</b></p>
                        <p class="formtitle"><b>Personal Informatiom</b></p>
                    </div>

                    <div class="col-12">
                        <hr class="line">
                    </div>

                    <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                        <!-- <p class="formtitle"><b>General Informatiom</b></p> -->
                        <p class="formtitledescription">Update the admin personal details</p>
                    </div>

                    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                                <p class="col-12 title" style="padding-left: 0;"><b>ADMIN PROFILE PIC</b></p>
                            </div>  
                            <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                                <!-- Image preview element -->
                                <?php if (!empty($data['adminProfilePic'])): ?>
                                    <!-- Display the current icon -->
                                    <img id="adminProfilePicPreview" src="data:image/png;base64,<?php echo $data['adminProfilePic'] ?>" alt="Admin Profile Pic" style="margin-top: 10px; max-width: 100px;">
                                <?php else: ?>
                                    <img id="adminProfilePicPreview" src="../images/default_admin.jpeg" alt="Profile Pic Preview" style="display: block; margin-top: 10px; max-width: 100px; border-radius: 50%; margin-bottom: 10px;">
                                <?php endif; ?>
                                
                                <input type="file" name="adminProfilePic" class="form-control inputfield" accept="image/png, image/jpeg, image/gif, image/jpg" id="adminProfilePicUpload" >
                            </div>
                        </div>

                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                                <p class="col-12 title" style="padding-left: 0;"><b>ADMIN NAME</b></p>
                                <input type="text" name="adminName" class="form-control inputfield" placeholder="Enter Admin Name" value="<?php echo $data['adminName'] ?>" required>
                            </div>
                            <!-- <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                                <p class="col-12 title" style="padding-left: 0;"><b>DEPARTMENT</b></p>

                            </div> -->
                        </div>

                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>ADMIN PHONE NO.</b></p>
                                <input type="text" name="adminPhoneNo" class="form-control inputfield" placeholder="Example: 0123456789" value="<?php echo $data['adminPhoneNo'] ?>" required
                                pattern="^0[1-9][0-9]{7,9}$" title="Please enter a valid Malaysian phone number (e.g., 0123456789 or +60123456789)">
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>ADMIN EMAIL (use as username to login)</b></p>
                                <input type="email" name="adminEmail" class="form-control inputfield" placeholder="Enter Admin Email" value="<?php echo $data['adminEmail'] ?>" required>
                            </div>
                        </div>

                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>PASSWORD</b></p>
                                <input type="password" name="adminPassword" id="adminPassword" class="form-control inputfield" placeholder="Enter Password" value="<?php echo $data['adminPassword'] ?>" required
                                    pattern="(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}" 
                                    title="Password must be at least 8 characters long and include at least one uppercase letter, one number, and one special character.">
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="col-12 title" style="padding-left: 0;"><b>PASSWORD CONFIRM</b></p>
                                <input type="password" name="adminPasswordConfirm" id="adminPasswordConfirm" class="form-control inputfield" placeholder="Confirm Password" value="<?php echo $data['adminPassword'] ?>" required>
                                <!-- Error message placeholder -->
                                <span id="passwordError" style="color: red; display: none;">Passwords do not match. Please try again.</span>
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
        document.getElementById('adminProfilePicUpload').addEventListener('change', function(event) {
            const fileInput = event.target;
            const file = fileInput.files[0];
            const preview = document.getElementById('adminProfilePicPreview');

            // Check if file is selected and has a valid image type
            if (file && (file.type === "image/png" || file.type === "image/jpeg" || file.type === "image/jpg" || file.type === "image/gif")) {
                const reader = new FileReader();

                reader.onload = function(e) {
                    preview.src = e.target.result; // Set the image source to the uploaded image
                    preview.style.display = 'block'; // Show the image preview
                }

                reader.readAsDataURL(file); // Convert the file to a base64 string
            } else {
                // Reset to default image if no valid image file is selected
                preview.src = "../images/default_admin.jpeg";
            }
        });
    </script>

    <!-- Script for password confirmation validation -->
    <script>
        document.querySelector('form').addEventListener('submit', function(event) {
            const password = document.getElementById('adminPassword').value;
            const confirmPassword = document.getElementById('adminPasswordConfirm').value;
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