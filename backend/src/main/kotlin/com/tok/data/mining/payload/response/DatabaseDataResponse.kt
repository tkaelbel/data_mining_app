package com.tok.data.mining.payload.response

data class DatabaseDataResponse(
    val databaseName: String,
    val collections: Set<String>?
)