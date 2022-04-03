/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package khttp.structures.maps

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CaseInsensitiveMapSpec : Spek({
    describe("a case-insensitive map initialized with a backing map") {
        val backingMap = hashMapOf("a" to "b", "c" to "d")
        val caseInsensitiveMap = CaseInsensitiveMap(backingMap)
        context("accessing a mapping") {
            val backed = backingMap["a"]
            val lower = caseInsensitiveMap["a"]
            val upper = caseInsensitiveMap["A"]
            it("should be the same value as in the backing map") {
                assertEquals(backed, lower)
            }
            it("should be the same value as in the backing map") {
                assertEquals(backed, upper)
            }
            it("should have an equal upper and lower value") {
                assertTrue(upper == lower)
            }
        }
        context("checking if a mapping is present") {
            val backed = backingMap.containsKey("c")
            val lower = caseInsensitiveMap.containsKey("c")
            val upper = caseInsensitiveMap.containsKey("C")
            it("should be the same value as the result for the backing map") {
                assertEquals(backed, lower)
            }
            it("should be the same value as the result for the backing map") {
                assertEquals(backed, upper)
            }
            it("should have an equal upper and lower value") {
                assertTrue(upper == lower)
            }
        }
        context("toString") {
            val backing = backingMap.toString()
            val insensitive = caseInsensitiveMap.toString()
            it("should be the same string returned by the backing map") {
                assertEquals(backing, insensitive)
            }
        }
        context("checking for invalid mappings") {
            it("should be null") {
                assertFalse(caseInsensitiveMap.containsKey(null as String?))
            }
            it("should be null") {
                assertFalse(caseInsensitiveMap.containsKey(object {} as Any))
            }
            it("should be null") {
                assertNull(caseInsensitiveMap.get(null as String?))
            }
            it("should be null") {
                assertNull(caseInsensitiveMap.get(object {} as Any))
            }
            it("should be false") {
                assertFalse(caseInsensitiveMap.containsKey("b"))
            }
            it("should be false") {
                assertFalse(caseInsensitiveMap.containsKey("B"))
            }
        }
    }
    describe("a case-insensitive map initialized with an empty backing map") {
        val backingMap = hashMapOf<String, String>()
        val caseInsensitiveMap = CaseInsensitiveMap(backingMap)
        context("checking for invalid mappings") {
            it("should be null") {
                assertFalse(caseInsensitiveMap.containsKey(null as String?))
            }
            it("should be null") {
                assertFalse(caseInsensitiveMap.containsKey(object {} as Any))
            }
        }
    }
})
