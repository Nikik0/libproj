package com.nikik0.libproj.services

import com.nikik0.libproj.entities.MovieTag
import com.nikik0.libproj.repositories.TagRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class TagService(
    private val tagRepository: TagRepository
) {

    suspend fun saveTagIfNotPresent(tag: MovieTag) =
        tagRepository.findByName(tag.name)?:tagRepository.save(tag)

    suspend fun findTagsForMovie(movieId: Long) =
        tagRepository.findTagsForMovie(movieId).toList()

    suspend fun findByName(name: String) =
        tagRepository.findByName(name)
}