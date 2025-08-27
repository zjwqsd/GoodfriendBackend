package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.CreateOrderRequest
import com.goodfriend.backend.dto.CreateOrderResponse
import com.goodfriend.backend.dto.OrderStatusResponse
import com.goodfriend.backend.service.PaymentService
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/payment")
class PaymentController(
    private val paymentService: PaymentService
) {

    /**
     * 1) 创建微信支付订单
     *    示例：POST /api/payment/create-order
     */
    @PostMapping("/create-order")
    fun createOrder(
        @RequestBody req: CreateOrderRequest
    ): ResponseEntity<CreateOrderResponse> {
        val order = paymentService.createOrder(req)
        return ResponseEntity.ok(order)
    }

    /**
     * 2) 获取订单状态
     *    示例：GET /api/payment/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    fun getOrderStatus(
        @PathVariable orderId: String
    ): ResponseEntity<OrderStatusResponse> {
        val status = paymentService.getOrderStatus(orderId)
        return ResponseEntity.ok(status)
    }

    /**
     * 3) 取消订单
     *    示例：POST /api/payment/cancel-order/{orderId}
     */
    @PostMapping("/cancel-order/{orderId}")
    fun cancelOrder(
        @PathVariable orderId: String
    ): ResponseEntity<Void> {
        paymentService.cancelOrder(orderId)
        return ResponseEntity.noContent().build()
    }
}

