package com.tok.data.mining.controller

import com.tok.data.mining.mongodb.MongoDbManager
import com.tok.data.mining.payload.request.CollectionDataRequest
import com.tok.data.mining.payload.response.CollectionDataResponse
import com.tok.data.mining.payload.response.DatabaseDataResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = arrayOf("*"), maxAge = 3600)
@RestController
@RequestMapping("api/v1/")
class DataController(
    val mongoDbManager: MongoDbManager
) : IDataController {

    @GetMapping("/databases")
    fun databases(): ResponseEntity<List<DatabaseDataResponse>>{
        return ResponseEntity.ok(mongoDbManager.getDatabases())
    }

    @GetMapping("/collectionData")
    fun collectionData(request: CollectionDataRequest): ResponseEntity<CollectionDataResponse> {
        val tempPage = request.page ?: 0
        val tempSize = request.size ?: 20
        return ResponseEntity.ok(mongoDbManager.getCollectionData(request.databaseName, request.collectionName, tempPage, tempSize))
    }
}