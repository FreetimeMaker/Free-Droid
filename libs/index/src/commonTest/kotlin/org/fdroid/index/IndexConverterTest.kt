package org.fdroid.index

import com.goncalossilva.resources.Resource
import com.freetime.freedroid.index.IndexParser.parseV1
import com.freetime.freedroid.index.v2.IndexV2
import com.freetime.freedroid.test.TestDataEmptyV2
import com.freetime.freedroid.test.TestDataMaxV2
import com.freetime.freedroid.test.TestDataMidV2
import com.freetime.freedroid.test.TestDataMinV2
import com.freetime.freedroid.test.TestUtils.sorted
import com.freetime.freedroid.test.v1compat
import kotlin.test.Test
import kotlin.test.assertEquals

internal const val ASSET_PATH = "../sharedTest/src/main/assets"

internal class IndexConverterTest {

    @Test
    fun testEmpty() {
        testConversation("$ASSET_PATH/index-empty-v1.json", TestDataEmptyV2.index.v1compat())
    }

    @Test
    fun testMin() {
        testConversation("$ASSET_PATH/index-min-v1.json", TestDataMinV2.index.v1compat())
    }

    @Test
    fun testMid() {
        testConversation("$ASSET_PATH/index-mid-v1.json", TestDataMidV2.indexCompat)
    }

    @Test
    fun testMax() {
        testConversation("$ASSET_PATH/index-max-v1.json", TestDataMaxV2.indexCompat)
    }

    private fun testConversation(file: String, expectedIndex: IndexV2) {
        val indexV1Res = Resource(file)
        val indexV1Str = indexV1Res.readText()
        val indexV1 = parseV1(indexV1Str)

        val v2 = IndexConverter().toIndexV2(indexV1)

        assertEquals(expectedIndex.sorted(), v2.sorted())
    }

}
