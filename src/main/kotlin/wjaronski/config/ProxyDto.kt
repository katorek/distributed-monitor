package wjaronski.config

data class ProxyDto(
    val subPort: Int,
    val pubPort: Int,
    val checkIfAliveTime: Long
)