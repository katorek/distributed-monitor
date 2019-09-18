package wjaronski.config.dto

data class MonitorDto(
    val name: String,
    val ip: String,
    val maxPermits: Int,
    val conditionVariablesCount: Int,
    val algorithm: String,
    val proxy: ProxyDto
)
