package com.ebay.sdk

import android.net.Uri
import com.ebay.sdk.EbayTrading.Environment
import com.ebay.soap.eBLBaseComponents.EBayAPIInterface
import com.ebay.soap.eBLBaseComponents.GetItemRequestType
import com.ebay.soap.eBLBaseComponents.GetItemResponseType
import com.ebay.soap.eBLBaseComponents.ItemType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import jakarta.jws.WebParam
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import java.net.InetSocketAddress
import java.net.Proxy
import java.nio.charset.Charset
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @Test
    fun call() {

        val mockServer = ClientAndServer.startClientAndServer(1000)
        mockkStatic(Uri::class)

        // Copied from Gemini - REQUIRED to keep EbayTrading free of custom "testing-only" URLs.
        // Create an insecure TrustManager to make OkHttp trust MockServer's self-signed SSL
        val trustAllCerts = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, arrayOf<TrustManager>(trustAllCerts), SecureRandom())
        }
        // END Copied from Gemini

        try {
            val mockProdUri = mockk<Uri>()
            every { mockProdUri.scheme } returns "https"
            every { mockProdUri.host } returns "api.ebay.com"
            every { mockProdUri.path } returns "/ws/api.dll"
            every { mockProdUri.toString() } returns Environment.PRODUCTION.urlString
            every { Uri.parse(Environment.PRODUCTION.urlString) } returns mockProdUri

            // Copied from Gemini - REQUIRED for the proxy scaffolding to work
            val testHttpClient = OkHttpClient.Builder()
                // eBay Trading supports HTTP 1.1.  Force it here.
                .protocols(listOf(Protocol.HTTP_1_1))
                // Explicitly force this specific client instance to route through local MockServer
                .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("localhost", 1000)))
                // Trust MockServer's local SSL certificates
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts)
                .hostnameVerifier { _, _ -> true }
                .build()
            // END Copied from Gemini

            val url = Environment.PRODUCTION.url
            val apiVersion = "1379"
            val siteId = "2"
            val accessToken = "testAccessToken"

            val ebayTradingClient = EbayTrading(apiVersion = apiVersion, siteId = siteId, httpClient = testHttpClient)

            val itemID = "110043671232"
            val getItem = Pair(GetItemRequestType().apply {
                this.itemID = itemID
            }, GetItemResponseType().apply {
                item = ItemType().apply {
                    this.itemID = itemID
                }
            })

            val requestAndResponse = Pair(request()
                .withMethod("POST")
                // Gemini - CRITICAL: Match the actual URL structure MockServer parses out of the tunnel
                .withSecure(true)
                // END Gemini
                .withPath(url.path)
                // these are the critical headers specified by eBay
                .withHeaders(
                    Header("X-EBAY-API-CALL-NAME", "GetItem"),
                    Header("X-EBAY-API-SITEID", siteId),
                    Header("X-EBAY-API-COMPATIBILITY-LEVEL", apiVersion),
                    Header("X-EBAY-API-IAF-TOKEN", accessToken),
                    Header("Content-Type", "(?i).*text/xml.*"))
                , response()
                    .withStatusCode(200)
                    .withBody(EbayTrading.marshal(getItem.component2()))
            )
            mockServer.`when`(requestAndResponse.component1())
                .respond(requestAndResponse.component2())

            val response = ebayTradingClient.call(getItem.component1(), accessToken)
            assertEquals(getItem.component2(), response)
        } finally {
            unmockkStatic(Uri::class)
            mockServer.stop()
        }
    }
}