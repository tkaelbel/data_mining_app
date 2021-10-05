package com.tok.data.mining.algorithm

import com.tok.data.mining.mongodb.MongoDbManager
import org.springframework.stereotype.Component

@Component
class AprioriDataHelper {
    val aprioriWorkModel = HashMap<String, AprioriWorkModel>()
}

data class AprioriWorkModel(
    var data: MongoDbManager.CollectionColumnDataModel?, var countedItems: Long?, var numTransactions: Long?,
    val minimumSupport: Double, val minimumConfidence: Double, var simpleData: List<String>?)