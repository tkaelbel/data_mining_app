package com.tok.data.mining.algorithm.apriori

data class ItemSet(val items: Array<String>, val support: Double, var confidence: Double? = null, val counted: Int = 0) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemSet

        if (!items.contentEquals(other.items)) return false
        if (support != other.support) return false
        if (confidence != other.confidence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = items.contentHashCode()
        result = 31 * result + support.hashCode()
        result = 31 * result + (confidence?.hashCode() ?: 0)
        return result
    }
}
