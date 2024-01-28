package com.nikik0.libproj.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "address")
data class AddressEntity(
    @Id
    val id: Long?,
    val country: String?,
    val state: String?,
    val city: String?,
    val district: String?,
    val street: String?,
    val building: Int?,
    @Column("building_literal")
    val buildingLiteral: String?,
    @Column("apartment_number")
    val apartmentNumber: Int?,
    @Column("additional_info")
    val additionalInfo: String?
)
