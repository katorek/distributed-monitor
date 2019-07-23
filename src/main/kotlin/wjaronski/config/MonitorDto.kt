package wjaronski.config

data class MonitorDto(
    val name: String,
    val ip: String,
    val maxPermits: Int,
    val conditionVariablesCount: Int,
    val proxy: ProxyDto
)