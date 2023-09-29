package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.MovieStudio
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StudioRepository: CoroutineCrudRepository<MovieStudio, Long> {
    @Query("select * from studio s join studio_movie sm on s.id = sm.studio_id where sm.movie_id = :movieId")
    fun findStudioForMovie(movieId: Long): Flow<MovieStudio>
}