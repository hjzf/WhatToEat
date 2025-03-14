package com.lkjhgfdsa.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lkjhgfdsa.R
import java.util.UUID

enum class FoodType(val value: String, val icon: Int) {
    TYPE_1("TYPE_1", R.drawable.ic_type_1),
    TYPE_2("TYPE_2", R.drawable.ic_type_2),
    TYPE_3("TYPE_3", R.drawable.ic_type_3),
    TYPE_4("TYPE_4", R.drawable.ic_type_4),
    TYPE_5("TYPE_5", R.drawable.ic_type_5),
    NULL("NULL", R.drawable.ic_transparent);
}

@Entity(tableName = "option")
data class Option(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val type: String = FoodType.NULL.value,
)

fun Option.foodType(): FoodType {
    return when (this.type) {
        FoodType.TYPE_1.value -> FoodType.TYPE_1
        FoodType.TYPE_2.value -> FoodType.TYPE_2
        FoodType.TYPE_3.value -> FoodType.TYPE_3
        FoodType.TYPE_4.value -> FoodType.TYPE_4
        FoodType.TYPE_5.value -> FoodType.TYPE_5
        else -> FoodType.NULL
    }
}