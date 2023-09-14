package com.nikik0.libproj.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "addressEntity")
data class AddressEntity(
    @Id
    val id: Long,
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
