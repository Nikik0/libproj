package com.nikik0.libproj.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("studio")
data class MovieStudio(
    @Id
    val id: Long,
    val name: String,
    val employees: Long,
    val owner: String
)
