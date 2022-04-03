/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package khttp.structures.cookie

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CookieJarSpec : Spek({
    describe("a CookieJar constructed with Cookies") {
        val cookie1 = Cookie("test1", "value1")
        val cookie2 = Cookie("test2", "value2", mapOf("attr1" to "attrv1"))
        val cookies = listOf(cookie1, cookie2)
        val cookieJar = CookieJar(*cookies.toTypedArray())
        context("inspecting the cookie jar") {
            val size = cookieJar.size
            it("should have two cookies") {
                assertEquals(2, size)
            }
        }
        context("accessing a cookie by name") {
            val cookie = cookieJar.getCookie("test1")
            it("should not be null") {
                assertNotNull(cookie)
            }
            it("should have the same name") {
                assertEquals("test1", cookie!!.key)
            }
            it("should have the same value") {
                assertEquals("value1", cookie!!.value)
            }
            it("should have the no attributes") {
                assertEquals(0, cookie!!.attributes.size)
            }
        }
        context("checking if a cookie exists by name") {
            it("should exist") {
                assertTrue("test1" in cookieJar)
            }
            it("should not exist") {
                assertFalse("test3" in cookieJar)
            }
            it("should not exist") {
                assertFalse(cookieJar.containsKey(null as String?))
            }
        }
        context("checking if a cookie exists by value") {
            it("should exist") {
                assertTrue(cookieJar.containsValue(cookie1.valueWithAttributes))
            }
            it("should not exist") {
                assertFalse(cookieJar.containsValue(""))
            }
            it("should not exist") {
                assertFalse(cookieJar.containsValue(null as String?))
            }
        }
        context("accessing another cookie by name") {
            val cookie = cookieJar.getCookie("test2")
            it("should not be null") {
                assertNotNull(cookie)
            }
            it("should have the same name") {
                assertEquals("test2", cookie!!.key)
            }
            it("should have the same value") {
                assertEquals("value2", cookie!!.value)
            }
            it("should have the same attributes") {
                assertEquals(mapOf("attr1" to "attrv1"), cookie!!.attributes)
            }
        }
        context("accessing a cookie that doesn't exist") {
            val cookie = cookieJar.getCookie("test3")
            val cookieRaw: Any? = cookieJar.get(null as String?)
            it("should be null") {
                assertNull(cookie)
            }
            it("should be null") {
                assertNull(cookieRaw)
            }
        }
        context("accessing a cookie with Map methods") {
            val cookieValue = cookieJar["test1"]
            it("should exist") {
                assertNotNull(cookieValue)
            }
            it("should have the value of the first cookie") {
                assertEquals(cookie1.valueWithAttributes, cookieValue)
            }
        }
        context("accessing a cookie that doesn't exist with Map methods") {
            val cookieValue = cookieJar["test3"]
            it("should not exist") {
                assertNull(cookieValue)
            }
        }
        context("adding a cookie to the cookie jar") {
            val cookie = Cookie("delicious", "cookie", mapOf("edible" to "damn straight"))
            cookieJar.setCookie(cookie)
            val size = cookieJar.size
            val added = cookieJar.getCookie("delicious")
            it("should have three cookies") {
                assertEquals(3, size)
            }
            it("should have the same cookie as was added") {
                assertEquals(added, cookie)
            }
        }
    }
    describe("a CookieJar constructed with a map") {
        val cookies = mapOf("test1" to "value1", "test2" to "value2; attr1=attrv1")
        val cookieJar = CookieJar(cookies)
        context("inspecting the cookie jar") {
            val size = cookieJar.size
            it("should have two cookies") {
                assertEquals(2, size)
            }
        }
        context("accessing a cookie by name") {
            val cookie = cookieJar.getCookie("test1")
            it("should not be null") {
                assertNotNull(cookie)
            }
            it("should have the same name") {
                assertEquals("test1", cookie!!.key)
            }
            it("should have the same value") {
                assertEquals("value1", cookie!!.value)
            }
            it("should have the no attributes") {
                assertEquals(0, cookie!!.attributes.size)
            }
        }
        context("accessing another cookie by name") {
            val cookie = cookieJar.getCookie("test2")
            it("should not be null") {
                assertNotNull(cookie)
            }
            it("should have the same name") {
                assertEquals("test2", cookie!!.key)
            }
            it("should have the same value") {
                assertEquals("value2", cookie!!.value)
            }
            it("should have the same attributes") {
                assertEquals(mapOf("attr1" to "attrv1"), cookie!!.attributes)
            }
        }
        context("accessing a cookie that doesn't exist") {
            val cookie = cookieJar.getCookie("test3")
            it("should be null") {
                assertNull(cookie)
            }
        }
        context("adding a cookie to the cookie jar") {
            val cookie = Cookie("delicious", "cookie", mapOf("edible" to "damn straight"))
            cookieJar.setCookie(cookie)
            val size = cookieJar.size
            val added = cookieJar.getCookie("delicious")
            it("should have three cookies") {
                assertEquals(3, size)
            }
            it("should have the same cookie as was added") {
                assertEquals(added, cookie)
            }
        }
        context("adding a cookie to the cookie jar with Map methods") {
            val cookie = Cookie("tasty", "cookie", mapOf("edible" to "damn straight"))
            cookieJar[cookie.key] = cookie.valueWithAttributes
            val size = cookieJar.size
            val added = cookieJar.getCookie("tasty")
            it("should have four cookies") {
                assertEquals(4, size)
            }
            it("should have the same cookie as was added") {
                assertEquals(added, cookie)
            }
        }
        context("removing a cookie with Map methods") {
            val originalSize = cookieJar.size
            cookieJar.remove("tasty")
            val cookieValue = cookieJar["tasty"]
            val size = cookieJar.size
            it("should have one less cookie") {
                assertEquals(originalSize - 1, size)
            }
            it("should not be accessible by name") {
                assertNull(cookieValue)
            }
        }
        context("removing an object that is not a string") {
            val originalSize = cookieJar.size
            val removed: Any? = (cookieJar as MutableMap<*, *>).remove(null)
            val size = cookieJar.size
            it("should be the same size") {
                assertTrue(originalSize == size)
            }
            it("should not have removed anything") {
                assertNull(removed)
            }
        }
    }
})
