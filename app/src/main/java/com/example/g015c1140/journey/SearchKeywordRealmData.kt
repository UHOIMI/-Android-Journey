package com.example.g015c1140.journey

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class SearchKeywordRealmData(
        @PrimaryKey open var id: String = UUID.randomUUID().toString(),
        @Required open var keyword: String = ""
) : RealmObject()