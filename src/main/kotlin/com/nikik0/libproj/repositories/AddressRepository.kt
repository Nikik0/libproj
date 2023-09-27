package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.AddressEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository


@Repository
interface AddressRepository : CoroutineCrudRepository <AddressEntity, Long> {
    @Query("select a.* from address a join customer_address ca on a.id = ca.address_id where ca.customer_id = :customerId limit 1")
    //@Query("select * from address limit 1")
    fun findAddressForCustomerId(customerId: Long): Flow<Any>
}