package com.tok.data.mining.algorithm

import com.tok.data.mining.payload.request.AlgorithmRequest
import com.tok.data.mining.payload.response.AlgorithmResponse
import reactor.core.publisher.Flux

interface IAlgorithm {
    val name: AlgorithmName

    fun execute(data: AlgorithmRequest): Flux<AlgorithmResponse>
}

enum class AlgorithmName {
    APRIORI, K_NEAREST, K_MEANS
}
