package com.goodfriend.backend.exception

import com.goodfriend.backend.response.ErrorResponse
import io.jsonwebtoken.JwtException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartException
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

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val message = "参数 '${ex.name}' 类型错误，应为 ${ex.requiredType?.simpleName}"
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(message))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val message = ex.constraintViolations.firstOrNull()?.message ?: "请求参数校验失败"
        return ResponseEntity.badRequest().body(ErrorResponse(message))
    }



    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val firstError = ex.bindingResult.fieldErrors.firstOrNull()
        val message = firstError?.defaultMessage ?: "请求参数校验失败"
        return ResponseEntity.badRequest().body(ErrorResponse(message))
    }



    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val message = when (val cause = ex.cause) {
            null -> "请求体缺失"
            is com.fasterxml.jackson.core.JsonParseException -> "请求体不是合法的 JSON 格式"
            is com.fasterxml.jackson.databind.exc.MismatchedInputException -> {
                val path = cause.path.joinToString(".") { it.fieldName ?: "[unknown]" }
                if (path.isNotBlank())
                    "字段 '$path' 缺失或类型不匹配"
                else
                    "请求字段缺失或类型不匹配"
            }
            else -> "请求体解析失败，请检查格式和字段"
        }

        return ResponseEntity.badRequest().body(ErrorResponse(message))
    }



    @ExceptionHandler(JwtException::class)
    fun handleJwtException(ex: JwtException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(ErrorResponse("Token 解析失败"))
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleStaticResourceNotFound(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(404).body(ErrorResponse("资源未找到: ${ex.resourcePath}"))
    }


    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceeded(ex: MaxUploadSizeExceededException): ResponseEntity<ErrorResponse> {
        val max = ex.maxUploadSize  // 可能为 -1 或 null（取决于容器/配置）
        val message = if (max > 0) {
            val mb = (max + (1024 * 1024 - 1)) / (1024 * 1024) // 向上取整到 MB
            "上传文件大小超出限制：最大 ${mb}MB"
        } else {
            "上传文件大小超出限制"
        }
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ErrorResponse(message))
    }

    @ExceptionHandler(MultipartException::class)
    fun handleMultipart(ex: MultipartException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("文件上传解析失败，请检查表单格式与文件大小"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception): ResponseEntity<ErrorResponse> {
        ex.printStackTrace()
        return ResponseEntity.status(500).body(ErrorResponse("服务器内部错误"))
    }
}
