package com.tok.data.mining.mongodb

import com.mongodb.client.MongoClient
import com.tok.data.mining.payload.response.*
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.lang.Exception
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


@Component
class MongoDbManager(
    private val mongoClient: MongoClient
) {

    val admin: String = "admin"
    val local: String = "local"

    /**
     * Determines all available mongo db databases
     */
    fun getDatabases(): List<DatabaseDataResponse> =
        mongoClient.listDatabaseNames().filter { it != admin && it != local }
            .map { DatabaseDataResponse(it, determineCollectionNames(it)) }.toList()

    private fun determineCollectionNames(databaseName: String): Set<String> =
        mongoClient.getDatabase(databaseName).listCollectionNames().toSet()

    fun determineCollectionColumns(databaseName: String, collectionName: String): FieldResponse {
        val data = try {
            mongoClient.getDatabase(databaseName).getCollection(collectionName).find().limit(1)
        } catch (e: Exception) {
            println("Something went wrong accessing database: $databaseName and collection: $collectionName")
            throw e
        }
        val columns = data.map { it -> it.entries.map { it.key } }.flatten().filter { it != "_id" }.toSet()
        return FieldResponse(columns)
    }

    fun getCollectionData(databaseName: String, collectionName: String, page: Int, size: Int): CollectionDataResponse {
        val values = ArrayList<ArrayList<KeyValue>>()
        val columnNames = HashSet<String>()

        //TODO: check for catching exception if database or collection does not exist
        val collection = mongoClient.getDatabase(databaseName).getCollection(collectionName)

        val totalCount = collection.countDocuments()
        val data = collection.find().skip(if (page > 0) ((page - 1) * size) else 0).limit(size)

        data.forEach { document ->
            val formattedData = ArrayList<KeyValue>()

            document.entries.forEach {
                columnNames.add(it.key)
                formattedData.add(
                    KeyValue(
                        it.key,
                        (if (it.value is ObjectId) {
                            it.value.toString()
                        } else {
                            it.value
                        }),
                        it.value.javaClass.simpleName
                    )
                )
            }
            values.add(formattedData)
        }

        return CollectionDataResponse(
            databaseName,
            collectionName,
            columnNames,
            values,
            Pagination(page, size, totalCount, totalCount / size)
        )
    }

    fun determineCollectionColumnData(
        databaseName: String,
        collectionName: String,
        column: String
    ): CollectionColumnDataModel {
        //TODO: check for catching exception if database or collection does not exist
        val mongoTemplate = MongoTemplate(mongoClient, databaseName)

        val query = Query()
        query.fields().include(column)
        query.fields().exclude("_id")
        val values = mongoTemplate.find<Any>(query, collectionName)

        return CollectionColumnDataModel(column, values)
    }


    data class CollectionColumnDataModel(val column: String, val data: List<Any>)

}

