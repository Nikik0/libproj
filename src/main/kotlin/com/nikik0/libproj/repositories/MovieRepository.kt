package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.MovieEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository


@Repository
interface MovieRepository: CoroutineCrudRepository<MovieEntity,Long> {

    @Query("select * from movie m join tag_movie tm on m.id = tm.movie_id where tm.tag_id = :tagId")
    fun findMoviesByTagId(tagId: Long): Flow<MovieEntity>

}