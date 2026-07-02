package com.ebay.sdk

import com.ebay.soap.eBLBaseComponents.EBayAPIInterface
import com.ebay.soap.eBLBaseComponents.GetItemRequestType
import com.ebay.soap.eBLBaseComponents.ItemType
import jakarta.jws.WebParam
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
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
    fun unmarshal_ignoreUnknownProperties() {
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
    fun unmarshal_rejectsNonEbayClass() {
        assertThrows(IllegalArgumentException::class.java) {
            EbayTrading.unmarshal("<root/>".toByteArray(), String::class.java)
        }
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

    @Test
    fun marshal_rootName_roundTrip() {
        val getItem = EBayAPIInterface::class.java.getMethod("getItem", GetItemRequestType::class.java)
        val wsParam = getItem.parameters[0].getAnnotation(WebParam::class.java)!!
        val itemID = "12345"
        val request = GetItemRequestType().apply { this.itemID = itemID }
        val xml = EbayTrading.marshal(request, wsParam.name, wsParam.targetNamespace)
        assertTrue(String(xml, Charset.forName("UTF-8")).contains(wsParam.name))
        assertEquals(itemID, EbayTrading.unmarshal(xml, GetItemRequestType::class.java).itemID)
    }
}