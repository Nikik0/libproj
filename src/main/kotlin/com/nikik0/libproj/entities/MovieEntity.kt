package com.nikik0.libproj.entities

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("movie")
data class MovieEntity(
    @Id
    val id: Long,
    val name: String,
    val producer: String,
    @Transient
    var actors: List<Actor>,
    @Transient
    var tags: List<MovieTag>,
    @Transient
    var studio: MovieStudio?,
    val budget: Long,
    @Column("movie_url")
    val movieUrl: String
){
    @PersistenceCreator
    constructor(
        id: Long,
        name: String,
        producer: String,
        budget: Long,
        movieUrl: String
    ) : this(id, name, producer, emptyList(), emptyList(), null, budget, movieUrl)
}
