package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.AddressEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository


@Repository
interface AddressRepository : CoroutineCrudRepository <AddressEntity, Long> {

}