package com.tok.data.mining.payload.response

data class KeyValue(val key: String, val value: Any, val type: String)

data class CollectionDataResponse(val databaseName: String, val collectionName: String, val columns: Set<String>, val data: List<List<KeyValue>>, val pagination: Pagination?)

data class Pagination(val page: Int, val size: Int, val totalCount: Long, val totalPages: Number)

data class FieldResponse(val fields: Set<String>)
