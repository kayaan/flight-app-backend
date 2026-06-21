package com.flightapp.backend.auth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class AuthController {

    @GetMapping("/api/auth/login/google")
    fun loginWithGoogle(): String {
        return "redirect:/oauth2/authorization/google"
    }

    @PostMapping("/api/auth/logout")
    @ResponseBody
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val authentication = SecurityContextHolder.getContext().authentication

        SecurityContextLogoutHandler().logout(
            request,
            response,
            authentication
        )
    }
}