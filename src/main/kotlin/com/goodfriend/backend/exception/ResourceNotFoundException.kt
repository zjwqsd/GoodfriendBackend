package com.goodfriend.backend.exception

/**
 * 资源未找到异常类
 * 当请求的资源在系统中不存在时抛出此异常
 */
class ResourceNotFoundException : RuntimeException {

    /**
     * 使用默认消息构造异常
     */
    constructor() : super("请求的资源不存在")

    /**
     * 使用自定义消息构造异常
     * @param message 异常详细信息
     */
    constructor(message: String) : super(message)

    /**
     * 使用资源ID和资源类型构造异常
     * @param id 资源ID
     * @param resourceType 资源类型（如"User"、"Order"等）
     */
    constructor(id: Any, resourceType: String) : super("${resourceType} with ID $id not found")

    /**
     * 使用自定义消息和原因构造异常
     * @param message 异常详细信息
     * @param cause 异常原因
     */
    constructor(message: String, cause: Throwable?) : super(message, cause)
}