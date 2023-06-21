package com.example.firesenseapp

import android.content.Context
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.widget.Toast
import com.example.firesense_app.R
import java.io.*
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.collections.ArrayList
import org.json.JSONObject

class Utils(val mContext: Context) {

    // val dir =       "/storage/emulated/0/Android/data/com.example.firesense_app/files"
    // val interna =   "/storage/emulated/0/Android/data/com.example.firesense_app/files"
    var RAIZ_PATH = Environment.getExternalStorageDirectory().toString() + "/Android/data/"
    var FIRES_PATH = RAIZ_PATH + "/Fires"

    fun showToast(msj: String) {
        Handler(Looper.getMainLooper())
                .post(Runnable { Toast.makeText(mContext, msj, Toast.LENGTH_SHORT).show() })
    }

    fun oldDate(fires: ArrayList<Fire>): String {
        var auxFire: Fire? = null
        for (fire in fires) {
            if (auxFire == null) auxFire = fires.first()
            if (fire.date.isBefore(auxFire.date)) auxFire = fire
        }
        val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy") // HH:mm:ss
        return auxFire!!.date.format(formatterDate)
    }

    fun currentDate(fires: ArrayList<Fire>): String {
        var auxFire: Fire? = null
        for (fire in fires) {
            if (auxFire == null) auxFire = fires.first()
            if (fire.date.isAfter(auxFire.date)) auxFire = fire
        }
        val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy") // HH:mm:ss
        return auxFire!!.date.format(formatterDate)
    }

    fun unzipin() {
        var path: String = RAIZ_PATH + "/" + mContext.packageName + "/"
        ProcessBuilder()
                .command("unzip", path + "Fires/mis.zip")
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor()
    }

    fun unpackZip(fire: String, mision: String): Boolean {
        Log.i("RAIZ_PATH", RAIZ_PATH + "/" + mContext.packageName)
        Log.i("mContext.getPackageName()", mContext.packageName)
        var path: String = RAIZ_PATH + "/" + mContext.packageName + "/"
        var zipname: String = "Fires/mis.zip"
        val ias: InputStream
        val zis: ZipInputStream
        try {
            var filename: String
            ias =
                    FileInputStream(
                            "/storage/emulated/0/Android/data/com.example.firesense_app/mis.zip"
                    )
            Log.i("FileInputStream", path + zipname)
            zis = ZipInputStream(BufferedInputStream(ias))
            var ze: ZipEntry? = null
            val buffer = ByteArray(1024)
            var count: Int = 0
            while (zis.nextEntry.also({ ze = it }) != null) {
                //    Log.i("RAIZ_PATH", "while")
                filename = ze!!.name
                if (ze!!.isDirectory) {
                    //    Log.i("RAIZ_PATH", "isDirectory")
                    val fmd = File(path + filename)
                    fmd.mkdirs()
                    continue
                }
                val fout = FileOutputStream(path + filename)
                while (zis.read(buffer).also({ count = it }) != -1) {
                    fout.write(buffer, 0, count)
                }
                fout.close()
                zis.closeEntry()
            }
            zis.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        var sourcePath =
                Paths.get(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/var/www/html/FIRES/" +
                                fire +
                                "/" +
                                mision +
                                "/RGB"
                )
        var targetPath =
                Paths.get(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/Fires/" +
                                fire +
                                "/" +
                                mision +
                                "/RGB"
                )
        var dir =
                File(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/Fires/" +
                                fire +
                                "/" +
                                mision +
                                "/RGB"
                )
        deleteRecursive(dir)
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)

        sourcePath =
                Paths.get(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/var/www/html/FIRES/" +
                                fire +
                                "/" +
                                mision +
                                "/MOSAICO"
                )
        targetPath =
                Paths.get(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/Fires/" +
                                fire +
                                "/" +
                                mision +
                                "/MOSAICO"
                )
        dir =
                File(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/Fires/" +
                                fire +
                                "/" +
                                mision +
                                "/MOSAICO"
                )
        deleteRecursive(dir)
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)

