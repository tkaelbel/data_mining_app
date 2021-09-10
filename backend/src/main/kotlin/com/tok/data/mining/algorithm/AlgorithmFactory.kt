package com.tok.data.mining.algorithm

import org.springframework.stereotype.Component

@Component
class AlgorithmFactory(
    algorithms: Set<IAlgorithm>
) {

    lateinit var createdAlgorithms: MutableMap<AlgorithmName, IAlgorithm>

    init {
        createAlgorithms(algorithms)
    }

    private fun createAlgorithms(algorithms: Set<IAlgorithm>){
        createdAlgorithms = HashMap()
        algorithms.forEach {
            createdAlgorithms.put(it.name, it)
        }
    }

    fun getAlgorithm(name: AlgorithmName): IAlgorithm? {
        val algorithm = createdAlgorithms.get(name)

        if(algorithm == null) throw Error("Algorithm $name was not found")

        return createdAlgorithms.get(name)
    }
}