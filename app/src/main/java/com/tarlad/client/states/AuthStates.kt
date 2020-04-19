package com.tarlad.client.states

enum class AuthState {
    Login, Register, ForgetPass
}

enum class RegisterEmail {
    Empty, Ok, Error, Loading
}