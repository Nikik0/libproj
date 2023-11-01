package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.MovieTag
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository: CoroutineCrudRepository<MovieTag, Long> {
    @Query("select * from tag t join tag_movie tm on t.id = tm.tag_id where tm.movie_id = :movieId")
    fun findTagsForMovie(movieId: Long): Flow<MovieTag>

    fun findByName(name: String): Flow<MovieTag>
}