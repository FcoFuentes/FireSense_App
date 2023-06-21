package com.example.firesenseapp

import android.util.Log
import com.example.firesense_app.Command
import com.example.firesense_app.R
import java.io.File
import java.io.IOException
import java.net.Socket
import java.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject

class Communication {
    var ipJetson: String = "192.168.1.19"
    var port: Int = 8080 // 65321//8080

    fun getClientConnection(): Socket? {
        Log.i("getClientConnection", "getClientConnection")
        try {
            val client = Socket(ipJetson, port)
            Log.i("testConnect", client.toString())
            return client
        } catch (e: IOException) { // Log the exception
            Log.i("testConnect", "error")
            return null
        }
    }

    fun executeAction(client: Socket?, command: Command): Boolean {
        val jsonObject = JSONObject()
        jsonObject.accumulate("command", command)
        jsonObject.accumulate("data", JSONObject())
        client!!.outputStream.write(("" + jsonObject).toByteArray())
        var msj: String = client.inputStream.read().toChar().toString()
        var i = 0
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
            i++
        }
        var jsonResponse = JSONObject(msj)
        return true
    }

    fun getFiresJetson(utils: Utils, client: Socket?): ArrayList<Fire> {
        var fires = ArrayList<Fire>()
        var jsonResponse: JSONObject? = null
        val jsonObject1 = JSONObject()
        val jsonObject = JSONObject()
        jsonObject.accumulate("command", Command.GETFIRES)
        jsonObject.accumulate("data", jsonObject1)
        client!!.outputStream.write(("" + jsonObject).toByteArray())
        var msj: String = client.inputStream.read().toChar().toString()
        var i = 0
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
            i++
        }
        jsonResponse = JSONObject(msj)
        val fileName = utils.getRaizPath() + "/firesJetson.json"
        val myfile = File(fileName)
        myfile.delete()
        myfile.writeText(msj)
        val jsonFires = jsonResponse!!.getJSONArray("fires")
        Log.i("jsonFires", jsonFires.toString())
        for (i in 0 until jsonFires.length()) {
            val item = jsonFires.getJSONObject(i)
            val fire: Fire =
                    Fire(
                            item.get("name").toString(),
                            LocalDateTime.parse(item.get("date").toString()),
                            item.get("location").toString(),
                            item.get("latitude") as Double,
                            item.get("longitude") as Double,
                            item.get("description").toString(),
                            R.drawable.fire2
                    )
            for (j in 0 until item.getJSONArray("misions").length()) {
                val itemMision = item.getJSONArray("misions").getJSONObject(j)
                fire.addMission(
                        Mission(
                                itemMision.get("name").toString(),
                                LocalDateTime.parse(itemMision.get("date").toString()),
                                itemMision.get("alt").toString().toInt(),
                                itemMision.get("latitude") as Double,
                                itemMision.get("longitude") as Double,
                                itemMision.get("fireName").toString(),
                                itemMision.get("description").toString(),
                                R.drawable.drone1,
                                itemMision.get("state").toString()
                        )
                )
            }
            fires.add(fire)
        }
        if (client != null) {
            //  client!!.close()
        }
        utils.createFiresPath(fires)
        return fires
    }

    fun getInfo(): JSONObject {
        Log.i("getInfo", "getInfo")
        val client = Socket(ipJetson, port)
        val jsonObject1 = JSONObject()
        val jsonObject = JSONObject()
        jsonObject.accumulate("command", Command.GETINFO)
        // jsonObject.accumulate("name", name)
        jsonObject.accumulate("data", jsonObject1)
        client.outputStream.write(("" + jsonObject).toByteArray())
        var msj: String = client.inputStream.read().toChar().toString()
        Log.i("COMMUNICA", "SOCKETT")
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
        }
        val jsonResponse = JSONObject(msj)
        Log.i("getInfo", jsonResponse.toString())
        client.close()
        return jsonResponse
    }

    fun testConnect(): Boolean {
        var client = getClientConnection()
        // si no hay comunicacion con jetson
        if (client == null) {
            return false
            Log.i("conexion", "conexion")
            //  showToast("Sin conexion a jetson")
        }
        // conexion con jetson
        else {
            return true
            Log.i("noconexion", "noconexion")
            //  showToast("con conexion a jetson")
            // firesJetson = comm.getFiresJetson(util,client)
        }
        /*
        try {
            val client = Socket(ipJetson, port)
            Log.i("testConnect", client.toString())
            client.close()
            return true
        } catch (e: IOException) { // Log the exception
            Log.i("testConnect", "error")
            return false
        }*/
    }

    fun sendMission(mission: Mission, fire: String, client: Socket?): Boolean {
        val jsonObject1 = mission.getJson(fire)
        val jsonObject = JSONObject()
        jsonObject.accumulate("command", Command.CREATEMISSION)
        jsonObject.accumulate("data", jsonObject1)
        client!!.outputStream.write(("" + jsonObject).toByteArray())
        var msj = client.inputStream.read().toChar().toString()
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
        }
        val jsonResponse = JSONObject(msj)
        return jsonResponse.get("state").toString().equals("success")
    }

    fun sendFire(fire: Fire, client: Socket?): Boolean {
        val jsonObject1 = fire.getJson()
        val jsonObject = JSONObject()
        jsonObject.accumulate("command", Command.CREATEFIRE)
        jsonObject.accumulate("data", jsonObject1)
        client!!.outputStream.write(("" + jsonObject).toByteArray())
        var msj: String = client!!.inputStream.read().toChar().toString()
        while (client!!.inputStream.available() != 0) {
            var a = client!!.inputStream.read().toChar()
            msj = msj + a.toString()
        }
        val jsonResponse = JSONObject(msj)
        return jsonResponse.get("state").toString().equals("success")
    }

    fun delFire(fire: String, mission: String): Boolean {
        Log.i("dellFire", "dellFire")
        val client = Socket(ipJetson, port)
        val jsonObject = JSONObject()
        jsonObject.accumulate("fire", fire)
        jsonObject.accumulate("mission", mission)
        jsonObject.accumulate("command", Command.DELFIRE)
        // jsonObject.accumulate("data", jsonObject1)

        client.outputStream.write(("" + jsonObject).toByteArray())
        var msj: String = client.inputStream.read().toChar().toString()
        // Log.i("dellFire", msj)
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
        }
        Log.i("dellFire", msj)
        val jsonResponse = JSONObject(msj)
        client.close()
        return jsonResponse.get("state").toString().equals("success")
    }

    fun delMission(name: String): Boolean {
        Log.i("dellFire", "dellFire")
        val client = Socket(ipJetson, port)
        val jsonObject = JSONObject()
        jsonObject.accumulate("fire", name)
        jsonObject.accumulate("mission", name)
        jsonObject.accumulate("command", Command.DELFIRE)
        // jsonObject.accumulate("data", jsonObject1)

        client.outputStream.write(("" + jsonObject).toByteArray())
        var msj: String = client.inputStream.read().toChar().toString()
        // Log.i("dellFire", msj)
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
        }
        Log.i("dellFire", msj)
        val jsonResponse = JSONObject(msj)
        client.close()
        return jsonResponse.get("state").toString().equals("success")
    }

    fun editFire(jsonObject1: JSONObject, name: String) {
        val client = Socket(ipJetson, port)
        jsonObject1.accumulate("nameOld", name)
        val jsonObject = JSONObject()
        jsonObject.accumulate("command", Command.EDITFIRE)
        // jsonObject.accumulate("name", name)
        jsonObject.accumulate("data", jsonObject1)
        client.outputStream.write(("" + jsonObject).toByteArray())
        var msj: String = client.inputStream.read().toChar().toString()
        Log.i("COMMUNICA", "SOCKETT")
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
        }
        // val jsonResponse = JSONObject(msj)
        client.close()
    }

    fun sendUsb(fire: String, mision: String): String {
        val client = Socket(ipJetson, port)
        val jsonObject1 = JSONObject()
        jsonObject1.accumulate("fire", fire)
        jsonObject1.accumulate("mission", mision)
        val jsonObject = JSONObject()
        jsonObject.accumulate("command", Command.EXPORTUSB)
        jsonObject.accumulate("data", jsonObject1)
        Log.i("JSON SEND USB 2", jsonObject.toString())

        client.outputStream.write(("" + jsonObject).toByteArray())
        var msj = client.inputStream.read().toChar().toString()
        Log.i("JSON SEND USB 3", msj)
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
        }
        val jsonResponse = JSONObject(msj)
        Log.i("JSON SEND USB", jsonResponse.toString())
        client.close()
        return jsonResponse.getString("state")
    }

    fun import(fire: String, mision: String): String {
        val client = Socket(ipJetson, port)
        val jsonObject1 = JSONObject()
        jsonObject1.accumulate("fire", fire)
        jsonObject1.accumulate("mission", mision)
        val jsonObject = JSONObject()
        jsonObject.accumulate("command", Command.IMPORT)
        jsonObject.accumulate("data", jsonObject1)
        client.outputStream.write(("" + jsonObject).toByteArray())
        var msj = client.inputStream.read().toChar().toString()
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
        }
        val jsonResponse = JSONObject(msj)
        Log.i("import", jsonResponse.toString())

        client.close()
        return jsonResponse.getString("state")
    }

    fun test_conn(fire: String, mision: String): String {
        val client = Socket(ipJetson, port)
        /*val jsonObject1 = JSONObject()
        jsonObject1.accumulate("fire", fire)
        jsonObject1.accumulate("mission", mision)*/
        val jsonObject = JSONObject()
        jsonObject.accumulate("command", Command.TESTCONN)
        // jsonObject.accumulate("data", jsonObject1)
        client.outputStream.write(("" + jsonObject).toByteArray())
        var msj = client.inputStream.read().toChar().toString()
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
        }
        val jsonResponse = JSONObject(msj)
        Log.i("import", jsonResponse.toString())

        client.close()
        return jsonResponse.getString("state")
    }

    fun getJsonImagesLocal(utils: Utils, fire: String, mission: String, folder: String): JSONArray {
        var path: String =
                utils.RAIZ_PATH +
                        "/" +
                        utils.mContext.getPackageName() +
                        "/Fires/" +
                        fire +
                        "/" +
                        mission +
                        "/RGB"
        Log.i("getJsonImagesLocal", path)
        val jsonObject = JSONArray()
        File(path).walkTopDown().forEach { if (!it.name.equals(folder)) jsonObject.put(it.name) }
        Log.i("getJsonImagesLocal", jsonObject.toString())
        return jsonObject
    }

    fun getJsonImages(fire: String, mision: String, folder: String): JSONArray {
        val client = Socket(ipJetson, port)
        val jsonObject1 = JSONObject()
        jsonObject1.accumulate("fire", fire)
        jsonObject1.accumulate("mission", mision)
        jsonObject1.accumulate("folder", folder)
        val jsonObject = JSONObject()
        jsonObject.accumulate("command", Command.GETIMAGES)
        jsonObject.accumulate("data", jsonObject1)
        client.outputStream.write(("" + jsonObject).toByteArray())
        var msj = client.inputStream.read().toChar().toString()
        while (client.inputStream.available() != 0) {
            var a = client.inputStream.read().toChar()
            msj = msj + a.toString()
        }

        val jsonResponse = JSONObject(msj)
        Log.i("JSONimages USB", jsonResponse.getJSONArray("data").toString())

        client.close()
        return jsonResponse.getJSONArray("data")
        //   return jsonResponse
    }
}