        sourcePath =
                Paths.get(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/var/www/html/FIRES/" +
                                fire +
                                "/" +
                                mision +
                                "/THERMAL"
                )
        targetPath =
                Paths.get(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/Fires/" +
                                fire +
                                "/" +
                                mision +
                                "/THERMAL"
                )
        dir =
                File(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/Fires/" +
                                fire +
                                "/" +
                                mision +
                                "/THERMAL"
                )
        deleteRecursive(dir)
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)

        dir = File(RAIZ_PATH + "/" + mContext.packageName + "/var")
        deleteRecursive(dir)

        return true
    }

    // verifica si esta  ver mejor forma, por ahora solo si hay imagenes
    fun isImported(mision: String, fire: String): Boolean {

        val fRGB =
                File(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/FIRES/" +
                                fire +
                                "/" +
                                mision +
                                "/THERMAL"
                )
        val fTHERMAL =
                File(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/FIRES/" +
                                fire +
                                "/" +
                                mision +
                                "/RGB"
                )
        val fMOSAIC =
                File(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/FIRES/" +
                                fire +
                                "/" +
                                mision +
                                "/MOSAICO"
                )
        Log.i("fRGB", "" + fRGB)
        Log.i("fTHERMAL", "" + fRGB.list().size)
        Log.i("fMOSAIC", "" + fMOSAIC)

        if (fRGB.isDirectory && fTHERMAL.isDirectory && fMOSAIC.isDirectory) {
            if (fRGB.list().size > 0 && fTHERMAL.list().size > 0 && fMOSAIC.list().size > 0) {
                return true
            }
        }
        return false
    }

    fun zipAll(directory: String, zipFile: String) {
        val sourceFile = File(directory)
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use {
            it.use { zipFiles(it, sourceFile, "") }
        }
    }

    private fun zipFiles(zipOut: ZipOutputStream, sourceFile: File, parentDirPath: String) {
        val data = ByteArray(2048)
        for (f in sourceFile.listFiles()) {
            if (!f.name.contains("json"))
                    if (f.isDirectory) {
                        val entry = ZipEntry(f.name + File.separator)
                        entry.time = f.lastModified()
                        entry.isDirectory
                        entry.size = f.length()
                        Log.i("zip", "Adding Directory: " + f.name)
                        zipOut.putNextEntry(entry)
                        // Call recursively to add files within this directory
                        zipFiles(zipOut, f, f.name)
                    } else {
                        if (!f.name.contains(".zip")
                        ) { // If folder contains a file with extension ".zip", skip it
                            if (!f.name.contains(".txt"))
                                    FileInputStream(f).use { fi ->
                                        BufferedInputStream(fi).use { origin ->
                                            val path = parentDirPath + File.separator + f.name
                                            Log.i("zip", "Adding file: $path")
                                            val entry = ZipEntry(path)
                                            entry.time = f.lastModified()
                                            entry.isDirectory
                                            entry.size = f.length()
                                            zipOut.putNextEntry(entry)
                                            while (true) {
                                                val readBytes = origin.read(data)
                                                if (readBytes == -1) {
                                                    break
                                                }
                                                zipOut.write(data, 0, readBytes)
                                            }
                                        }
                                    }
                        } else {
                            zipOut.closeEntry()
                            zipOut.close()
                        }
                    }
        }
    }

    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory)
                for (child in fileOrDirectory.listFiles()) deleteRecursive(child)
        fileOrDirectory.delete()
    }

    fun deleteRecursiveMision(mision: String, fire: String, folder: String) {
        var fileOrDirectory =
                File(
                        RAIZ_PATH +
                                "/" +
                                mContext.packageName +
                                "/Fires/" +
                                fire +
                                "/" +
                                mision +
                                "/" +
                                folder
                )
        if (fileOrDirectory.isDirectory)
                for (child in fileOrDirectory.listFiles()) deleteRecursive(child)
        fileOrDirectory.delete()
    }

    fun getRaizPath(): String {
        return RAIZ_PATH + mContext.packageName.toString()
    }

    fun getFirePath(): String {
        return RAIZ_PATH + mContext.packageName.toString() + "/FIRES"
    }

    // verifica si existe un incendio con el mismo nombre
    fun existFire(fires: ArrayList<Fire>, fireName: String): Boolean {
        for (fire in fires) if (fire.name
                        .lowercase(Locale.getDefault())
                        .equals(fireName.lowercase(Locale.getDefault()))
        )
                return true
        return false
    }

    // verifica si existe una mision con el mismo nombre
    fun existMission(missions: ArrayList<Mission>, missionName: String): Boolean {
        for (mission in missions) if (mission.name
                        .lowercase(Locale.getDefault())
                        .equals(missionName.lowercase(Locale.getDefault()))
        )
                return true
        return false
    }

    fun delFireList(fires: ArrayList<Fire>, name: String): ArrayList<Fire> {
        for (fire in fires) if (fire.name.equals(name)) {
            fires.remove(fire)
            break
        }
        return fires
    }

    fun dirPath(path: String) {
        Log.i("dirPath_CREARDIRECTORIO", path)
        var file = File(path)
        if (!file.exists()) {
            Log.i("NOEXISTE", path)
            var zx = file.mkdirs()
            Log.i("CREADO?", zx.toString())
        } else Log.i("EXISTE", path)
    }

    fun createFile(file: String, str: String) {

        val fileNameSetting = getRaizPath() + "/" + file
        val myfileSetting = File(fileNameSetting)
        if (!myfileSetting.exists()) myfileSetting.writeText(str)
    }

    fun createRaizDir() {
        Log.i("createRaizDir", RAIZ_PATH + mContext.packageName)
        Log.i("createRaizDir1", mContext.packageName)
        // var path1 :String = RAIZ_PATH+mContext.getPackageName()
        dirPath(
                RAIZ_PATH + mContext.packageName
        ) // crea /storage/emulated/0/Android/data/com.example.firesense_app
        dirPath(RAIZ_PATH + mContext.packageName + "/FIRES/")
        val fileName = getRaizPath() + "/firesJetson.json"
        val myfile = File(fileName)
        if (!myfile.exists()) myfile.writeText("{\"command\":\"GETFIRES\",\"fires\":[]}")
        createFile(
                "jetsonSetting.json",
                "{\"api\":\"ERROR\",\"available\":0,\"capacity\":0,\"command\":\"GETINFO\",\"free\":0,\"height\":0,\"resolution\":\"640x480\",\"rgb\":\"OK\",\"state\":\"success\",\"thermal\":\"OK\",\"usb\":\"OK\",\"velocity\":0}"
        )
        /* val fileNameSetting = getRaizPath() + "/jetsonSetting.json"
                val myfileSetting = File(fileNameSetting)

                if(!myfileSetting.exists())
                    myfileSetting.writeText("{\"api\":\"ERROR\",\"available\":8415100928,\"capacity\":29458731008,\"command\":\"GETINFO\",\"free\":9935114240,\"height\":29458731008,\"resolution\":\"640x480\",\"rgb\":\"OK\",\"state\":\"success\",\"thermal\":\"OK\",\"usb\":\"OK\",\"velocity\":29458731008}")
        */
        //   var path :String = RAIZ_PATH+mContext.getPackageName()+"/FIRES/"
        //   Log.i("createRaizDir", path1)
        //  Log.i("createRaizDir1", path)

        /*   var file = File(path)
        if (!file.exists()) {
            Log.i("NOEXISTE", path)
            file.mkdirs()
          //  File(path+"/FIRES/").mkdir()
        }
        else
            Log.i("EXISTE", path)*/
    }

    // sino existe el archivo de configuracion se crea con valores por defecto
    fun createSettingFile() {
        val jsonObject1 = JSONObject()
        jsonObject1.accumulate("altitud", 100)
        jsonObject1.accumulate("velocity", 3)
        jsonObject1.accumulate("resolution", "640x480")

        /*  var file = File(RAIZ_PATH)
        if (!file.exists()) {
            file.mkdirs()
            File(FIRES_PATH).mkdir()
        }*/
    }

    fun createFiresPath(fires: ArrayList<Fire>) {
        Log.i("createFiresPath", "createFiresPaasdth")
        for (fire in fires) {
            dirPath(RAIZ_PATH + mContext.packageName + "/FIRES/" + fire.name)
            val myfile =
                    File(
                            RAIZ_PATH +
                                    mContext.packageName +
                                    "/FIRES/" +
                                    fire.name +
                                    "/" +
                                    "/json.json"
                    )
            myfile.delete()
            myfile.writeText(fire.getJson().toString())
            for (mission in fire.missions) {
                dirPath(
                        RAIZ_PATH +
                                mContext.packageName +
                                "/FIRES/" +
                                fire.name +
                                "/" +
                                mission.name
                )
                val myfile =
                        File(
                                RAIZ_PATH +
                                        mContext.packageName +
                                        "/FIRES/" +
                                        fire.name +
                                        "/" +
                                        mission.name +
                                        "/json.json"
                        )
                myfile.delete()
                myfile.writeText(mission.getJson(fire.name).toString())
                dirPath(
                        RAIZ_PATH +
                                mContext.packageName +
                                "/FIRES/" +
                                fire.name +
                                "/" +
                                mission.name +
                                "/MOSAICO"
                )
                dirPath(
                        RAIZ_PATH +
                                mContext.packageName +
                                "/FIRES/" +
                                fire.name +
                                "/" +
                                mission.name +
                                "/RGB"
                )
                dirPath(
                        RAIZ_PATH +
                                mContext.packageName +
                                "/FIRES/" +
                                fire.name +
                                "/" +
                                mission.name +
                                "/THERMAL"
                )
            }
        }
    }

    fun createMissionPath(fire: String, mision: String) {
        val missionPath = File(FIRES_PATH + "/" + fire + "/" + mision)
        if (!missionPath.exists()) {
            missionPath.mkdir()
            Log.i("missionPath.absolutePath", missionPath.absolutePath)
            File(missionPath.absolutePath + "/MOSAICO").mkdir()
            File(missionPath.absolutePath + "/RGB").mkdir()
            File(missionPath.absolutePath + "/THERMAL").mkdir()
        }
    }

    fun imageReaderNew(root: File) {
        Log.i("downloadFilePath", "imageReaderNew")
        val fileList: ArrayList<File> = ArrayList()
        val listAllFiles = root.listFiles()

        if (listAllFiles != null && listAllFiles.size > 0) {
            for (currentFile in listAllFiles) {
                // if (currentFile.name.endsWith(".jpeg")) {
                // File absolute path
                Log.i("downloadFilePath", currentFile.absolutePath)
                // File Name
                Log.i("downloadFileName", currentFile.name)
                fileList.add(currentFile.absoluteFile)
                //    }
            }
            Log.w("fileList", "" + fileList.size)
        }
    }

    // crea la carpeta del incendio y el json con su informacion.

    fun createFire(fire: Fire) {
        // var fire:Fire? =null
        var path: String = RAIZ_PATH + mContext.packageName + "/Fires/" + fire.name
        val f = File(path)
        f.mkdir()
        Log.i("createFirecreateFire", RAIZ_PATH + mContext.packageName)
        Log.i("createFirecreateFire", path)
        Log.i("createFirecreateFire", path + "/json.json")
        File(path + "/json.json").writeText(fire.getJson().toString())

        /*
        if (f.exists()){//si existe retornamos que el incendio ya fue creado.
            imageReaderNew(f)
            Log.i("FILE", "EXISTE")
        }
        else{//si no existe la carpeta se crea
            //val fire:Fire = Fire(json.getString("name"),)
           // fire = Fire(name,LocalDateTime.parse(json.getString("fecha")),json.getString("localidad"),-35.282501, -71.256751,
           //     R.drawable.photo_male_1)
            f.mkdir()
            File(path+"/json.json").writeText(json.toString())
            Log.i("FILE", "NO EXISTE")
        }*/
        // return fire
    }

    fun addMissionToFire(fire: ArrayList<Fire>): ArrayList<Fire> {
        return fire
    }

    fun createAllDir(fires: ArrayList<Fire>) {
        Log.i("createAllDir", "NcreateAllDir")
        for (fire in fires) {
            val path: String = FIRES_PATH + "/" + fire.name
            val f = File(path)
            Log.i("File(path)", path)
            if (!f.exists()) {
                Log.i("!f.exists()", path)
                f.mkdir()
            }
        }
    }

    fun writeFireJetson(json: JSONObject) {}

    fun existFile(name: String): Boolean {
        val path: String = RAIZ_PATH + mContext.packageName + "/" + name
        val f = File(path)
        return f.exists()
    }

    // verifica el wifi y activa si no está activado
    fun isConnectingToInternet(): Boolean {
        var wifiManager: WifiManager
        wifiManager = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(mContext, "Wifi desactivado ... encendiendo...", Toast.LENGTH_LONG)
                    .show()
            wifiManager.isWifiEnabled = true
        } else {
            Toast.makeText(mContext, "Wifi activado", Toast.LENGTH_LONG).show()
            var info = wifiManager.connectionInfo
            Log.i("isConnectingToInternte", info.ssid)
            if (info.ssid.equals("\"FireSense\""))
                    Toast.makeText(mContext, "Conectado a red FireSense...", Toast.LENGTH_LONG)
                            .show()
            else Toast.makeText(mContext, "Conectese a red FireSense...", Toast.LENGTH_LONG).show()
        }
        return false
    }

    fun createMission(name: String, fireName: String, json: JSONObject) {
        val path: String = RAIZ_PATH + mContext.packageName + "/Fires/" + fireName + "/" + name
        val f = File(path)
        if (f.exists()) { // si existe retornamos que el incendio ya fue creado.
            // imageReaderNew(f)
            Log.i("FILE", path)
        } else { // si no existe la carpeta se crea
            Log.i("createMission", "EXISTE")
            Log.i("FILE", path)
            f.mkdir()
            File(path + "/MOSAICO").mkdir()
            File(path + "/RGB").mkdir()
            File(path + "/THERMAL").mkdir()
            File(path + "/json.json").writeText(json.toString())
            Log.i("FILE", "NO EXISTE")
        }
    }

    fun reWriteJson(name: String, fireName: String, json: JSONObject) {
        val path: String = RAIZ_PATH + mContext.packageName + "/Fires/" + fireName + "/" + name
        File(path + "/json.json").writeText(json.toString())
    }

    fun fireExists() {}

    // elimina un incendio
    fun dellFire(fire: Fire, mision: Mission?) {
        var path: String = FIRES_PATH + "/" + fire.name
        if (mision != null) path = path + "/" + mision.name

        val f = File(path)
        var a = f.deleteRecursively()
        Log.i("dellFire", path + " : " + a)
    }

    fun editFire(fire: Fire, name: String, location: String): JSONObject {
        val path: String = FIRES_PATH + "/" + fire.name
        val oldfolder = File(FIRES_PATH + "/" + fire.name)
        val newfolder = File(FIRES_PATH + "/" + name)
        Log.i(
                "editFire",
                oldfolder.toString() +
                        " - " +
                        newfolder.toString() +
                        " - " +
                        oldfolder.renameTo(newfolder)
        )
        /*  val jsonObject1 = JSONObject()
        jsonObject1.accumulate("name", name)
        jsonObject1.accumulate("fecha", formatted)
        jsonObject1.accumulate("localidad", loc)*/

        val inputStream: InputStream = File(newfolder.path + "/json.json").inputStream()
        val inputString = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(inputString)
        val jsonObject1 = JSONObject()
        jsonObject1.accumulate("name", name)
        jsonObject1.accumulate("fecha", jsonObject.get("fecha"))
        jsonObject1.accumulate("localidad", location)
        File(newfolder.path + "/json.json").writeText(jsonObject1.toString())
        Log.i("jsonObject", jsonObject.toString())
        return jsonObject1
    }

    fun missionExists() {}

    fun readLocalImg(fire: String, mission: String, folder: String): ArrayList<String> {
        Log.i("readLocalImg", "readLocalImg")
        var arrayStr = ArrayList<String>()
        Log.i("readLocalImg", getRaizPath() + "/FIRES/" + fire + "/" + mission + "/" + folder)
        File(getRaizPath() + "/FIRES/" + fire + "/" + mission + "/" + folder).walk().forEach {
            if (!it.name.equals(folder)) {
                arrayStr.add(it.name)
                println(it)
            }
        }
        return arrayStr
    }

    fun readFires(): ArrayList<Fire> {
        // if()
        val inputStream: InputStream = File(getRaizPath() + "/firesJetson.json").inputStream()
        val inputString = inputStream.bufferedReader().use { it.readText() }
        var fires = ArrayList<Fire>()

        val msj: String = inputString.toString()
        Log.i("readFires", inputString)
        println(inputString)
        var jsonResponse: JSONObject? = null // JSONObject(inputStream)
        jsonResponse = JSONObject(msj)
        val jsonFires = jsonResponse.getJSONArray("fires")

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
        return fires

        // Log.i("readFiresjsonResponse", msj)
        // val jsonFires = jsonResponse.getJSONArray("fires")
        // Log.i("readFiresjsonFires", jsonFires.toString())
        /*  val jsonFires = JSONObject(inputStream)!!.getJSONArray("fires")

        val f:File = File(dir)
        val fires = ArrayList<Fire>()
        if (f.exists()){
            imageReaderNew(f)
            Log.i("FILE", "EXISTE")
        }/*
        else{
            f.mkdir()
            Log.i("FILE", "NO EXISTE")

        }*/
        Log.i("readFires", f.toString())
        for (item in f.listFiles()){
            Log.i("readFires", item.toString())
            fires.add(createFire(item.toString()))
            //readMission(item.toString())
            // item.
        }
        return fires*/

    }

    /*fun createFire(fire:Fire): Boolean {

        val inputStream: InputStream = File(FIRES_PATH+"/json.json").inputStream()
        //val inputStream: InputStream = File(path+"/json.json").inputStream()
        val inputString = inputStream.bufferedReader().use { it.readText() }
        Log.i("createFire", "NO EXISTE")
        println(inputString)
        Log.i("createFire", inputString)
        val jsonObject = JSONObject(inputString)

        //val jsonData = JSONObject(inputString)

        //val fire = Fire(name,LocalDateTime.parse(fecha),localidad,-35.282501, -71.256751)
       // createMissions(path,fire)
        return true

    }*/

    fun createMissions(path: String, fire: Fire) {
        val f = File(path)
        for (item in f.listFiles()) {
            if (!item.toString().contains(".json")) {
                val inputStream: InputStream = File(item.toString() + "/json.json").inputStream()
                val inputString = inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(inputString)

                val name = jsonObject.get("name").toString()
                val date = jsonObject.get("date").toString()
                val alt = jsonObject.get("alt").toString().toInt()
                val fireName = jsonObject.get("fireName").toString()
                //  val mission = Mission(name,LocalDateTime.parse(date),alt,-35.284603,
                // -71.256804,fireName,R.drawable.photo_female_2)
                // fire.addMission(mission)
                Log.i("readMission", item.toString())
            }
        }
    }

    // elimina una mision de la memoria
    fun removeMission(path: String, fire: Fire) {}

    // elimina un incendio junto a todas sus misiones.
    fun removeFire(path: String, fire: Fire) {}

    fun getLastMission(fires: ArrayList<Fire>): Mission? {
        var auxMission: Mission? = null
        for (fire in fires) {
            if (auxMission == null && !fire.missions.isEmpty()) auxMission = fire.missions.first()
            for (mission in fire.missions) {
                if (mission.date.isAfter(auxMission!!.date)) auxMission = mission
            }
        }

        return auxMission
    }

    fun getFire(fires: ArrayList<Fire>, mission: Mission?): Fire? {
        if (mission != null)
                for (fire in fires) {
                    if (fire.name.equals(mission.fireName)) return fire
                }
        return null
    }

    fun exitFireName(fires: ArrayList<Fire>, fire: Fire, name: String): Boolean {
        for (fireAux in fires) {
            if (fireAux.name.equals(fire.name)) return true
            /* Log.i("exitFireName", fire.name+" - "+fireAux.name+" - "+name+"  = "+fireAux.name.equals(name)+" && "+!fireAux.equals(fire))
            if (fireAux.name.equals(name) && !(fireAux.name.equals(fire.name))) {
                Log.i("nole", "NO EXISTE"+fireAux.toString()+" - "+fire.toString())
                return true
            }*/

        }
        return false
    }

    // obtener nombre conexion actual, si la red es fireSense
    fun checkConnectFireSense(): Boolean {

        var wifiManager: WifiManager
        wifiManager = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        return wifiManager.isWifiEnabled
    }

    // retorna las coordenadas del gps
    fun getCoordinates() {}

    fun setIcon(menu_: Menu, comm: Communication, showToast: Boolean) {
        var client: Socket? = null
        AsyncTask.execute { client = comm.getClientConnection() }
        Handler()
                .postDelayed(
                        {
                            if (client == null) {
                                if (showToast) this.showToast("Sin conexión a FireSense")
                                menu_.findItem(R.id.connectIcon).setIcon(R.drawable.noconnect)
                            } else {
                                client!!.close()
                                // if (showToast)
                                // this.showToast("Conectado a FireSense")
                                menu_.findItem(R.id.connectIcon).setIcon(R.drawable.connect)
                            }
                        },
                        500
                )
    }
}
