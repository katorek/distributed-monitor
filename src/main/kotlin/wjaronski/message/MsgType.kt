package wjaronski.message

enum class MsgType {
    SIGNAL,
    SIGNAL_ALL,
    ACQUIRE,
    RELEASE,
    CS_REQUEST, // CS = Critical Section
    CS_REPLY
}
