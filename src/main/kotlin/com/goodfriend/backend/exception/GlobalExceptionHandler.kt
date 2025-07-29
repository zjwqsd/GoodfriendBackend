package com.goodfriend.backend.exception

import com.goodfriend.backend.response.ErrorResponse
import io.jsonwebtoken.JwtException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.resource.NoResourceFoundException

class ApiException(
    val status: Int = 400,             // HTTP 状态码，如 400/403/404
    override val message: String       // 返回给用户看的提示信息
) : RuntimeException(message)

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ex.status).body(ErrorResponse(ex.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val msg = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "参数校验失败"
        return ResponseEntity.badRequest().body(ErrorResponse(msg))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val msg = ex.constraintViolations.firstOrNull()?.message ?: "参数不合法"
        return ResponseEntity.badRequest().body(ErrorResponse(msg))
    }

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(ex: JwtException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(ErrorResponse("Token 解析失败"))
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleStaticResourceNotFound(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(404).body(ErrorResponse("资源未找到: ${ex.resourcePath}"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception): ResponseEntity<ErrorResponse> {
        ex.printStackTrace()
        return ResponseEntity.status(500).body(ErrorResponse("服务器内部错误"))
    }
}
