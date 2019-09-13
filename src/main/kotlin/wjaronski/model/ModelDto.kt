package wjaronski.model

enum class ModelDto(
) {
    PRODUCER,
    CONSUMER;

    override fun toString(): String {
        return when (this) {
            PRODUCER -> "Prod"
            CONSUMER -> "Cons"
        }
    }
}
