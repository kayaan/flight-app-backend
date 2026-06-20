package com.flightapp.backend.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        ex: ResponseStatusException
    ): ResponseEntity<ApiErrorResponse> {
        val status = HttpStatus.valueOf(ex.statusCode.value())

        return ResponseEntity
            .status(status)
            .body(
                ApiErrorResponse(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = ex.reason
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ApiErrorResponse> {
        val status = HttpStatus.BAD_REQUEST

        val message = ex.bindingResult
            .fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }

        return ResponseEntity
            .status(status)
            .body(
                ApiErrorResponse(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException
    ): ResponseEntity<ApiErrorResponse> {
        val status = HttpStatus.BAD_REQUEST

        return ResponseEntity
            .status(status)
            .body(
                ApiErrorResponse(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = ex.message
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception
    ): ResponseEntity<ApiErrorResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR

        return ResponseEntity
            .status(status)
            .body(
                ApiErrorResponse(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = "Unexpected server error"
                )
            )
    }
}