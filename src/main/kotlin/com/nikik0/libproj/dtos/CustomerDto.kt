package com.nikik0.libproj.dtos

import com.nikik0.libproj.entities.AddressEntity

data class CustomerDto(
    val id: Long,
    val name: String,
    val surname:String,
    val country: String,
    val state: String,
    val city: String,
    val district: String,
    val street: String,
    val building: Int,
    val buildingLiteral: String,
    val apartmentNumber: Int,
    val additionalInfo: String
)
