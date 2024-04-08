package com.timber.soft.newkeyboard.tools

import android.content.Context
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Serializable
import java.nio.charset.StandardCharsets

object JsonDeserializer {

    fun parseJsonFromAssets(context: Context, fileName: String): List<RootModel>? {
        var dataItems: List<RootModel>? = null
        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            inputStream.close()
            reader.close()
            val gson = Gson()
            val dataItemArray =
                gson.fromJson(stringBuilder.toString(), Array<RootModel>::class.java)
            dataItems = dataItemArray.toList()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return dataItems
    }

}


data class RootModel(
    val className: String, val list: List<DataModel>
) : Serializable

data class DataModel(
    val preview: String, val thumb: String, val title: String, val zipUrl: String
) : Serializable