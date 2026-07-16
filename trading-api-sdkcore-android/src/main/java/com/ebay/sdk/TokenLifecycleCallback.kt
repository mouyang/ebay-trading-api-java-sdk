package com.ebay.sdk

import java.time.Instant

/**
 * Notified when eBay reports token lifecycle events in API responses.
 * Mirrors [com.ebay.sdk.TokenEventListener.warnHardExpiration] from the desktop SDK.
 */
fun interface TokenLifecycleCallback {
    fun onHardExpirationWarning(expirationDate: Instant)
}
