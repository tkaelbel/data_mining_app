package com.tok.data.mining.algorithm

import com.tok.data.mining.mongodb.MongoDbManager
import com.tok.data.mining.payload.request.AlgorithmRequest
import com.tok.data.mining.payload.request.AprioriRequest
import com.tok.data.mining.payload.response.AlgorithmResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@Component
class Apriori(
    val mongoDbManager: MongoDbManager,
    val aprioriHelper: AprioriDataHelper
) : IAlgorithm  {

    override val name: AlgorithmName
        get() = AlgorithmName.APRIORI

    override fun execute(data: AlgorithmRequest): Flux<AlgorithmResponse> {
        if(data !is AprioriRequest) throw Error("Wrong poperties type")

        val keyName = "${data.databaseName}.${data.collectionName}"

        aprioriHelper.aprioriWorkModel.set(keyName,
            AprioriWorkModel(null,null, null, data.minimumSupport, data.minimumConfidence, null))

        return Flux.just(startOrganizeData(data),
            organizeData(data, keyName),
            convertData(keyName),
            countItems(keyName),
            inputConfiguration(keyName)
        )
    }

    private fun startOrganizeData(data: AlgorithmRequest): AlgorithmResponse {
        return AlgorithmResponse("Started to organize data from ${data.databaseName}.${data.collectionName} \n")
    }

    private fun organizeData(data: AlgorithmRequest, keyName: String): AlgorithmResponse {
        aprioriHelper.aprioriWorkModel.get(keyName)?.data = mongoDbManager.determineCollectionColumnData(data.databaseName, data.collectionName, data.columnName)
        return AlgorithmResponse("Organized data \n")
    }

    private fun convertData(keyName: String): AlgorithmResponse {
        val data = aprioriHelper.aprioriWorkModel.get(keyName)?.data
        val convertedData = ArrayList<String>()
        data?.data?.forEach {
            if(it is List<*>){
                val buffer = StringBuffer()
                it.forEach {
                    buffer.append(it)
                    buffer.append(",")
                }
                buffer.append(buffer.length-1)
                convertedData.add(buffer.toString())
            }
        }

        aprioriHelper.aprioriWorkModel.get(keyName)?.simpleData = convertedData
        return AlgorithmResponse("Converted data to readable format \n")
    }

    private fun countItems(keyName: String): AlgorithmResponse {
        val simpleData = aprioriHelper.aprioriWorkModel.get(keyName)?.simpleData
        val countedItems = HashSet<String>()
        simpleData?.forEach {
            val stk = StringTokenizer(it, ",")
            while (stk.hasMoreElements()) {
                countedItems.add(stk.nextToken())
            }
        }
        aprioriHelper.aprioriWorkModel.get(keyName)?.countedItems =  countedItems.size.toLong()

        return AlgorithmResponse("Counting all items \n")
    }

    private fun inputConfiguration(keyName: String): AlgorithmResponse{
        val workModel = aprioriHelper.aprioriWorkModel.get(keyName)
        val buffer = StringBuffer("Input configuration: ")
        buffer.append("counted: ${workModel?.countedItems} items, ")
        buffer.append("determined: ${workModel?.simpleData?.size} transactions, ")
        buffer.append("minimum support: ${workModel?.minimumSupport}, ")
        buffer.append("minimum confidence: ${workModel?.minimumConfidence}, ")
        return AlgorithmResponse(buffer.toString())
    }




}