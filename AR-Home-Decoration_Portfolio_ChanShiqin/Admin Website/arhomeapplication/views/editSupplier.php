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
$retrieve = $db->retrieve("supplier/$id");
$data = json_decode($retrieve, 1);

// Check if the supplier exists
if (!$data) {
    $_SESSION['error'] = "Supplier not found.";
    header("Location: supplier.php");
    exit();
}

// Helper function to display a value or '-' if empty
function displayValue($value) {
    return !empty($value) ? htmlspecialchars($value) : '-';
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Supplier</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
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
            </div>

            <div class="row">
                <?php if (isset($_SESSION['error'])): ?>
                    <div id="errorAlert" class="col-12 alert alert-danger alert-dismissible fade show" role="alert">
                        <?php echo htmlspecialchars($_SESSION['error']); ?>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    <?php unset($_SESSION['error']); ?>
                <?php endif; ?>
            </div>

            <form action="../actions/action_editSupplier.php" method="post" enctype="multipart/form-data">
            <div class="row formframe">
                <div class="col-12">
                    <p class="bigtitle"><b>Edit Supplier</b></p>
                    <p class="formtitle"><b>Supplier Company Information</b></p>
                </div>

                <div class="col-12">
                    <hr class="line">
                </div>

                <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                    <p class="formtitledescription">Update the supplier company details</p>
                </div>

                <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                            <p class="col-12 title" style="padding-left: 0;"><b>SUPPLIER COMPANY NAME</b></p>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                            <input type="text" name="supplierCompanyName" class="form-control inputfield" value="<?php echo displayValue($data['supplierCompanyName']) ?>" required disabled>
                        </div>
                    </div>

                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                            <p class="col-12 title" style="padding-left: 0;"><b>COMPANY PHONE NO.</b></p>
                            <input type="text" name="supplierCompanyPhoneNo" class="form-control inputfield" value="<?php echo displayValue($data['supplierCompanyPhoneNo']) ?>" required
                                pattern="^0[1-9][0-9]{7,9}$" title="Please enter a valid Malaysian phone number">
                        </div>

                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                            <p class="col-12 title" style="padding-left: 0;"><b>COMPANY EMAIL</b></p>
                            <input type="email" name="supplierCompanyEmail" class="form-control inputfield" value="<?php echo displayValue($data['supplierCompanyEmail']) ?>">
                        </div>
                    </div>

                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                            <p class="title" style="padding-left: 0;"><b>COMPANY ADDRESS</b></p>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                            <textarea name="supplierAddress" class="form-control inputfield" rows="2"><?php echo displayValue($data['supplierAddress']) ?></textarea>
                        </div>
                    </div>
                </div>

                <div class="col-12">
                    <p class="formtitle"><b>Person Incharge (PIC) Information</b></p>
                </div>

                <div class="col-12">
                    <hr class="line">
                </div>

                <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
                    <p class="formtitledescription">Update the supplier PIC details</p>
                </div>

                <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                            <p class="col-12 title" style="padding-left: 0;"><b>PERSON INCHARGE NAME</b></p>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-8">
                            <input type="text" name="supplierPICName" class="form-control inputfield" value="<?php echo displayValue($data['supplierPICName']) ?>" required>
                        </div>
                    </div>

                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                            <p class="col-12 title" style="padding-left: 0;"><b>PIC PHONE NO.</b></p>
                            <input type="text" name="supplierPICPhoneNo" class="form-control inputfield" value="<?php echo displayValue($data['supplierPICPhoneNo']) ?>" required
                                pattern="^0[1-9][0-9]{7,9}$" title="Please enter a valid Malaysian phone number">
                        </div>

                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                            <p class="col-12 title" style="padding-left: 0;"><b>PIC EMAIL</b></p>
                            <input type="email" name="supplierPICEmail" class="form-control inputfield" value="<?php echo displayValue($data['supplierPICEmail']) ?>">
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
                    <p class="formtitledescription">Fill up the info of the supplier</p>
                </div>
                
                <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">

                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-8 col-sm-8 col-md-6 col-lg-4">
                            <p class="title"><b>CONTRACT</b></p>
                            <div class="contract-options">
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input custom-radio" type="radio" name="hasContract" id="contractYes" value="yes" <?php echo ($data['hasContract'] === 'yes') ? 'checked' : '' ?> onchange="toggleContractDuration(true)">
                                    <label class="form-check-label" for="contractYes" style="font-size: 14px;">Yes</label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input custom-radio" type="radio" name="hasContract" id="contractNo" value="no" <?php echo ($data['hasContract'] === 'no') ? 'checked' : '' ?> onchange="toggleContractDuration(false)">
                                    <label class="form-check-label" for="contractNo" style="font-size: 14px;">No</label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div id="contractDurationFields" style="<?php echo ($data['hasContract'] === 'yes') ? 'display: block;' : 'display: none;'; ?>">
                        <div class="row" style="margin-bottom: 10px;">
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="title" style="padding-left: 0;"><b>CONTRACT START DATE</b></p>
                                <input type="text" name="supplierContractStartDate" class="form-control inputfield" value="<?php echo displayValue($data['supplierContractStartDate']) ?>" id="contractStartDate" placeholder="Select start date" required>
                            </div>

                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                                <p class="title" style="padding-left: 0;"><b>CONTRACT END DATE</b></p>
                                <input type="text" name="supplierContractEndDate" class="form-control inputfield" value="<?php echo displayValue($data['supplierContractEndDate']) ?>" id="contractEndDate" placeholder="Select end date" required>
                            </div>
                        </div>
                    </div>

                    <div class="row" style="margin-bottom: 10px;">
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                            <p class="title" style="padding-left: 0;"><b>REMARKS (Optional)</b></p>
                            <textarea name="supplierRemarks" class="form-control inputfield" rows="2"><?php echo displayValue($data['supplierRemarks']) ?></textarea>
                        </div>
                    </div>
                </div>
                
                <div class="col-12 text-center" style="margin-top: 20px;">
                        <input type="hidden" name="id" value="<?php echo $id ?>">
                        <button type="submit" class="btn btn-submit" id="btn-submit" style="width: auto;">Save Changes</button>
                    </div>
            </div>
            </form>
        </div>
    </div>

    <script>
        // Initialize Flatpickr for date inputs
        flatpickr("#contractStartDate", {
            dateFormat: "Y-m-d"
        });

        flatpickr("#contractEndDate", {
            dateFormat: "Y-m-d"
        });

        function toggleContractDuration(isContract) {
            document.getElementById('contractDurationFields').style.display = isContract ? 'block' : 'none';
        }
    </script>
</body>
</html>
