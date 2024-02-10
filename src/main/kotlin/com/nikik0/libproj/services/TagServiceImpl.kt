package com.nikik0.libproj.services

import com.nikik0.libproj.entities.MovieTag
import com.nikik0.libproj.kafka.model.EntityAffected
import com.nikik0.libproj.kafka.model.Event
import com.nikik0.libproj.kafka.model.EventType
import com.nikik0.libproj.kafka.service.EventProducer
import com.nikik0.libproj.repositories.TagRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class TagServiceImpl(
    private val tagRepository: TagRepository,
    private val eventProducer: EventProducer
) : TagService {

    override suspend fun saveTagIfNotPresent(tag: MovieTag) =
        tagRepository.findByName(tag.name)?:tagRepository.save(tag).also {
            eventProducer.publish(
                Event(
                    it.id,
                    EventType.CREATE,
                    EntityAffected.TAG,
                    "Tag ${it.name} saved"
                )
            )
        }

    override suspend fun findTagsForMovie(movieId: Long) =
        tagRepository.findTagsForMovie(movieId).toList()

    override suspend fun findByName(name: String) =
        tagRepository.findByNameIgnoreCase(name)
}