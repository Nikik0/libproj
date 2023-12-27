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

    suspend fun customerFavouriteMovieInsert(customerId: Long, movieIds: List<Long>){
        //todo this should be a batch save if i'll figure out how to do it in r2dbc kotlin
//        for (id in movieIds)
//            client.sql("INSERT into customer_favourite_movies values ($1, $2)")
//                .bind(0,customerId)
//                .bind(1, id)
//                .await()


        client.inConnectionMany { connection ->
            val statement = connection.createStatement("INSERT into customer_favourite_movies values ($1, $2)")
//            movieIds.forEach {
//                statement
//                    .bind(0, customerId)
//                    .bind(1, it)
//                    .add()
//            }
            movieIds.dropLast(1).forEach {
                statement
                    .bind(0, customerId)
                    .bind(1, it)
                    .add()
            }
//            for (i in 0 until movieIds.size - 1){
//                statement
//                    .bind(0, customerId)
//                    .bind(1, movieIds[i])
//                    .add()
//            }
            statement
                .bind(0, customerId)
                .bind(1, movieIds[movieIds.size-1])

            statement.execute().toFlux().flatMap { result ->
                result.map { row, _ -> row.get("customer_id", Long::class.java) }
            }
        }.subscribe()
        //val smth = client.sql("INSERT into customer_favourite_movies values ($1, $2)")
//        client.inConnectionMany { connection -> {
//            val state = connection.createStatement("INSERT into customer_favourite_movies values ($1, $2)")
//            for (i in movieIds){
//                state.bind(0,customerId)
//                    .bind(1, i)
//                    .add()
//
//            }
//            return Flux.from(state.execut
//                    e())
        //
//
//            val statement: Unit = connection.createStatement("INSERT into customer_favourite_movies values ($1, $2)")
//                .returnGeneratedValues("id")
//
//            for (p in data) {
//                statement.bind(0, p.getTitle()).bind(1, p.getContent()).add()
//            }
//            return Flux.from(statement.execute()).flatMap<Any>(Function<T, Publisher<*>> { result: T ->
//                result.map { row, rowMetadata ->
//                    row.get(
//                        "id",
//                        UUID::class.java
//                    )
//                }
//            }
//            )

//        } }
    }

    suspend fun customerWatchedMovieInsert(customerId: Long, movieId: Long){
        client.sql("INSERT into customer_watched_movies values ($1, $2)")
            .bind(0, customerId)
            .bind(1, movieId)
            .await()
    }

    suspend fun checkIfCustomerWatchedMovie(customerId: Long, movieId: Long) =
        client.sql("SELECT 1 from customer_watched_movies where customer_id = $customerId and watched_movie_id = $movieId")
            .fetch().awaitSingleOrNull() != null

    suspend fun movieActorInsert(movieId: Long, ActorId: Long){
        client.sql("INSERT into movie_actor values ($1, $2)")
            .bind(0, movieId)
            .bind(1, ActorId)
            .await()
    }

    suspend fun movieActorInsert(movieId: Long, actorIds: List<Long>){
        for (id in actorIds)
            client.sql("INSERT into movie_actor values ($1, $2)")
                .bind(0, movieId)
                .bind(1, id)
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

    suspend fun tagMovieInsert(tagIds: List<Long>, movieId: Long) {
        for (id in tagIds)
            client.sql("INSERT into tag_movie values ($1, $2)")
                .bind(0, id)
                .bind(1, movieId)
                .await()
    }

}