package com.controlled_feed.backend.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String)
class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException::class)
    fun handleException(ex: RuntimeException): ResponseEntity<ErrorResponse>
    {
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?:"Something went wrong"
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleException(ex: NoSuchElementException): ResponseEntity<ErrorResponse>
    {
        val error = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message=ex.message?:"Resource not found"
        )
        return ResponseEntity.badRequest().body(error)
    }
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse>
    {
        val message =ex.bindingResult.fieldErrors.joinToString(", ") {"${it.field}: ${it.defaultMessage}"  }
        val error = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation error",
            message = message
        )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse>{
        val error = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message?:"An unexpected error occurred"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(error)
    }
}