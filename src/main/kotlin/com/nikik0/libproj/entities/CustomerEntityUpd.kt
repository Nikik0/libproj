package com.nikik0.libproj.entities

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("customer")
data class CustomerEntityUpd(
    @Id
    val id: Long,
    val name: String,
    val surname:String,
    @Column("address_id")
    val addressId: Long,
    @Transient
    var address: AddressEntity?,
//    @Transient
//    val watched: List<MovieEntity>,
    @Transient
    var favorites: List<MovieEntity> = emptyList()
){
    @PersistenceCreator
    constructor(
        id: Long,
        name: String,
        surname:String,
        addressId: Long
    ): this(id, name, surname, addressId, null,emptyList())
}
