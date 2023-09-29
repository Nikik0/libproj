package com.nikik0.libproj.repositories

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.stereotype.Repository

@Repository
class ManyToManyRepository (
    private val client: DatabaseClient
        ){

    suspend fun customerAddressInsert(customerId: Long, addressId: Long): Unit {
        client.sql("INSERT into customer_address values ($1, $2)")
            .bind(0, customerId)
            .bind(1, addressId)
            .await()
    }

    suspend fun customerFavouriteMovieInsert(customerId: Long, movieId: Long){
        client.sql("INSERT into customer_favourite_movies values ($1, $2)")
            .bind(0, customerId)
            .bind(1, movieId)
            .await()
    }

    suspend fun customerWatchedMovieInsert(customerId: Long, movieId: Long){
        client.sql("INSERT into customer_watched_movies values ($1, $2)")
            .bind(0, customerId)
            .bind(1, movieId)
            .await()
    }

    suspend fun movieActorInsert(movieId: Long, ActorId: Long){
        client.sql("INSERT into movie_actor values ($1, $2)")
            .bind(0, movieId)
            .bind(1, ActorId)
            .await()
    }

    suspend fun studioMovieInsert(studioId: Long, movieId: Long){
        client.sql("INSERT into studio_movie values ($1, $2)")
            .bind(0, studioId)
            .bind(1, movieId)
            .await()
    }

    suspend fun tagMovieInsert(tagId: Long, movieId: Long){
        client.sql("INSERT into tag_movie values ($1, $2)")
            .bind(0, tagId)
            .bind(1, movieId)
            .await() 
    }

}