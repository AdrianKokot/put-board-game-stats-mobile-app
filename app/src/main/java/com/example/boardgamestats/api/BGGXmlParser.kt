package com.example.boardgamestats.api

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class BoardGame(var id: String, var name: String, var publishYear: Int)

fun parseXml(inputStream: InputStream): List<BoardGame> {
    val factory = XmlPullParserFactory.newInstance()
    factory.isNamespaceAware = true
    val parser = factory.newPullParser()
    parser.setInput(inputStream, null)

    val boardGames = mutableListOf<BoardGame>()
    var eventType = parser.eventType
    var currentBoardGame: BoardGame? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                when (parser.name) {
                    "item" -> {
                        currentBoardGame = BoardGame(parser.getAttributeValue(null, "id"), "", 0)
                    }
                    "name" -> {
                        currentBoardGame?.name = parser.getAttributeValue(null, "value")
                    }
                    "yearpublished" -> {
                        currentBoardGame?.publishYear = parser.getAttributeValue(null, "value").toInt()

                    }
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "item") {
                    currentBoardGame?.let { boardGames.add(it) }
                    currentBoardGame = null
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
