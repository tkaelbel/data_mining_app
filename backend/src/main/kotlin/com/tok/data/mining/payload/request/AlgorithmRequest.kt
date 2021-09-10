package com.tok.data.mining.payload.request

data class AlgorithmRequest(val name: String, val properties: AlgorithmProperties, val databaseName: String, val collectionName: String, val columnName: String)

open class AlgorithmProperties(){}

class AprioriProperties(val minimumConfidence: Double, val minimumSupport: Double, val itemCount: Int) :
    AlgorithmProperties() {
}