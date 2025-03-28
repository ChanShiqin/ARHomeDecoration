package com.example.arhomedecorationapplication

 import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.Response
 import retrofit2.http.Field
 import retrofit2.http.FormUrlEncoded
 import retrofit2.http.POST

interface ApiInterface {

    @GET("index.php")
    suspend fun fetchPaymentDetails() : Response<PaymentDetailsResponse>

    @FormUrlEncoded
    @POST("index.php")  // Ensure the correct endpoint is used
    suspend fun fetchPaymentDetails(
        @Field("name") userName: String,
        @Field("email") userEmail: String,
        @Field("amount") amount: Int
    ): Response<PaymentDetailsResponse>
}


//    @GET("/posts")
//    suspend fun getPosts() : List <Posts>

//    @POST("")
//    suspend fun setPosts(@Body post: Posts)
