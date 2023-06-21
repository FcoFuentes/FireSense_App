package com.example.firesenseapp

import android.util.Log
import java.io.Serializable
import java.time.LocalDateTime
import org.json.JSONObject

class Fire(
        val name: String,
        var date: LocalDateTime,
        var locationPlace: String,
        var lat: Double,
        var lon: Double,
        var description: String,
        var imageUrl: Int
) : Serializable {

    var missions = ArrayList<Mission>()

    fun getJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.accumulate("name", name)
        jsonObject.accumulate("fecha", date.toString())
        jsonObject.accumulate("localidad", locationPlace)
        jsonObject.accumulate("latitude", lat)
        jsonObject.accumulate("longitude", lon)
        jsonObject.accumulate("description", description)
        return jsonObject
    }

    fun addMission(mission: Mission) {
        Log.i("addMission", mission.name)
        this.missions.add(mission)
    }
}
