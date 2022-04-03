/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package khttp

import khttp.extensions.fileLike
import khttp.helpers.StringIterable
import org.json.JSONArray
import org.json.JSONObject
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KHttpPostSpec : Spek({
    describe("a post request with raw data") {
        val request = post("http://httpbin.org/post", data = "Hello, world!")
        context("accessing json") {
            val json = request.jsonObject
            it("should contain the data") {
                assertEquals("Hello, world!", json.getString("data"))
            }
        }
    }
    describe("a post request with raw data") {
        val request = post("http://httpbin.org/post", data = "a=b&c=d")
        context("accessing json") {
            val json = request.jsonObject
            it("should contain the data") {
                assertEquals("a=b&c=d", json.getString("data"))
            }
        }
    }
    describe("a post form request") {
        val request = post("http://httpbin.org/post", data = mapOf("a" to "b", "c" to "d"))
        context("accessing json") {
            val json = request.jsonObject
            it("should contain the form data") {
                val form = json.getJSONObject("form")
                assertEquals("b", form.getString("a"))
                assertEquals("d", form.getString("c"))
            }
        }
    }
    describe("a request with json as a Map") {
        val jsonMap = mapOf("books" to listOf(mapOf("title" to "Pride and Prejudice", "author" to "Jane Austen")))
        val request = post("http://httpbin.org/post", json = jsonMap)
        context("accessing the json") {
            val json = request.jsonObject
            val returnedJSON = json.getJSONObject("json")
            val returnedBooks = returnedJSON.getJSONArray("books")
            it("should be the same length") {
                assertEquals(jsonMap.size, returnedJSON.length())
            }
            it("should have the same book length") {
                assertEquals(jsonMap.get("books")!!.size, returnedBooks.length())
            }
            val firstBook = jsonMap.get("books")!!.get(0)
            val firstReturnedBook = returnedBooks.getJSONObject(0)
            it("should have the same book title") {
                assertEquals(firstBook["title"], firstReturnedBook.getString("title"))
            }
            it("should have the same book author") {
                assertEquals(firstBook["author"], firstReturnedBook.getString("author"))
            }
        }
    }
    describe("a request with json as an Iterable") {
        val jsonArray = StringIterable("a word")
        val request = post("http://httpbin.org/post", json = jsonArray)
        context("accessing the json") {
            val json = request.jsonObject
            val returnedJSON = json.getJSONArray("json")
            it("should be equal") {
                assertEquals(jsonArray.string, String(returnedJSON.mapIndexed { i, _ -> returnedJSON.getString(i)[0] }.toCharArray()))
            }
        }
    }
    describe("a request with json as a List") {
        val jsonList = listOf("A thing", "another thing")
        val request = post("https://httpbin.org/post", json = jsonList)
        context("accessing the json") {
            val json = request.jsonObject
            val returnedJSON = json.getJSONArray("json")
            it("should have an equal first element") {
                assertEquals(jsonList[0], returnedJSON.getString(0))
            }
            it("should have an equal second element") {
                assertEquals(jsonList[1], returnedJSON.getString(1))
            }
        }
    }
    describe("a request with json as an Array") {
        val jsonArray = arrayOf("A thing", "another thing")
        val request = post("https://httpbin.org/post", json = jsonArray)
        context("accessing the json") {
            val json = request.jsonObject
            val returnedJSON = json.getJSONArray("json")
            it("should have an equal first element") {
                assertEquals(jsonArray[0], returnedJSON.getString(0))
            }
            it("should have an equal second element") {
                assertEquals(jsonArray[1], returnedJSON.getString(1))
            }
        }
    }
    describe("a request with json as a JSONObject") {
        val jsonObject = JSONObject("""{"valid": true}""")
        val request = post("https://httpbin.org/post", json = jsonObject)
        context("accessing the json") {
            val json = request.jsonObject
            val returnedJSON = json.getJSONObject("json")
            it("should have a true value for the key \"valid\"") {
                assertTrue(returnedJSON.getBoolean("valid"))
            }
        }
    }
    describe("a request with json as a JSONArray") {
        val jsonObject = JSONArray("[true]")
        val request = post("https://httpbin.org/post", json = jsonObject)
        context("accessing the json") {
            val json = request.jsonObject
            val returnedJSON = json.getJSONArray("json")
            it("should have a true value for the first key") {
                assertTrue(returnedJSON.getBoolean(0))
            }
        }
    }
    describe("a request with invalid json") {
        context("construction") {
            it("should throw an IllegalArgumentException") {
                assertFailsWith(IllegalArgumentException::class) {
                    post("https://httpbin.org/post", json = object {})
                }
            }
        }
    }
    describe("a file upload without form parameters") {
        val file = "hello".fileLike("derp")
        val response = post("https://httpbin.org/post", files = listOf(file))
        context("accessing the json") {
            val json = response.jsonObject
            val files = json.getJSONObject("files")
            it("should have one file") {
                assertEquals(1, files.length())
            }
            it("should have the same name") {
                assertNotNull(files.optString(file.fieldName, null))
            }
            it("should have the same contents") {
                assertEquals(file.contents.toString(Charsets.UTF_8), files.optString(file.fieldName))
            }
        }
    }
    describe("a file upload with form parameters") {
        val file = "hello".fileLike("derp")
        val params = mapOf("top" to "kek")
        val response = post("https://httpbin.org/post", files = listOf(file), data = params)
        context("accessing the json") {
            val json = response.jsonObject
            val files = json.getJSONObject("files")
            val form = json.getJSONObject("form")
            it("should have one file") {
                assertEquals(1, files.length())
            }
            it("should have the same name") {
                assertNotNull(files.optString(file.fieldName, null))
            }
            it("should have the same contents") {
                assertEquals(file.contents.toString(Charsets.UTF_8), files.optString(file.fieldName))
            }
            it("should have one parameter") {
                assertEquals(1, form.length())
            }
            it("should have the same name") {
                assertNotNull(form.optString("top", null))
            }
            it("should have the same contents") {
                assertEquals("kek", form.optString("top"))
            }
        }
    }
    describe("a streaming file upload") {
        // Get our file to stream (a beautiful rare pepe)
        val file = File("src/test/resources/rarest_of_pepes.png")
        val response = post("https://httpbin.org/post", data = file)
        context("accessing the data") {
            val json = response.jsonObject
            val data = json.getString("data")
            it("should start with a base64 header") {
                assertTrue(data.startsWith("data:application/octet-stream;base64,"))
            }
            val base64 = data.split("data:application/octet-stream;base64,")[1]
            val rawData = Base64.getDecoder().decode(base64)
            it("should be the same decoded content") {
                assertEquals(file.readBytes().asList(), rawData.asList())
            }
        }
    }
    describe("a streaming InputStream upload") {
        // Get our file to stream (a beautiful rare pepe)
        val file = File("src/test/resources/rarest_of_pepes.png")
        val inputStream = file.inputStream()
        val response = post("https://httpbin.org/post", data = inputStream)
        context("accessing the data") {
            val json = response.jsonObject
            val data = json.getString("data")
            it("should start with a base64 header") {
                assertTrue(data.startsWith("data:application/octet-stream;base64,"))
            }
            val base64 = data.split("data:application/octet-stream;base64,")[1]
            val rawData = Base64.getDecoder().decode(base64)
            it("should be the same decoded content") {
                assertEquals(file.readBytes().asList(), rawData.asList())
            }
        }
    }
    describe("a JSON request") {
        val expected = """{"test":true}"""
        val response = post("https://httpbin.org/post", json = mapOf("test" to true))
        context("accessing the request body") {
            val body = response.request.body
            it("should be the expected valid json") {
                assertEquals(expected, body.toString(Charsets.UTF_8))
            }
        }
    }
})
