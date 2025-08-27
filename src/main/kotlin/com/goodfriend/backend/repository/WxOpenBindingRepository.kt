package com.goodfriend.backend.repository

import com.goodfriend.backend.data.WxOpenBinding
import org.springframework.data.jpa.repository.JpaRepository

interface WxOpenBindingRepository : JpaRepository<WxOpenBinding, Long> {
    fun findByOpenid(openid: String): WxOpenBinding?
}
