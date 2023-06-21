package com.example.firesenseapp

import java.io.Serializable
import java.time.LocalDateTime
import org.json.JSONObject

// var name :String,var date: Date,var alt: Int,var lat: Double,var lon: Double
class Mission(
        var name: String,
        var date: LocalDateTime,
        var alt: Int,
        var lat: Double,
        var lon: Double,
        var fireName: String,
        var description: String,
        var imageUrl: Int,
        var state: String
) : Serializable {

    fun getJson(fire: String): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.accumulate("name", name)
        jsonObject.accumulate("date", date.toString())
        jsonObject.accumulate("alt", alt)
        jsonObject.accumulate("latitude", lat)
        jsonObject.accumulate("longitude", lon)
        jsonObject.accumulate("fire", fire)
        jsonObject.accumulate("state", state)
        jsonObject.accumulate("fireName", fireName)
        jsonObject.accumulate("description", description)
        return jsonObject
    }
}
