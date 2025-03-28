<?php
// Turn on error reporting
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// Start the session at the beginning of the file
session_start();

// Check if the cookies are set
$saved_email = isset($_COOKIE['email']) ? $_COOKIE['email'] : '';
$saved_password = isset($_COOKIE['password']) ? $_COOKIE['password'] : '';
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <style>
        <?php include "../css/login.css" ?>
    </style>
</head>
<body>
    <div class="overlay"></div>
    <div class="login-container">
        <div class="login-box">
            <img src="../images/login_image.jpeg" alt="Login Image">
            <div class="login-form">
                <h2 style="margin-bottom: 15px;">AuraDecor.</h2>
                <h3>Login</h3>
                
                <form method="post" action="../actions/action_login.php">
                    <input type="text" name="email" placeholder="Email" value="<?php echo htmlspecialchars($saved_email); ?>" required>
                    <input type="password" name="password" placeholder="Password" value="<?php echo htmlspecialchars($saved_password); ?>" required>
                    <div class="remember-me">
                        <!-- <input type="checkbox" name="remember_me" id="remember_me" <?php echo $saved_email ? 'checked' : ''; ?>> -->
                        <!-- <label for="remember_me">Remember Me</label> -->
                    </div>
                    <input type="submit" value="Login">
                </form>

                <!-- Display error messages if set -->
                <?php if (isset($_SESSION['error_message'])): ?>
                    <div class="error-message"><?php echo $_SESSION['error_message']; unset($_SESSION['error_message']); ?></div>
                <?php endif; ?>
            </div>
        </div>
    </div>
</body>
</html>