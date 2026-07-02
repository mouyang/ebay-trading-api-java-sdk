package com.ebay.sdk

import com.ebay.soap.eBLBaseComponents.ItemType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class EbayTradingTest {
    @Test
    fun unmarshal() {
        val itemID = "12345"
        val xml = """
            <Item>
                <ItemID>${itemID}</ItemID>
            </Item>
        """.trimIndent().toByteArray(Charset.forName("UTF-8"))
        val item = EbayTrading.unmarshal(xml, ItemType::class.java)
        assertEquals(itemID, item.itemID)
    }

    /**
     * Sometimes eBay provides elements that are no longer valid for a particular API version.
     * Unmarshalling should not crash because of that.
     */
    @Test
    fun unmarshal_unknownProperties() {
        val itemID = "12345"
        val xml = """
            <Item>
                <ItemID>${itemID}</ItemID>
                <UnknownItemProperty>unknownItemProperyValue</UnknownItemProperty>
            </Item>
        """.trimIndent().toByteArray(Charset.forName("UTF-8"))
        val item = EbayTrading.unmarshal(xml, ItemType::class.java)
        assertEquals(itemID, item.itemID)
    }

    @Test
    fun marshal_roundTrip() {
        val itemID = "12345"
        val item = ItemType().apply { this.itemID = itemID }
        val roundTripped = EbayTrading.unmarshal(EbayTrading.marshal(item), ItemType::class.java)
        assertEquals(itemID, roundTripped.itemID)
    }

    @Test
    fun marshal_rejectsNonEbayClass() {
        assertThrows(IllegalArgumentException::class.java) {
            EbayTrading.marshal("not an eBay class")
        }
    }
}