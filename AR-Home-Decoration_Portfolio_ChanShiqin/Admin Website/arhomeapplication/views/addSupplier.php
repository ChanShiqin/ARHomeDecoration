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
    <title>Add Supplier</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
    <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
    <script src="https://kit.fontawesome.com/1dd4ddab77.js" crossorigin="anonymous"></script>
    <style>
        <?php include "../css/addSupplier.css" ?>
    </style>
</head>
<body>
    <?php include 'navigationBar.php'; ?>
    <div class="content">
        <div class="content-box">
            <div class="row filterandaddbutton">
                <div class="col-xs-4 col-sm-6 col-md-9 col-lg-2">
                    <a href="supplier.php" class="btn add-button" id="add-button">< &nbsp Back</a>
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

            <form action="../actions/action_addSupplier.php" method="post" enctype="multipart/form-data">
            <div class="row formframe">
                <div class="col-12">
                    <p class="bigtitle"><b>Add New Supplier</b></p>
                    <p class="formtitle"><b>Supplier Company Informatiom</b></p>
                </div>

                <div class="col-12">
                    <hr class="line">
                </div>

                <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                    <!-- <p class="formtitle"><b>General Informatiom</b></p> -->
                    <p class="formtitledescription">Fill up the new supplier company details</p>
                </div>

                <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                            <p class="col-12 title" style="padding-left: 0;"><b>SUPPLIER COMPANY NAME</b></p>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                            <input type="text" name="supplierCompanyName" class="form-control inputfield" placeholder="Enter Company Name" required>
                        </div>
                    </div>

                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                            <p class="col-12 title" style="padding-left: 0;"><b>COMPANY PHONE NO.</b></p>
                            <input type="text" name="supplierCompanyPhoneNo" class="form-control inputfield" placeholder="Example: 0123456789" required
                                pattern="^0[1-9][0-9]{7,9}$" title="Please enter a valid Malaysian phone number (e.g., 0123456789 or +60123456789)">
                        </div>

                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                            <p class="col-12 title" style="padding-left: 0;"><b>COMPANY EMAIL</b></p>
                            <input type="email" name="supplierCompanyEmail" class="form-control inputfield" placeholder="Enter Company Email" >
                        </div>
                    </div>

                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                            <p class="title" style="padding-left: 0;"><b>COMPANY ADDRESS</b></p>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                            <textarea  name="supplierAddress" class="form-control inputfield" rows="2" placeholder="Enter Supplier Address"></textarea>
                        </div>
                    </div>

                </div>

                <div class="col-12">
                    <p class="formtitle"><b>Person Incharge (PIC) Informatiom</b></p>
                </div>

                <div class="col-12">
                    <hr class="line">
                </div>

                <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                    <!-- <p class="formtitle"><b>General Informatiom</b></p> -->
                    <p class="formtitledescription">Fill up the supplier PIC details</p>
                </div>

                <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                            <p class="col-12 title" style="padding-left: 0;"><b>PERSON INCHARGE NAME</b></p>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                            <input type="text" name="supplierPICName" class="form-control inputfield" placeholder="Enter PIC Name" required>
                        </div>
                    </div>

                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                            <p class="col-12 title" style="padding-left: 0;"><b>PIC PHONE NO.</b></p>
                            <input type="text" name="supplierPICPhoneNo" class="form-control inputfield" placeholder="Example: 0123456789" required
                                pattern="^0[1-9][0-9]{7,9}$" title="Please enter a valid Malaysian phone number (e.g., 0123456789 or +60123456789)">
                        </div>

                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                            <p class="col-12 title" style="padding-left: 0;"><b>PIC EMAIL</b></p>
                            <input type="email" name="supplierPICEmail" class="form-control inputfield" placeholder="Enter PIC Email">
                        </div>
                    </div>
                </div>

                <div class="col-12">
                    <p class="formtitle"><b>General Information</b></p>
                </div>

                <div class="col-12">
                    <hr class="line">
                </div>

                <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                    <!-- <p class="formtitle"><b>General Informatiom</b></p> -->
                    <p class="formtitledescription">Fill up the info of the supplier</p>
                </div>
                
                <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">

                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-8 col-sm-8  col-md-6 col-lg-4">
                            <p class="title"><b>CONTRACT</b></p>
                            <div class="contract-options">
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input custom-radio" type="radio" name="hasContract" id="contractYes" value="yes" onchange="toggleContractDuration(true)" required>
                                    <label class="form-check-label" for="contractYes" style="font-size: 14px;">Yes</label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input custom-radio" type="radio" name="hasContract" id="contractNo" value="no" onchange="toggleContractDuration(false)" required>
                                    <label class="form-check-label" for="contractNo" style="font-size: 14px;">No</label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row" id="contractDuration" style="display: none; margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                            <p class="title"><b>CONTRACT START DATE</b></p>
                            <input type="text" id="startDatePicker" name="supplierContractStartDate" class="form-control inputfield" placeholder="Select Start Date">
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                            <p class="title"><b>CONTRACT END DATE</b></p>
                            <input type="text" id="endDatePicker" name="supplierContractEndDate" class="form-control inputfield" placeholder="Select End Date">
                        </div>
                    </div>

                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                            <p class="title" style="padding-left: 0;"><b>REMARK (Optional)</b></p>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                            <textarea  name="supplierRemark" class="form-control inputfield" rows="2" placeholder="Enter Supplier Remark"></textarea>
                        </div>
                    </div>
                </div>

                <div class="col-12 text-center" style="margin-top: 20px;">
                    <button type="submit" class="btn btn-submit" id="btn-submit">Submit</button>
                </div>

            </div>

        </div>
    </div>

    <script>
        flatpickr("#datePicker", {
            dateFormat: "Y-m-d", // Customize the date format
            altInput: true,      // Shows a prettier input field
            altFormat: "F j, Y", // Displays full month and day name
            allowInput: true,    // Allow user input
            minDate: "today"     // Prevent selecting past dates
        });

        // Initialize Flatpickr for the start date picker
        const startDatePicker = flatpickr("#startDatePicker", {
            dateFormat: "Y-m-d",  // Format the date
            altInput: true,       // Prettier input
            altFormat: "F j, Y",  // Display full month and day name
            allowInput: true,     // Allow manual input
            onChange: function(selectedDates) {
            // When the start date changes, update the end date picker's minimum date
            let startDate = selectedDates[0];
            endDatePicker.set('minDate', startDate); // Set minimum end date to start date
            }
        });

        // Initialize Flatpickr for the end date picker
        const endDatePicker = flatpickr("#endDatePicker", {
            dateFormat: "Y-m-d",  // Format the date
            altInput: true,       // Prettier input
            altFormat: "F j, Y",  // Display full month and day name
            allowInput: true,     // Allow manual input
            minDate: null,        // Initially, no minimum date (will be set based on start date)
        });

        function toggleContractDuration(show) {
            const contractDuration = document.getElementById('contractDuration');
            if (show) {
                contractDuration.style.display = 'flex'; // Show fields as a row
            } else {
                contractDuration.style.display = 'none'; // Hide the fields
            }
        }
    </script>

</body>
</html>