package com.goodfriend.backend.service

import com.goodfriend.backend.dto.CreateOrderRequest
import com.goodfriend.backend.dto.CreateOrderResponse
import com.goodfriend.backend.dto.OrderStatusResponse
import com.goodfriend.backend.dto.WxPayParams
import org.springframework.stereotype.Service
import java.util.*

@Service
class PaymentService {

    fun createOrder(req: CreateOrderRequest): CreateOrderResponse {
        // 调用微信支付 SDK 创建订单，获取返回的支付参数
        val orderId = UUID.randomUUID().toString()

        // 假设这里使用微信支付 SDK 创建订单并获取支付参数
        val wxPayParams = WxPayParams(
            appId = "yourAppId",
            timeStamp = System.currentTimeMillis().toString(),
            nonceStr = UUID.randomUUID().toString(),
            packageValue = "prepay_id=xxxx",
            signType = "MD5",
            paySign = "generatedSign"
        )

        return CreateOrderResponse(orderId, wxPayParams)
    }

    fun getOrderStatus(orderId: String): OrderStatusResponse {
        // 使用微信支付 API 查询订单状态
        // 这里简单模拟返回订单状态
        return OrderStatusResponse(orderId, "SUCCESS", 1000)
    }

    fun cancelOrder(orderId: String) {
        // 使用微信支付 API 取消订单
        // 这里简单模拟取消操作
        println("Cancel order $orderId")
    }
}
