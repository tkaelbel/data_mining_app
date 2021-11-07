package com.tok.data.mining.algorithm

import kotlin.math.pow

class Util {
    companion object {
        fun round(value: Double, places: Int): Double {
            val factor = 10.0.pow(places.toDouble())
            val tempValue = value.times(factor)
            return Math.round(tempValue).div(factor)
        }
    }
}