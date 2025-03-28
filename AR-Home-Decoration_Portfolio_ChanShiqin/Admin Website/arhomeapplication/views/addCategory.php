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
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Category</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://kit.fontawesome.com/1dd4ddab77.js" crossorigin="anonymous"></script>
    <style>
        <?php include "../css/addCategory.css" ?>
    </style>
</head>
<body>
    <?php include 'navigationBar.php'; ?>
    <div class="content">
        <div class="content-box">
            <div class="row filterandaddbutton">
                <div class="col-xs-4 col-sm-6 col-md-9 col-lg-2">
                    <a href="category.php" class="btn add-button" id="add-button">< &nbsp Back</a>
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

            <form action="../actions/action_addCategory.php" method="post" enctype="multipart/form-data">
                <div class="row formframe">
                    <div class="col-12">
                        <p class="bigtitle"><b>Add New Category</b></p>
                        <p class="formtitle"><b>General Informatiom</b></p>
                    </div>

                    <div class="col-12">
                        <hr class="line">
                    </div>

                    <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                        <!-- <p class="formtitle"><b>General Informatiom</b></p> -->
                        <p class="formtitledescription">Fill up the new category details</p>
                    </div>

                    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                                <p class="col-12 title" style="padding-left: 0;"><b>CATEGORY NAME</b></p>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                                <input type="text" name="categoryName" class="form-control inputfield" placeholder="Enter Category Name" required>
                            </div>
                        </div>
                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                <p class="title" style="padding-left: 0;"><b>CATEGORY DESCRIPTION</b></p>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                <textarea  name="categoryDescription" class="form-control inputfield" rows="2" placeholder="Enter Category Description"></textarea>
                            </div>
                        </div>
                        <!-- <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                                <p class="col-12 title" style="padding-left: 0;"><b>STATUS</b> &nbsp; [ you can change it anytime ]</p> 
                                <p class="col-12 title" style="padding-left: 0; padding-top: 0;"></p>
                            </div>
                            <div class="col-xs-12 col-sm-8 col-md-6 col-lg-6">
                                <label class="switch">
                                    <input type="checkbox" id="statusToggle" name="categoryStatus" value="Inactive">
                                    <span class="slider"></span>
                                </label>
                            </div>
                        </div> -->
                    </div>

                    <div class="col-12" style="margin-top: 20px;">
                        <p class="formtitle"><b>Upload Icon</b></p>
                    </div>

                    <div class="col-12">
                        <hr class="line">
                    </div>

                    <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                        <p class="formtitledescription">Upload new category icon, file type only allowed PNG.</p>
                    </div>

                    <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                        <!-- Image preview element -->
                        <img id="iconPreview" src="#" alt="Icon Preview" style="display: none; margin-top: 10px; max-width: 100px;">
                        
                        <input type="file" name="categoryIcon" class="form-control inputfield" accept=".png" id="categoryIconUpload" required>
                        <small class="form-text text-muted">Only PNG files are allowed.</small>
                    </div>

                    <div class="col-12 text-center" style="margin-top: 20px;">
                        <button type="submit" class="btn btn-submit" id="btn-submit">Submit</button>
                    </div>

                </div>
            </form>

        </div>
    </div>

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

    <!-- JavaScript to Toggle Status Text -->
    <script>
        document.getElementById('statusToggle').addEventListener('change', function() {
            var statusText = document.getElementById('statusText');
            if (this.checked) {
                // statusText.textContent = 'Active';
                this.value = 'Active'; // Update input value for form submission
            } else {
                // statusText.textContent = 'Inactive';
                this.value = 'Inactive'; // Update input value for form submission
            }
        });

        // Set default to "Inactive"
        window.onload = function() {
            document.getElementById('statusToggle').value = 'Inactive'; // Default value
        };
    </script>

    <script>
        document.getElementById('categoryIconUpload').addEventListener('change', function(event) {
            const fileInput = event.target;
            const file = fileInput.files[0];
            const preview = document.getElementById('iconPreview');

            if (file && file.type === "image/png") {
                const reader = new FileReader();

                reader.onload = function(e) {
                    preview.src = e.target.result; // Set image source to the base64 URL
                    preview.style.display = 'block'; // Show the image preview
                }

                reader.readAsDataURL(file); // Convert the file to a base64 string
            } else {
                preview.src = "#";
                preview.style.display = 'none'; // Hide the image preview if the file is invalid
            }
        });
    </script>

</body>
</html>