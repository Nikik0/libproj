package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.AddressEntity
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitFirst
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class CustomAddressRepository(
    private val template: R2dbcEntityTemplate,
    private val client: DatabaseClient
) {
    suspend fun findForCustomerId(id: Long): AddressEntity =
        template.select(AddressEntity::class.java).matching(Query.query(
            Criteria.where("id").`is`(id)
        )).awaitFirst()

    suspend fun ff(id: Long): AddressEntity? =
        client.sql("select * from address a join customer_address ca on a.id = ca.address_id where ca.customer_id = $1 limit 1")
            .bind(0,id)
            .map { row -> AddressEntity(
                row.get("id", Long::class.java)!!,
                row.get("country", String::class.java)!!,
                row.get("state", String::class.java)!!,
                row.get("city", String::class.java)!!,
                row.get("district", String::class.java)!!,
                row.get("street", String::class.java)!!,
                row.get("building", Int::class.java)!!,
                row.get("buildingLiteral", String::class.java)!!,
                row.get("apartmentNumber", Int::class.java)!!,
                row.get("additionalInfo", String::class.java)!!
            ) }
            //.fetch()
            .first()
            .awaitSingleOrNull() as AddressEntity?


}