package net.typho.nbt_tools

interface EditBoxExtension {
    var `nbt_tools$isChat`: Boolean

    fun adjustMaxLength(maxLength: Int, text: String): Int {
        return if (`nbt_tools$isChat` && text.startsWith("/")) {
            Short.MAX_VALUE.toInt()
        } else {
            maxLength
        }
    }
}