package com.nikik0.libproj.repositories

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.stereotype.Repository

@Repository
class ManyToManyRepository (
    private val client: DatabaseClient
        ){

    suspend fun customerAddressInsert(customerId: Long, addressId: Long): Unit {
        client.sql("INSERT into customer_address values ($1, $2)")
            .bind(0, customerId)
            .bind(1, addressId)
            .await()
    }
}