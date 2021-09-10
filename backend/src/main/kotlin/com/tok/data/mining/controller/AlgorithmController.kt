package com.tok.data.mining.controller

import com.tok.data.mining.algorithm.Apriori
import com.tok.data.mining.payload.request.AlgorithmRequest
import com.tok.data.mining.payload.response.AlgorithmResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("api/v1/algorithm/")
class AlgorithmController(
    val apriori: Apriori
) {

    @GetMapping("apriori")
    fun apriori(request: AlgorithmRequest): Flux<AlgorithmResponse> {
        return apriori.execute(request)
    }
}