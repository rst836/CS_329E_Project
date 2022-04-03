/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package khttp

import khttp.helpers.SslContextUtils
import khttp.structures.authorization.BasicAuthorization
import org.json.JSONObject
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KHttpGetSpec : Spek({
    describe("a get request") {
        val url = "http://httpbin.org/range/26"
        val response = get(url)
        context("accessing the string") {
            val string = response.text
            it("should equal the alphabet in lowercase") {
                assertEquals("abcdefghijklmnopqrstuvwxyz", string)
            }
        }
        context("accessing the url") {
            val resultantURL = response.url
            it("should equal the starting url") {
                assertEquals(url, resultantURL)
            }
        }
        context("accessing the status code") {
            val statusCode = response.statusCode
            it("should be 200") {
                assertEquals(200, statusCode)
            }
        }
        context("converting it to a string") {
            val string = response.toString()
            it("should be correct") {
                assertEquals("<Response [200]>", string)
            }
        }
    }
    describe("a json object get request with parameters") {
        val response = get("http://httpbin.org/get", params = mapOf("a" to "b", "c" to "d"))
        context("accessing the json") {
            val json = response.jsonObject
            it("should contain the parameters") {
                val args = json.getJSONObject("args")
                assertEquals("b", args.getString("a"))
                assertEquals("d", args.getString("c"))
            }
        }
    }
    describe("a json object get request with a map of parameters") {
        val response = get("http://httpbin.org/get", params = mapOf("a" to "b", "c" to "d"))
        context("accessing the json") {
            val json = response.jsonObject
            it("should contain the parameters") {
                val args = json.getJSONObject("args")
                assertEquals("b", args.getString("a"))
                assertEquals("d", args.getString("c"))
            }
        }
    }
    describe("a get request with basic auth") {
        val response = get("http://httpbin.org/basic-auth/khttp/isawesome", auth = BasicAuthorization("khttp", "isawesome"))
        context("accessing the json") {
            val json = response.jsonObject
            it("should be authenticated") {
                assertTrue(json.getBoolean("authenticated"))
            }
            it("should have the correct user") {
                assertEquals("khttp", json.getString("user"))
            }
        }
    }
    describe("a get request with cookies") {
        val response = get("http://httpbin.org/cookies", cookies = mapOf("test" to "success"))
        context("accessing the json") {
            val json = response.jsonObject
            it("should have the same cookies") {
                val cookies = json.getJSONObject("cookies")
                assertEquals("success", cookies.getString("test"))
            }
        }
    }
    describe("a get request that redirects and allowing redirects") {
        val response = get("http://httpbin.org/redirect-to?url=http://httpbin.org/get")
        context("accessing the json") {
            val json = response.jsonObject
            it("should have the redirected url") {
                assertEquals("http://httpbin.org/get", json.getString("url"))
            }
        }
    }
    describe("a get request that redirects with HTTP 307 and allowing redirects") {
        val response = get("http://httpbin.org/redirect-to?status_code=307&url=http://httpbin.org/get")
        context("accessing the json") {
            val json = response.jsonObject
            it("should have the redirected url") {
                assertEquals("http://httpbin.org/get", json.getString("url"))
            }
        }
    }
    describe("a get request that redirects with HTTP 308 and allowing redirects") {
        val response = get("http://httpbin.org/redirect-to?status_code=308&url=http://httpbin.org/get")
        context("accessing the json") {
            val json = response.jsonObject
            it("should have the redirected url") {
                assertEquals("http://httpbin.org/get", json.getString("url"))
            }
        }
    }
    describe("a get request that redirects and disallowing redirects") {
        val response = get("http://httpbin.org/redirect-to?url=http://httpbin.org/get", allowRedirects = false)
        context("accessing the status code") {
            val code = response.statusCode
            it("should be 302") {
                assertEquals(302, code)
            }
        }
    }
    describe("a get request that redirects with HTTP 307 and disallowing redirects") {
        val response = get("http://httpbin.org/redirect-to?status_code=307&url=http://httpbin.org/get", allowRedirects = false)
        context("accessing the status code") {
            val code = response.statusCode
            it("should be 307") {
                assertEquals(307, code)
            }
        }
    }
    describe("a get request that redirects with HTTP 308 and disallowing redirects") {
        val response = get("http://httpbin.org/redirect-to?status_code=308&url=http://httpbin.org/get", allowRedirects = false)
        context("accessing the status code") {
            val code = response.statusCode
            it("should be 308") {
                assertEquals(308, code)
            }
        }
    }
    describe("a get request that redirects five times") {
        val response = get("http://httpbin.org/redirect/5")
        context("accessing the json") {
            val json = response.jsonObject
            it("should have the get url") {
                assertEquals("http://httpbin.org/get", json.getString("url"))
            }
        }
    }
    describe("a get request that takes ten seconds to complete") {
        context("request") {
            it("should throw a timeout exception") {
                assertFailsWith(SocketTimeoutException::class) {
                    get("http://httpbin.org/delay/10", timeout = 1.0)
                }
            }
        }
    }
    describe("a get request that sets cookies without redirects") {
        val cookieName = "test"
        val cookieValue = "quite"
        val response = get("http://httpbin.org/cookies/set?$cookieName=$cookieValue", allowRedirects = false)
        context("inspecting the cookies") {
            val cookies = response.cookies
            it("should set a cookie") {
                assertEquals(1, cookies.size)
            }
            val cookie = cookies.getCookie(cookieName)
            val text = cookies[cookieName]
            it("should have the specified cookie name") {
                assertNotNull(cookie)
            }
            it("should have the specified text") {
                assertNotNull(text)
            }
            it("should have the same value") {
                assertEquals(cookieValue, cookie!!.value)
            }
            it("should have the same text value") {
                // Attributes ignored
                assertEquals(cookieValue, text!!.toString().split(";")[0])
            }
        }
    }
    describe("a get request that sets cookies with redirects") {
        val cookieName = "test"
        val cookieValue = "quite"
        val response = get("http://httpbin.org/cookies/set?$cookieName=$cookieValue")
        context("inspecting the cookies") {
            val cookies = response.cookies
            it("should set a cookie") {
                assertEquals(1, cookies.size)
            }
            val cookie = cookies.getCookie(cookieName)
            val text = cookies[cookieName]
            it("should have the specified cookie name") {
                assertNotNull(cookie)
            }
            it("should have the specified text") {
                assertNotNull(text)
            }
            it("should have the same value") {
                assertEquals(cookieValue, cookie!!.value)
            }
            it("should have the same text value") {
                // Attributes ignored
                assertEquals(cookieValue, text!!.toString().split(";")[0])
            }
        }
    }
    describe("a get request that sets multiple cookies with redirects") {
        val cookieNameOne = "test"
        val cookieValueOne = "quite"
        val cookieNameTwo = "derp"
        val cookieValueTwo = "herp"
        val response = get("http://httpbin.org/cookies/set?$cookieNameOne=$cookieValueOne&$cookieNameTwo=$cookieValueTwo")
        context("inspecting the cookies") {
            val cookies = response.cookies
            it("should set two cookies") {
                assertEquals(2, cookies.size)
            }
            val cookie = cookies.getCookie(cookieNameOne)
            val text = cookies[cookieNameOne]
            it("should have the specified cookie name") {
                assertNotNull(cookie)
            }
            it("should have the specified text") {
                assertNotNull(text)
            }
            it("should have the same value") {
                assertEquals(cookieValueOne, cookie!!.value)
            }
            it("should have the same text value") {
                // Attributes ignored
                assertEquals(cookieValueOne, text!!.toString().split(";")[0])
            }
            val cookieTwo = cookies.getCookie(cookieNameTwo)
            val textTwo = cookies[cookieNameTwo]
            it("should have the specified cookie name") {
                assertNotNull(cookieTwo)
            }
            it("should have the specified text") {
                assertNotNull(textTwo)
            }
            it("should have the same value") {
                assertEquals(cookieValueTwo, cookieTwo!!.value)
            }
            it("should have the same text value") {
                // Attributes ignored
                assertEquals(cookieValueTwo, textTwo!!.toString().split(";")[0])
            }
        }
    }
    describe("a gzip get request") {
        val response = get("https://httpbin.org/gzip")
        context("accessing the stream") {
            val stream = response.raw
            it("should be a GZIPInputStream") {
                assertTrue(stream is GZIPInputStream)
            }
        }
        context("accessing the json") {
            val json = response.jsonObject
            it("should be gzipped") {
                assertTrue(json.getBoolean("gzipped"))
            }
        }
    }
    describe("a deflate get request") {
        val response = get("https://httpbin.org/deflate")
        context("accessing the stream") {
            val stream = response.raw
            it("should be a InflaterInputStream") {
                assertTrue(stream is InflaterInputStream)
            }
        }
        context("accessing the json") {
            val json = response.jsonObject
            it("should be deflated") {
                assertTrue(json.getBoolean("deflated"))
            }
        }
    }
    describe("a get request that returns 418") {
        val response = get("https://httpbin.org/status/418")
        context("accessing the status code") {
            val status = response.statusCode
            it("should be 418") {
                assertEquals(418, status)
            }
        }
        context("accessing the text") {
            val text = response.text
            it("should contain \"teapot\"") {
                assertTrue(text.contains("teapot"))
            }
        }
    }
    describe("a get request for a UTF-8 document") {
        val response = get("https://httpbin.org/encoding/utf8")
        context("checking the encoding") {
            val encoding = response.encoding
            it("should be UTF-8") {
                assertEquals(Charsets.UTF_8, encoding)
            }
        }
        context("reading the text") {
            val text = response.text
            it("should contain ∮") {
                assertTrue(text.contains("∮"))
            }
        }
        context("changing the encoding") {
            response.encoding = Charsets.ISO_8859_1
            val encoding = response.encoding
            it("should be ISO-8859-1") {
                assertEquals(Charsets.ISO_8859_1, encoding)
            }
        }
        context("reading the text") {
            val text = response.text
            it("should not contain ∮") {
                assertFalse(text.contains("∮"))
            }
        }
    }
    describe("an unsupported khttp schema") {
        context("construction") {
            it("should throw an IllegalArgumentException") {
                assertFailsWith(IllegalArgumentException::class) {
                    get("ftp://google.com")
                }
            }
        }
    }
    describe("an unsupported Java schema") {
        context("construction") {
            it("should throw a MalformedURLException") {
                assertFailsWith(MalformedURLException::class) {
                    get("gopher://google.com")
                }
            }
        }
    }
    describe("a request with a user agent set") {
        val userAgent = "khttp/test"
        val request = get("https://httpbin.org/user-agent", headers = mapOf("User-Agent" to userAgent))
        context("accessing the json") {
            val json = request.jsonObject
            val responseUserAgent = json.getString("user-agent")
            it("should have the same user agent") {
                assertEquals(userAgent, responseUserAgent)
            }
        }
    }
    describe("a request with a port") {
        val request = get("https://httpbin.org:443/get")
        context("accessing the json") {
            val json = request.jsonObject
            it("should not be null") {
                assertNotNull(json)
            }
        }
    }
    describe("a get request for a JSON array") {
        val request = get("http://jsonplaceholder.typicode.com/users")
        context("accessing the json") {
            val json = request.jsonArray
            it("should have ten items") {
                assertEquals(10, json.length())
            }
        }
    }
    describe("a non-streaming get request") {
        val response = get("https://httpbin.org/get")
        context("checking the bytes available to be read") {
            val available = response.raw.available()
            it("should be 0") {
                assertEquals(0, available)
            }
        }
    }
    describe("a streaming get request with a streaming line response") {
        val response = get("http://httpbin.org/stream/4", stream = true)
        context("iterating over the lines") {
            val iterator = response.lineIterator()
            var counter = 0
            for (line in iterator) {
                val json = JSONObject(line.toString(response.encoding))
                assertEquals(counter++, json.getInt("id"))
            }
            it("should have iterated 4 times") {
                assertEquals(4, counter)
            }
        }
    }
    describe("a streaming get request with a streaming byte response") {
        val response = get("http://httpbin.org/stream-bytes/4?seed=1", stream = true)
        context("iterating over the bytes") {
            val iterator = response.contentIterator(chunkSize = 1)
            var counter = 0
            val expected = byteArrayOf(0x22, 0xD8.toByte(), 0xC3.toByte(), 0x41)
            for (byte in iterator) {
                assertEquals(1, byte.size)
                assertEquals(expected[counter++], byte[0])
            }
            it("should have iterated 4 times") {
                assertEquals(4, counter)
            }
        }
    }
    describe("a streaming get request without even lines") {
        val url = "https://httpbin.org/bytes/1690?seed=1"
        val response = get(url, stream = true)
        context("iterating the lines") {
            val iterator = response.lineIterator()
            val bytes = iterator.asSequence().toList().flatMap { it.toList() }
            val contentWithoutBytes = get(url).content.toList().filter { it != '\r'.toByte() && it != '\n'.toByte() }
            it("should be the same as the content without line breaks") {
                assertEquals(contentWithoutBytes, bytes)
            }
        }
    }
    describe("a request with a space in the url") {
        val url = "https://httpbin.org/anything/some text"
        val response = get(url)
        context("check the url") {
            val responseUrl = response.jsonObject["url"]
            it("should be the same as the request url") {
                assertEquals(url, responseUrl)
            }
        }
    }
    describe("a request where the client needs to authenticate itself by certificates") {
        val url = "https://client.badssl.com/"
        val sslContext = SslContextUtils.createFromKeyMaterial("keystores/badssl.com-client.p12", "badssl.com".toCharArray())

        val response = get(url, sslContext = sslContext)
        context("check the url") {
            val statusCode = response.statusCode
            it("should be ok") {
                if (statusCode == 400) {
                    print("WARNING: Certificate may have expired and needs to be updated")
                } else {
                    assertEquals(200, statusCode)
                }
            }
        }
    }
})
