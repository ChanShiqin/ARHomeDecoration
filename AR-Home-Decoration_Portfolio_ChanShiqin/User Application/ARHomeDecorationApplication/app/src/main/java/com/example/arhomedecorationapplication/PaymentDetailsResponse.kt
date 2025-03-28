package com.example.arhomedecorationapplication

data class PaymentDetailsResponse(
    val customer: String,
    val ephemeralKey: String,
    val paymentIntent: String,
    val publishableKey: String
)