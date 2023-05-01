package models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Noodles :IntIdTable(){
    val name = varchar("name",128).index()
    val user = reference("user",Users)
}

class Noodle(id:EntityID<Int>):IntEntity(id){
    companion object:IntEntityClass<Noodle>(Noodles)

    var name by Noodles.name
    var user by User referencedOn Noodles.user
}