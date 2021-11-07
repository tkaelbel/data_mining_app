package com.tok.data.mining.algorithm.apriori

import com.tok.data.mining.algorithm.AlgorithmName
import com.tok.data.mining.algorithm.IAlgorithm
import com.tok.data.mining.algorithm.Util
import com.tok.data.mining.mongodb.MongoDbManager
import com.tok.data.mining.payload.request.AlgorithmRequest
import com.tok.data.mining.payload.request.AprioriRequest
import com.tok.data.mining.payload.response.AlgorithmResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@Component
class Apriori(
    val mongoDbManager: MongoDbManager
) : IAlgorithm {

    override val name: AlgorithmName
        get() = AlgorithmName.APRIORI

    override fun execute(data: AlgorithmRequest): Flux<AlgorithmResponse> {
        if (data !is AprioriRequest) throw Error("Wrong parameters type")

        val aprioriWorkModel = AprioriWorkModel(
            minimumSupport = data.minimumSupport,
            minimumConfidence = data.minimumConfidence,
            itemCount = data.itemCount
        )

        val startTime = LocalDateTime.now()
        val started = "Started at: ${startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))}"

        val flux = Flux.just(
            AlgorithmResponse(started),
            AprioriOutputHelper.startOrganizeData(data),
            organizeData(data, aprioriWorkModel),
            convertData(aprioriWorkModel),
            AprioriOutputHelper.printData(aprioriWorkModel),
            AprioriOutputHelper.inputConfiguration(aprioriWorkModel),
            createInitialItemsets(aprioriWorkModel)
        )

        val fluxes = mutableListOf<Flux<AlgorithmResponse>>()

        var itemSetNumber = 1

        //support
        //TODO: refactor
        while (aprioriWorkModel.getCheckedItemsets().isNotEmpty()) {
            fluxes.add(Flux.just(calculateFrequencyOfItemset(aprioriWorkModel)))

            //TODO: change this to be used before the first flux / doesn't make sense to print the first "passing through.." if we know the itemcount
            if (aprioriWorkModel.getCheckedItemsets().size == 0) break

            //if the calculated itemsets have multiple items and these count of items is higher than the input of itemcount we don't continue
            if (aprioriWorkModel.getCheckedItemsets()[0].size >= aprioriWorkModel.itemCount) {
                fluxes.add(Flux.just(AlgorithmResponse("Input Item count: ${aprioriWorkModel.itemCount} is reached. Continue..")))
                fluxes.add(Flux.just(AlgorithmResponse("Finished with step one -> calculation of the support;")))
                break
            }

            fluxes.add(Flux.just(AlgorithmResponse("Found ${aprioriWorkModel.getCheckedItemsets().size} frequent itemsets of size $itemSetNumber with support higher than input of ${aprioriWorkModel.minimumSupport * 100}%")))
            itemSetNumber++
            fluxes.add(
                Flux.just(
                    AlgorithmResponse(
                        "Creating itemsets of size ${aprioriWorkModel.getCheckedItemsets()[0].size + 1} based on ${
                            aprioriWorkModel.getCheckedItemsets().map { it.contentToString() }
                        } " +
                                "itemsets of size ${aprioriWorkModel.getCheckedItemsets()[0].size}"
                    )
                )
            )
            fluxes.add(Flux.just(createNewItemsets(aprioriWorkModel)))
            fluxes.add(
                Flux.just(
                    AprioriOutputHelper.printItemsets(aprioriWorkModel)
                )
            )
        }

        //confidence
        //TODO: refactor
        var endFlux = Flux.just(AlgorithmResponse(""))
        fluxes.forEach {
            endFlux = Flux.merge(endFlux, it)
        }
        val moreFluxes = determineConfidenceRules(aprioriWorkModel)
        moreFluxes.forEach {
            endFlux = Flux.merge(endFlux, it)
        }

        val endTime = LocalDateTime.now()
        val end = endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))

        endFlux = Flux.merge(
            endFlux,
            Flux.just(
                AlgorithmResponse(
                    ";Finished at: $end. It took ${
                        Duration.between(
                            startTime,
                            endTime
                        ).seconds
                    } seconds / ${Duration.between(startTime, endTime).toMillis()} milliseconds"
                )
            )
        )
        return Flux.merge(flux, endFlux).share().delayElements(Duration.ofMillis(300))
    }

    private fun calculateConfidence(
        numerator: Double,
        divisor: Double,
        firstItem: Set<String>,
        secondItem: Set<String>,
        resultList: MutableList<RuleSet>,
        fluxList: MutableList<Flux<AlgorithmResponse>>,
        workModel: AprioriWorkModel,
        support: Double
    ) {
        val confidence = Util.round(numerator.div(divisor), 2)
        val rule = "${secondItem.joinToString(",")} -> ${firstItem.joinToString(",")}"
        fluxList.add(Flux.just(AlgorithmResponse("Calculated confidence for $rule - ${numerator.toInt()}/${divisor.toInt()}=$confidence")))
        if (confidence >= workModel.minimumConfidence) {
            fluxList.add(Flux.just(AlgorithmResponse("Added rule to result, because $confidence is higher than ${workModel.minimumConfidence};")))
            resultList.add(RuleSet(rule, confidence, support, secondItem.size+firstItem.size))
        }
    }

    private fun determineConfidenceRules(workModel: AprioriWorkModel): List<Flux<AlgorithmResponse>> {
        val list = mutableListOf<Flux<AlgorithmResponse>>()
        val resultList = mutableListOf<RuleSet>()

        // l variable to track the rule state
        var l = 2

        while (l <= workModel.itemCount) {
            //first we need to filter out the itemsets for the current rule size
            val currentItemsets = workModel.outputItemSets.filter { it.items.size == l }

            list.add(Flux.just(AlgorithmResponse("Generating rules out of size $l;")))

            // creating rules for itemsets size > 2
            currentItemsets.forEach { itemset ->

                // generate rules
                val rules = mutableSetOf<TempRule>()
                val lastItem = itemset.items.last()
                itemset.items.forEachIndexed { index, s ->
                    // generate rules x -> z,y,i
                    rules.add(TempRule(setOf(s), itemset.items.filter { it != s }.toSet()))

                    val tempIndex = if (index == itemset.items.size - 1) 0 else index
                    // generate rules x,z -> y,i
                    val secondItemset = itemset.items.filter { it != s && it != itemset.items[tempIndex] }.toSet()
                    if (!secondItemset.isEmpty())
                        rules.add(
                            TempRule(
                                setOf(s, itemset.items[tempIndex]),
                                secondItemset
                            )
                        )

                    // generate rules x,z,y -> i
                    if (s != lastItem) {
                        rules.add(TempRule(itemset.items.filter { it != lastItem }.toSet(), setOf(lastItem)))
                    }
                }
                // numerator
                val numerator = itemset.counted.toDouble()

                // iterate over the rules
                rules.forEach { tempRule ->
                    // find the divisor for the second item of the rule
                    val foundItemSet = workModel.outputItemSets.find { it.items.contentEquals(tempRule.second.toTypedArray()) }
                    if(foundItemSet != null){
                        calculateConfidence(
                            numerator,
                            foundItemSet.counted.toDouble(),
                            tempRule.first,
                            tempRule.second,
                            resultList,
                            list,
                            workModel,
                            itemset.support
                        )
                    }
                }
            }
            l++
        }

        list.add(Flux.just(AprioriOutputHelper.printResult(resultList)))

        return list
    }



    private fun createNewItemsets(workModel: AprioriWorkModel): AlgorithmResponse {
        val currentSizeOfItemset = workModel.getCheckedItemsets()[0].size
        val tempCandidates = mutableMapOf<String, Array<String>>()

        workModel.getCheckedItemsets().forEachIndexed { i: Int, strings: Array<String> ->
            var j = i + 1
            while (j < workModel.getCheckedItemsets().size) {
                val x = strings
                val y = workModel.getCheckedItemsets()[j]

                if (x.size != y.size) throw Error("Cannot go on. Length of itemsets are not equal!")

                val newCandidates = Array(currentSizeOfItemset + 1) { "" }
                var s = 0
                while (s < newCandidates.size - 1) {
                    newCandidates[s] = x[s]
                    s++
                }

                var nDifferent = 0
                var s1 = 0
                while (s1 < y.size) {
                    var found = false
                    var s2 = 0
                    while (s2 < x.size) {
                        if (x[s2] == y[s1]) found = true; break
                    }

                    if (!found) {
                        nDifferent++
                        newCandidates[newCandidates.size - 1] = y[s1]
                    }

                    s1++
                }


                if (nDifferent == 1) {
                    Arrays.sort(newCandidates)
                    tempCandidates.put(newCandidates.contentToString().replace("[", "").replace("]", ""), newCandidates)
                }
                j++
            }
        }

        workModel.getCheckedItemsets().clear()
        workModel.getCheckedItemsets().addAll(tempCandidates.values)
        return AlgorithmResponse("Created ${workModel.getCheckedItemsets().size} unique itemsets of size ${currentSizeOfItemset + 1}")
    }

    private fun calculateFrequencyOfItemset(workModel: AprioriWorkModel): AlgorithmResponse {
        //first we create unique items with a counter
        val itemCounter = mutableMapOf<String, Int>()
        workModel.getCheckedItemsets().forEach {
            itemCounter[it.contentToString().replace("[", "").replace("]", "").replace(" ", "")] = 0
        }

        for (e in itemCounter.iterator()) {
            //multiple itemsets
            if (e.key.contains(",")) {
                val splittedItems = e.key.split(",")
                //now we need to check if the combined items appear in the transaction (not only like item1, item2 also item1, item3, item2 (this is also a match))
                workModel.getCheckedSimpleData().forEach {
                    var match = false
                    run inner@{
                        splittedItems.forEach { item ->
                            match = it.contains(item)
                            //if we have a false after the first check then the combined items are not in the dataset / go on
                            if (!match) return@inner
                        }
                    }

                    if (match) {
                        var value = itemCounter[e.key]
                        if (value != null) itemCounter[e.key] = value.plus(1)
                    }
                }
            } else {
                itemCounter[e.key] = workModel.getCheckedSimpleData().count { it.contains(e.key) }
            }
        }

        // if we only have zeros in the int array we don't have a match
        // just go back
        if (itemCounter.count { it.value == 0 } == itemCounter.size) {
            workModel.itemSets = mutableListOf()
            return AlgorithmResponse("Input Item count: ${workModel.itemCount} is reached. Continue..")
        }

        //remove zeros
        val itemCounterWithoutZeros = itemCounter.filter { it.value != 0 }

        return generateCalculateFrequencyOutput(workModel, itemCounterWithoutZeros)
    }

    private fun generateCalculateFrequencyOutput(
        workModel: AprioriWorkModel,
        itemCounter: Map<String, Int>
    ): AlgorithmResponse {
        val output =
            StringBuffer("Passing through the data to compute the frequency of ${workModel.getCheckedItemsets().size} itemsets of size ${workModel.getCheckedItemsets()[0].size};")
        val frequentCandidates = mutableListOf<Array<String>>()

        itemCounter.forEach {
            val calculatedSupport = it.value.div(workModel.getCheckedNumTransactions().toDouble())
            if (calculatedSupport >= workModel.minimumSupport) {
                val items = it.key.split(",").toTypedArray()
                frequentCandidates.add(items)

                output.append(";${it.key}")
                output.append(" (")
                output.append(calculatedSupport)
                output.append(" ")
                output.append(it.value)
                output.append(")")

                workModel.outputItemSets.add(ItemSet(items, calculatedSupport, counted = it.value))
            }
        }
        workModel.itemSets = frequentCandidates
        return AlgorithmResponse("$output;")
    }

    private fun createInitialItemsets(workModel: AprioriWorkModel): AlgorithmResponse {
        val itemSets: MutableList<Array<String>> = mutableListOf()
        val buffer = StringBuffer("Created itemsets of size 1:;")
        workModel.getCheckedItems().forEach {
            itemSets.add(arrayOf(it))
            buffer.append("$it;")
        }

        workModel.itemSets = itemSets
        return AlgorithmResponse(buffer.toString())
    }



    //TODO: make it prettier
    private fun convertData(aprioriWorkModel: AprioriWorkModel): AlgorithmResponse {
        val convertedData = ArrayList<String>()
        val countedItems = HashSet<String>()

        aprioriWorkModel.getCheckedData().data.forEach { rows ->
            if (rows is Map<*, *>) {
                val buffer = StringBuffer()
                rows.forEach { list ->
                    if (list.value is List<*>) {
                        (list.value as List<*>).forEach {
                            //TODO type check
                            countedItems.add(it as String)
                            buffer.append(it)
                            buffer.append(",")
                        }
                    }
                }
                buffer.setLength(buffer.length - 1)
                convertedData.add(buffer.toString())
            }
        }
        aprioriWorkModel.simpleData = convertedData
        aprioriWorkModel.items = countedItems
        aprioriWorkModel.countedItems = countedItems.size
        aprioriWorkModel.numTransactions = convertedData.size
        return AlgorithmResponse("Converted data to readable format and counted all items")
    }

    private fun organizeData(data: AlgorithmRequest, aprioriWorkModel: AprioriWorkModel): AlgorithmResponse {
        aprioriWorkModel.data =
            mongoDbManager.determineCollectionColumnData(data.databaseName, data.collectionName, data.columnName)
        return AlgorithmResponse("Organized data")
    }

}