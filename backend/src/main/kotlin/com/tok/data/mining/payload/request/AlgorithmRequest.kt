package com.tok.data.mining.payload.request


open class AlgorithmRequest(
    val name: String,
    val databaseName: String,
    val collectionName: String,
    val columnName: String
)

class AprioriRequest(
    val minimumConfidence: Double, val minimumSupport: Double, val itemCount: Int, name: String,
    databaseName: String, collectionName: String, columnName: String
) :
    AlgorithmRequest(name, databaseName, collectionName, columnName)