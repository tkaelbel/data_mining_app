package com.tok.data.mining.tool.mongodb

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class MongoDbConfiguration() {

    @Bean
    fun getMongoClient(): MongoClient {
        //TODO:user in system variable
        val connectionstring = ConnectionString("mongodb+srv://app_user:${System.getenv("ATLAS_DM_PW")}@cluster0.0kxwd.mongodb.net/data_mining?retryWrites=true&w=majority")
        val settings = MongoClientSettings.builder().applyConnectionString(connectionstring).build()

        return MongoClients.create(settings)
    }
}