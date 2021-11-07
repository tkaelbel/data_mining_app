package com.tok.data.mining.algorithm.apriori

import com.tok.data.mining.payload.request.AlgorithmRequest
import com.tok.data.mining.payload.response.AlgorithmResponse

/**
 * Just an ouput helper class to generate strings for the response of the webservice call
 */
class AprioriOutputHelper {
    companion object {

        /**
         * Prints out the end result of the apriori algorithm
         */
        fun printResult(ruleSets: List<RuleSet>): AlgorithmResponse {
            val buffer = StringBuffer()

            val sortedList = ruleSets.sortedBy { it.typeRule }
            var itemBefore:RuleSet? = null
            sortedList.forEach{ ruleSet ->
                if(itemBefore == null || itemBefore?.typeRule != ruleSet.typeRule){
                    buffer.append(";;${ruleSet.typeRule}-Item-Rules         conf         sup;")
                }

                buffer.append(";${ruleSet.rule}         ${ruleSet.confidence}         ${ruleSet.support}")
                itemBefore = ruleSet
            }

            return AlgorithmResponse(buffer.toString())
        }

        /**
         * Prints the current itemsets.
         */
        fun printItemsets(workModel: AprioriWorkModel): AlgorithmResponse {
            val buffer = StringBuffer()
            workModel.getCheckedItemsets().forEach { buffer.append(";${it.contentToString()}") }
            return AlgorithmResponse("$buffer;")
        }

        /**
         * Prints the whole data, that is used to execute the apriori algorithm
         */
        fun printData(workModel: AprioriWorkModel): AlgorithmResponse {
            val buffer = StringBuffer("Data: ;")
            workModel.getCheckedSimpleData().forEach { buffer.append("$it;") }
            return AlgorithmResponse(buffer.toString())
        }


        fun startOrganizeData(data: AlgorithmRequest): AlgorithmResponse =
            AlgorithmResponse("Started to organize data from ${data.databaseName}.${data.collectionName}")

        fun inputConfiguration(workModel: AprioriWorkModel): AlgorithmResponse {
            val buffer = StringBuffer("Input configuration = ")
            buffer.append("counted: ${workModel.getCheckedCountedItems()} items, ")
            buffer.append("determined: ${workModel.getCheckedSimpleData().size} transactions, ")
            buffer.append("minimum support: ${workModel.minimumSupport}, ")
            buffer.append("minimum confidence: ${workModel.minimumConfidence}, ")
            buffer.append("item count: ${workModel.itemCount}")
            return AlgorithmResponse(buffer.toString())
        }
    }
}