package com.goodfriend.backend.exception

import com.goodfriend.backend.response.ErrorResponse
import io.jsonwebtoken.JwtException
import jakarta.validation.ConstraintViolationException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartException
import org.springframework.web.servlet.resource.NoResourceFoundException

class ApiException(
    val status: Int = 400,
    override val message: String
) : RuntimeException(message)

/** 404 */
class AppointmentNotFoundOrInvisibleException : RuntimeException()

/** 409 */
class AppointmentStateConflictException(
    message: String = "Appointment state conflict"
) : RuntimeException(message)

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandler {

    /* ---------- 业务语义类 ---------- */

    @ExceptionHandler(AppointmentNotFoundOrInvisibleException::class)
    fun notFound(): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse("资源不存在"))

    @ExceptionHandler(AppointmentStateConflictException::class)
    fun conflict(ex: AppointmentStateConflictException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse(ex.message ?: "状态冲突"))

    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(ex.status).body(ErrorResponse(ex.message))

    /* ---------- 校验失败：统一 422 ---------- */

    @ExceptionHandler(value = [
        MethodArgumentNotValidException::class, // @RequestBody + @Valid
        BindException::class,                   // 表单/查询对象 + @Valid
        ConstraintViolationException::class     // 直接注解在 @RequestParam/@PathVariable
    ])
    fun handleValidationErrors(ex: Exception): ResponseEntity<ErrorResponse> {
        val message = when (ex) {
            is MethodArgumentNotValidException ->
                ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "请求参数校验失败"
            is BindException ->
                ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "请求参数校验失败"
            is ConstraintViolationException ->
                ex.constraintViolations.firstOrNull()?.message ?: "请求参数校验失败"
            else -> "请求参数校验失败"
        }
        return ResponseEntity.status(422).body(ErrorResponse(message))
    }

    /* ---------- 解析/类型问题：统一 400 ---------- */

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val message = when (val cause = ex.cause) {
            null -> "请求体缺失"
            is com.fasterxml.jackson.core.JsonParseException -> "请求体不是合法的 JSON 格式"
            is com.fasterxml.jackson.databind.exc.MismatchedInputException -> {
                val path = cause.path.joinToString(".") { it.fieldName ?: "[unknown]" }
                if (path.isNotBlank()) "字段 '$path' 缺失或类型不匹配" else "请求字段缺失或类型不匹配"
            }
            else -> "请求体解析失败，请检查格式和字段"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(message))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val message = "参数 '${ex.name}' 类型错误，应为 ${ex.requiredType?.simpleName}"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(message))
    }

    /* ---------- 认证/鉴权 ---------- */

    @ExceptionHandler(org.springframework.security.core.AuthenticationException::class, JwtException::class)
    fun handleAuth(@Suppress("UNUSED_PARAMETER") ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse("未认证或 Token 非法"))

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDenied(@Suppress("UNUSED_PARAMETER") ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse("无权访问"))

    /* ---------- 其它 ---------- */

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleStaticResourceNotFound(ex: NoResourceFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse("资源未找到: ${ex.resourcePath}"))

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceeded(ex: MaxUploadSizeExceededException): ResponseEntity<ErrorResponse> {
        val max = ex.maxUploadSize
        val message = if (max > 0) {
            val mb = (max + (1024 * 1024 - 1)) / (1024 * 1024) // 向上取整 MB
            "上传文件大小超出限制：最大 ${mb}MB"
        } else "上传文件大小超出限制"
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ErrorResponse(message))
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException::class)
    fun handleDataIntegrity(@Suppress("UNUSED_PARAMETER") ex: org.springframework.dao.DataIntegrityViolationException)
            : ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("数据不合法或违反约束"))

    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception): ResponseEntity<ErrorResponse> {
        ex.printStackTrace()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("服务器内部错误"))
    }
}
