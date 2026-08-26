package com.securetrace.app.util

/** Minimal in-memory state to carry context into the OTP verification screen. */
object PendingOtp {
    var email: String = ""
    var purpose: String = "" // "register" or "login"
    var requireAdmin: Boolean = false

    fun clear() {
        email = ""
        purpose = ""
        requireAdmin = false
    }
}
