package com.tok.data.mining.algorithm.apriori

import com.tok.data.mining.mongodb.CollectionColumnDataModel

data class AprioriWorkModel(
    var data: CollectionColumnDataModel? = null,
    var countedItems: Int? = null,
    var numTransactions: Int? = null,
    val minimumSupport: Double,
    val minimumConfidence: Double,
    var simpleData: List<String>? = null,
    val itemCount: Int,
    var itemSets: MutableList<Array<String>>? = null,
    var items: Set<String>? = null,
    val outputItemSets: MutableList<ItemSet> = mutableListOf(),
){
    val errorMessage = "Could not get"
    fun getCheckedCountedItems(): Int = countedItems ?: throw Error("$errorMessage countedItems")
    fun getCheckedNumTransactions(): Int = numTransactions ?: throw Error("$errorMessage numTransactions")
    fun getCheckedSimpleData(): List<String> = simpleData ?: throw Error("$errorMessage simpleData")
    fun getCheckedItemsets(): MutableList<Array<String>> = itemSets ?: throw Error("$errorMessage itemSets")
    fun getCheckedItems(): Set<String> = items ?: throw Error("$errorMessage items")
    fun getCheckedData(): CollectionColumnDataModel = data ?: throw Error("$errorMessage data")
}
