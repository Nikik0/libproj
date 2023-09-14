package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : CoroutineCrudRepository<CustomerEntity, Long>{
    fun getCustomerEntitiesByNameAndSurname(name: String, surname: String): Flow<CustomerEntity>
}