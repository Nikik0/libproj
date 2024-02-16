package com.nikik0.libproj.repositories

import io.r2dbc.spi.Result
import org.reactivestreams.Publisher
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import java.util.function.Function


@Repository
class ManyToManyRepository (
    private val client: DatabaseClient
        ){

    suspend fun customerFavouriteMovieInsert(customerId: Long, movieId: Long) =
        client.sql("INSERT into customer_favourite_movies values ($customerId, $movieId)").await()

    suspend fun customerWatchedMovieInsert(customerId: Long, movieId: Long) =
        client.sql("INSERT into customer_watched_movies values ($customerId, $movieId)").await()

    suspend fun checkIfCustomerWatchedMovie(customerId: Long, movieId: Long) =
        client.sql("SELECT 1 from customer_watched_movies where customer_id = $customerId and watched_movie_id = $movieId")
            .fetch().awaitSingleOrNull() != null

    suspend fun checkIfCustomerFavMovie(customerId: Long, movieId: Long) =
        client.sql("SELECT 1 from customer_favourite_movies where customer_id = $customerId and favourite_movie_id = $movieId")
            .fetch().awaitSingleOrNull() != null

    suspend fun movieActorInsert(movieId: Long, actorIds: List<Long>){
        for (id in actorIds)
            client.sql("INSERT into movie_actor values ($movieId, $id)").await()
    }

    suspend fun studioMovieInsert(studioId: Long, movieId: Long) =
        client.sql("INSERT into studio_movie values ($studioId, $movieId)").await()

    suspend fun tagMovieInsert(tagIds: List<Long>, movieId: Long){
        for (id in tagIds)
            client.sql("INSERT into tag_movie values ($id, $movieId)").await()
    }

}