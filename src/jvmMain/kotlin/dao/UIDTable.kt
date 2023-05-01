package dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import utils.UIDGenerator

open class UIDTable(name: String = "", columnName: String = "id") : IdTable<Long>(name) {
    final override val id: Column<EntityID<Long>> = long(columnName).autoGenerate().entityId()
    final override val primaryKey = PrimaryKey(id)
    @JvmName("autoGenerateUID")
    private fun Column<Long>.autoGenerate(): Column<Long> = clientDefault { UIDGenerator.nextUID() }
}
