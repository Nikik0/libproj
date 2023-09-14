package com.nikik0.libproj.entities

data class CustomerEntity(
    val id: Long,
    val name: String,
    val surname:String,
    val address: AddressEntity
)
