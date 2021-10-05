package com.tok.data.mining.controller

import com.tok.data.mining.algorithm.AlgorithmManager
import com.tok.data.mining.algorithm.Apriori
import com.tok.data.mining.payload.request.AlgorithmRequest
import com.tok.data.mining.payload.request.AprioriRequest
import com.tok.data.mining.payload.response.AlgorithmResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@CrossOrigin(origins = arrayOf("*"), maxAge = 3600)
@RestController
@RequestMapping("api/v1/algorithm/")
class AlgorithmController(
    val algorithmManager: AlgorithmManager
) {

    @GetMapping("apriori")
    fun apriori(request: AprioriRequest): Flux<AlgorithmResponse> = algorithmManager.executeAlgorithm(request)

}