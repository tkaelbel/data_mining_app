package com.tok.data.mining.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.tok.data.mining.algorithm.AlgorithmManager
import com.tok.data.mining.payload.request.AprioriRequest
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RestController
@RequestMapping("api/v1/algorithm/")
class AlgorithmController(
    val algorithmManager: AlgorithmManager,
    val objectMapper: ObjectMapper
) {

    @GetMapping("apriori", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun apriori(request: AprioriRequest): Flux<String> = algorithmManager.executeAlgorithm(request).map {
        it.result
    }

}