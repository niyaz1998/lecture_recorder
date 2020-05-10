package com.example.lecturerecorder.model

import com.google.gson.annotations.SerializedName

data class AuthCredentials (
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

data class RegisterResponse (
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class LoginResponse (
    @SerializedName("code")
    val code: Int,
    @SerializedName("expire")
    val expire: String,
    @SerializedName("token")
    val token: String
)

data class ErrorResponse (
    @SerializedName("message")
    val message: String
)