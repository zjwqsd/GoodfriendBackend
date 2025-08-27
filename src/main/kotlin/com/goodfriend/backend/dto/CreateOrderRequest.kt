package com.goodfriend.backend.dto

data class CreateOrderRequest(
    val userId: Long,
    val amount: Int,  // 订单金额，单位为分
    val description: String
)

data class CreateOrderResponse(
    val orderId: String,
    val wxPayParams: WxPayParams
)

data class WxPayParams(
    val appId: String,
    val timeStamp: String,
    val nonceStr: String,
    val packageValue: String,
    val signType: String,
    val paySign: String
)

data class OrderStatusResponse(
    val orderId: String,
    val status: String,
    val amount: Int
)
