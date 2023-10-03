package com.nikik0.libproj.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("tag")
data class MovieTag(
    @Id
    val id: Long,
    val name: String
)
