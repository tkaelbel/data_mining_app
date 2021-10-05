package com.tok.data.mining.algorithm

import com.tok.data.mining.payload.request.AlgorithmRequest
import com.tok.data.mining.payload.response.AlgorithmResponse
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import reactor.core.publisher.Flux

@Component
class AlgorithmManager(
    val algorithmFactory: AlgorithmFactory
) {

    fun executeAlgorithm(request: AlgorithmRequest): Flux<AlgorithmResponse> {
        Assert.notNull(request, "request object is null")
        Assert.notNull(request.name, "algorithName is null")
        Assert.notNull(request.databaseName, "databaseName is null")
        Assert.notNull(request.collectionName, "collectionName is null")

        val algorithm = algorithmFactory.getAlgorithm(AlgorithmName.valueOf(request.name))
            ?: throw Error("Algorithm ${request.name} was not found")

        return algorithm.execute(request)
    }

}