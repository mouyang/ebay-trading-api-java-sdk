package com.ebay.sdk

import android.net.Uri
import androidx.core.net.toUri
import com.ebay.soap.eBLBaseComponents.AbstractRequestType
import com.ebay.soap.eBLBaseComponents.AbstractResponseType
import com.ebay.soap.eBLBaseComponents.EBayAPIInterface
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyName
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector
import jakarta.jws.WebMethod
import jakarta.jws.WebParam
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlSchema
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Anything hardcoded follows the conventions in the following documentation link.
 * https://developer.ebay.com/devzone/xml/docs/Concepts/MakingACall.html
 */
class EbayTrading(
    val environment: Environment = Environment.PRODUCTION,
    val apiVersion: String,
    val siteId: String,
    val httpClient: OkHttpClient = OkHttpClient(),
    /**
     * This is required in order to use the call methods without the accessToken as an argument.
     */
    val accessTokenSupplier: (() -> String)? = null) {

    enum class Environment(val url: Uri) {
        SANDBOX("https://api.sandbox.ebay.com/ws/api.dll".toUri()),
        PRODUCTION("https://api.ebay.com/ws/api.dll".toUri())
    }

    val xmlMapper: XmlMapper = XmlMapper(); init {
        xmlMapper.setAnnotationIntrospector(AddDefaultNamespaceIntrospector(xmlMapper.typeFactory))
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        // eBay seems to return a lot of undocumented elements that are not part of the WSDL.
        // This will prevent parsing errors due to such properties
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // reduces request payloads by not including properties not being set explicitly
        xmlMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
    }

    /* This makes sense because WSDL operations are uniquely referenced by name.  Relevant
       information on operations, like element root name and response class, should never be
       hardcoded.  Rather it should be pulled from generated annotations (which are derived from
       WSDL and therefore is authoritative).
     */
    val webMethodsByOperationName : Map<String, Method> = buildMap {
        EBayAPIInterface::class.java.methods.forEach action@{ method ->
            val webMethod = method.getAnnotation(WebMethod::class.java)
            if (null == webMethod || webMethod.exclude) return@action
            put(webMethod.operationName, method)
        }
    }

    // Propagate the root namespace if it is not provided in the child elements
    // source: https://github.com/FasterXML/jackson-dataformat-xml/issues/18#issuecomment-1308125741
    private class AddDefaultNamespaceIntrospector(typeFactory: TypeFactory)
        : JakartaXmlBindAnnotationIntrospector(typeFactory) {
        private fun getPackageNamespace(c: Class<*>): String? {
            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            return c.`package`.getAnnotation(XmlSchema::class.java).namespace
        }

        override fun findNamespace(config: MapperConfig<*>, ann: Annotated): String? {
            val ae = ann.annotated
            val xmlElement = ae.getAnnotation(XmlElement::class.java)
            if (xmlElement?.namespace == "##default") {
                if (ae is Field) {
                    return getPackageNamespace(ae.declaringClass)
                } else if (ae is Method) {
                    return getPackageNamespace(ae.declaringClass)
                }
            }
            return super.findNamespace(config, ann)
        }
    }


    /**
     * By the convention used in the WSDL schema, the desired operation is the simple class name
     * with the string "RequestType" is stripped from the end.
     */
    private fun assumedCallName(request: AbstractRequestType) : String {
        return request::class.java.simpleName.replace("RequestType", "")
    }

    /**
     * must have accessTokenSupplier in order to use this method
     * will use implicit call name based on request class
     */
    fun call(request: AbstractRequestType) : AbstractResponseType {
        return call(assumedCallName(request), request, accessTokenSupplier!!)
    }

    /**
     * will use implicit call name based on request class
     */
    fun call(request: AbstractRequestType, accessToken: String) : AbstractResponseType {
        try {
            return call(assumedCallName(request), request, accessToken)
        } catch (_ : IllegalArgumentException) {
            throw IllegalArgumentException("assumed operation for ${request::class.java.name} not found")
        }
    }

    /**
     * must have accessTokenSupplier in order to use this method
     */
    fun call(callName: String, request: AbstractRequestType) : AbstractResponseType {
        return call(callName, request, accessTokenSupplier!!)
    }

    fun call(callName: String, request: AbstractRequestType, accessToken: String) : AbstractResponseType {
        return call(callName, request, { accessToken })
    }

    /**
     * TODO Generalize so the interface is valid for non-Request-Response operations.  It just so
     * happens the original developer only needs Request-Response operations, but WSDL-specific can
     * eventually be refactored out of this component and other operation types (One-Way,
     * Solicit-Response, Notification) would need to be supported.
     */
    fun call(callName: String, request: AbstractRequestType, accessTokenSupplier: () -> String) : AbstractResponseType {
        suspend fun inner() : AbstractResponseType {
            val wsMethod = webMethodsByOperationName[callName]
            if (null == wsMethod) {
                throw IllegalArgumentException("operation not found for callName $callName")
            }
            val wsParam: WebParam = wsMethod.parameters[0].getAnnotation(WebParam::class.java)!!
            val httpRequest = Request.Builder()
                .url(environment.url.toString())
                .post(xmlMapper.writer()
                    .withRootName(PropertyName(wsParam.name, wsParam.targetNamespace))
                    .writeValueAsBytes(request)
                    .toRequestBody("text/xml; charset=UTF-8".toMediaType()))
                .header("X-EBAY-API-SITEID", siteId)
                .header("X-EBAY-API-COMPATIBILITY-LEVEL", apiVersion)
                .header("X-EBAY-API-IAF-TOKEN", accessTokenSupplier())
                .header("X-EBAY-API-CALL-NAME", callName)
                .build()
            // generated by Copilot :)
            suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->
                enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        if (cont.isCancelled) return
                        cont.resumeWith(Result.failure(e))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        cont.resumeWith(Result.success(response))
                    }
                })

                cont.invokeOnCancellation {
                    try {
                        cancel()
                    } catch (_: Throwable) {
                    }
                }
            }
            // END generated by Copilot :)
            val call = httpClient.newCall(httpRequest)
            val response = call.await()
            return xmlMapper.reader()
                .readValue(response.body.string(), wsMethod.returnType) as AbstractResponseType
        }

        return runBlocking {
            val call: Deferred<AbstractResponseType> = async {
                return@async inner()
            }
            return@runBlocking call.await()
        }
    }
}