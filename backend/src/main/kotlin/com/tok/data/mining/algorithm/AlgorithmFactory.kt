package com.tok.data.mining.algorithm

import org.springframework.stereotype.Component
import java.util.*

@Component
class AlgorithmFactory(
    algorithms: Set<IAlgorithm>
) {

    lateinit var createdAlgorithms: MutableMap<AlgorithmName, IAlgorithm>

    init {
        createAlgorithms(algorithms)
    }

    private fun createAlgorithms(algorithms: Set<IAlgorithm>){
        createdAlgorithms = EnumMap(AlgorithmName::class.java)
        algorithms.forEach {
            createdAlgorithms[it.name] = it
        }
    }

    fun getAlgorithm(name: AlgorithmName): IAlgorithm? =
        createdAlgorithms[name] ?: throw Error("Algorithm $name was not found")

}