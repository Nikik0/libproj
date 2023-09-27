package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.CustomerEntityUpd
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepositoryUpd: CoroutineCrudRepository<CustomerEntityUpd, Long> {
}