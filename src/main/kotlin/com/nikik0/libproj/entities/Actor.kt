package com.nikik0.libproj.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("actor")
data class Actor(
    @Id
    val id: Long,
    val name: String,
    val surname: String,
    val age: Int
)
