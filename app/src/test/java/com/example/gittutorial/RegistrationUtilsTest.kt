package com.example.gittutorial

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RegistrationUtilsTest{
    @Test
    fun `empty username return false`(){
        val result = RegistrationUtils.validateRegistrationInput(
            "",
            "123456789",
            "123456789"
        )
        assertThat(result).isFalse()
    }
    @Test
    fun `valid username and correctly repeated password returns true`(){
        val result = RegistrationUtils.validateRegistrationInput(
            "Shehab",
            "123456789",
            "123456789"
        )
        assertThat(result).isTrue()
    }

    @Test
    fun `username already exists return false`(){
        val result = RegistrationUtils.validateRegistrationInput(
            "Peter",
            "123456789",
            "123456789"
        )
        assertThat(result).isFalse()
    }
    // empty password
    @Test
    fun `empty password return false`(){
        val result = RegistrationUtils.validateRegistrationInput(
            "Shehab",
            "",
            ""
        )
        assertThat(result).isFalse()
    }
    // the password reqeated incorreclty
    @Test
    fun `password repeated incorrectly return false`(){
        val result = RegistrationUtils.validateRegistrationInput(
            "Shehab",
            "123456789",
            "sadfklsa"
        )
        assertThat(result).isFalse()
    }
    //password contains less thant 2 digits
    @Test
    fun `password less than two digits return false`(){
        val result = RegistrationUtils.validateRegistrationInput(
            "",
            "123456789",
            "123456789"
        )
        assertThat(result).isFalse()
    }
}