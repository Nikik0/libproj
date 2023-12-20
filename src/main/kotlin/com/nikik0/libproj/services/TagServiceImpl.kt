package com.nikik0.libproj.services

import com.nikik0.libproj.entities.MovieTag
import com.nikik0.libproj.repositories.TagRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class TagServiceImpl(
    private val tagRepository: TagRepository
) : TagService {

    override suspend fun saveTagIfNotPresent(tag: MovieTag) =
        tagRepository.findByName(tag.name)?:tagRepository.save(tag)

    override suspend fun findTagsForMovie(movieId: Long) =
        tagRepository.findTagsForMovie(movieId).toList()

    override suspend fun findByName(name: String) =
        tagRepository.findByNameIgnoreCase(name)
}