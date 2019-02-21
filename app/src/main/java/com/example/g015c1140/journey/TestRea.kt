package com.example.g015c1140.journey

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class TestRea(
        @PrimaryKey open var id: String = UUID.randomUUID().toString(),
        @Required open var name: String = "",
        open var latitude: Double = 0.0,
        open var longitude: Double = 0.0,
        open var comment: String = "",
        open var datetime: Date = Date(),
        open var image_A: String = "",
        open var image_B: String = "",
        open var image_C: String = ""
) : RealmObject()