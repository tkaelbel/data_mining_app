package com.tok.data.mining.mongodb

import com.mongodb.client.MongoClient
import com.tok.data.mining.payload.response.CollectionDataResponse
import com.tok.data.mining.payload.response.DatabaseDataResponse
import com.tok.data.mining.payload.response.KeyValue
import com.tok.data.mining.payload.response.Pagination
import org.bson.BsonDocument
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


@Component
class MongoDbManager(
    private val mongoClient: MongoClient
) {

    /**
     * Determines all available mongo db databases
     */
    fun getDatabases(): List<DatabaseDataResponse> {
        val models = ArrayList<DatabaseDataResponse>()
        mongoClient.listDatabaseNames().forEach {
            models.add(DatabaseDataResponse(it, determineCollectionNames(it)))
        }
        return models
    }

    private fun determineCollectionNames(databaseName: String): Set<String> {
        val collectionNames = HashSet<String>()
        mongoClient.getDatabase(databaseName).listCollectionNames().forEach {
            collectionNames.add(it)
        }
        return collectionNames
    }

    fun getCollectionData(databaseName: String, collectionName: String, page: Int, size: Int): CollectionDataResponse {
        Assert.notNull(databaseName, "databaseName cannot be null")
        Assert.notNull(collectionName, "collectionName cannot be null")

        val values = ArrayList<ArrayList<KeyValue>>()
        val columnNames = HashSet<String>()

        //TODO: check for catching exception if database or collection does not exist
        val collection = mongoClient.getDatabase(databaseName).getCollection(collectionName)

        val totalCount = collection.countDocuments()
        val data = collection.find().skip(if (page > 0) ((page - 1) * size) else 0 ).limit(size)

        data.forEach {
            val formattedData = ArrayList<KeyValue>()

            it.entries.forEach {
                columnNames.add(it.key)
                formattedData.add(
                    KeyValue(
                        it.key,
                        (if (it.value is ObjectId) {
                            it.value.toString()
                        } else {
                            it.value
                        })
                    )
                )
            }
            values.add(formattedData)
        }

        return CollectionDataResponse(databaseName, collectionName, columnNames, values, Pagination(page, size, totalCount))
    }

    fun determineCollectionColumnData(databaseName: String, collectionName: String, column: String): CollectionColumnDataModel {
        Assert.notNull(databaseName, "databaseName cannot be null")
        Assert.notNull(collectionName, "collectionName cannot be null")
        Assert.notNull(column, "column cannot be null")

        //TODO: check for catching exception if database or collection does not exist
        val mongoTemplate = MongoTemplate(mongoClient, databaseName)

        val values = mongoTemplate.find(BasicQuery("{}", "${column}: 1, _id: 0"), Document().javaClass, collectionName)

        return  CollectionColumnDataModel(column, values)
    }


}

