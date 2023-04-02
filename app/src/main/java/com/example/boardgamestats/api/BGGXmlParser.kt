package com.example.boardgamestats.api

import android.util.Log.d
import com.example.boardgamestats.models.BoardGame
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

fun parseXml(inputStream: InputStream): List<BoardGame> {
    val factory = XmlPullParserFactory.newInstance()
    factory.isNamespaceAware = true
    val parser = factory.newPullParser()
    parser.setInput(inputStream, null)

    val boardGames = mutableListOf<BoardGame>()
    var eventType = parser.eventType

    var currentId: Int? = null
    var currentName: String? = null
    var currentYear: Int? = null
    var currentThumbnail: String? = null
    var currentImage: String? = null
    var currentDescription: String? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                when (parser.name) {
                    "item" -> {
                        if (currentId == null) {
                            currentId = parser.getAttributeValue(null, "id").toInt()
                        }
                    }

                    "name" -> {
                        if (currentName == null) {
                            currentName = parser.getAttributeValue(null, "value")
                        }
                    }

                    "yearpublished" -> {
                        if (currentYear == null) {
                            currentYear = parser.getAttributeValue(null, "value").toInt()
                        }
                    }

                    "thumbnail" -> {
                        if (currentThumbnail == null) {
//                            d("currentThumbnail", parser.nextText())
                            currentThumbnail = parser.nextText()
                        }
                    }

                    "image" -> {
                        if (currentImage == null) {
//                            d("currentImage", parser.nextText())
                            currentImage = parser.nextText()
                        }
                    }

                    "description" -> {
                        if (currentDescription == null) {
//                            d("currentDescription", parser.nextText())
                            currentDescription = parser.nextText()
                        }
                    }
                }
            }

            XmlPullParser.END_TAG -> {
                if (parser.name == "item") {
                    if (currentId != null && currentName != null && currentYear != null) {
                        boardGames.add(
                            BoardGame(
                                currentId,
                                currentName,
                                currentYear,
                                currentThumbnail,
                                currentDescription,
                                false,
                                currentImage
                            )
                        )
                    }
                    currentId = null
                    currentName = null
                    currentYear = null
                }
            }
        }
        eventType = parser.next()
    }
    return boardGames
}

fun queryXmlApi(apiUrl: String): List<BoardGame> {
    val url = URL(apiUrl)
    val connection = url.openConnection() as HttpURLConnection

    try {
        return parseXml(connection.inputStream)
    } finally {
        connection.disconnect()
    }
}
