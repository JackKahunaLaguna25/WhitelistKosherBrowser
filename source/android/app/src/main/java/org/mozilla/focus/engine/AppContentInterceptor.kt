/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.engine

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import mozilla.components.browser.errorpages.ErrorPages
import mozilla.components.browser.errorpages.ErrorType
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.request.RequestInterceptor
import org.mozilla.focus.R
import org.mozilla.focus.activity.CrashListActivity
import org.mozilla.focus.browser.LocalizedContent
import org.mozilla.focus.ext.components
import org.mozilla.focus.whitelist.Whitelist
import org.mozilla.focus.whitelist.BlockedPageGenerator
import java.net.URI
import java.net.URISyntaxException

class AppContentInterceptor(
    private val context: Context,
) : RequestInterceptor {

    companion object {
        private const val TAG = "WhitelistInterceptor"

        // Track the current main page URL to determine if user navigated to a new site
        private var currentMainPageUrl: String? = null
    }

    override fun onLoadRequest(
        engineSession: EngineSession,
        uri: String,
        lastUri: String?,
        hasUserGesture: Boolean,
        isSameDomain: Boolean,
        isRedirect: Boolean,
        isDirectNavigation: Boolean,
        isSubframeRequest: Boolean,
    ): RequestInterceptor.InterceptionResponse? {

        Log.d(TAG, "=== REQUEST INTERCEPTED ===")
        Log.d(TAG, "URI: $uri")
        Log.d(TAG, "Last URI: $lastUri")
        Log.d(TAG, "Has user gesture: $hasUserGesture")
        Log.d(TAG, "Is same domain: $isSameDomain")
        Log.d(TAG, "Is redirect: $isRedirect")
        Log.d(TAG, "Is direct navigation: $isDirectNavigation")
        Log.d(TAG, "Is subframe request: $isSubframeRequest")
        Log.d(TAG, "Current main page URL: $currentMainPageUrl")

        // Handle internal Firefox Focus URLs first
        when (uri) {
            LocalizedContent.URL_ABOUT -> {
                Log.d(TAG, "Loading internal about page")
                return RequestInterceptor.InterceptionResponse.Content(
                    LocalizedContent.loadAbout(context),
                    encoding = "base64",
                )
            }

            LocalizedContent.URL_RIGHTS -> {
                Log.d(TAG, "Loading internal rights page")
                return RequestInterceptor.InterceptionResponse.Content(
                    LocalizedContent.loadRights(context),
                    encoding = "base64",
                )
            }

            LocalizedContent.URL_GPL -> {
                Log.d(TAG, "Loading internal GPL page")
                return RequestInterceptor.InterceptionResponse.Content(
                    LocalizedContent.loadGPL(context),
                    encoding = "base64",
                )
            }

            LocalizedContent.URL_LICENSES -> {
                Log.d(TAG, "Loading internal licenses page")
                return RequestInterceptor.InterceptionResponse.Content(
                    LocalizedContent.loadLicenses(context),
                    encoding = "base64",
                )
            }

            "about:crashes" -> {
                Log.d(TAG, "Loading crashes page")
                val intent = Intent(context, CrashListActivity::class.java)
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return RequestInterceptor.InterceptionResponse.Url("about:blank")
            }
        }

        // Skip whitelist checking for internal URLs, data URLs, and blank pages
        if (uri.startsWith("about:") ||
            uri.startsWith("data:") ||
            uri.startsWith("file:") ||
            uri == "about:blank") {
            Log.d(TAG, "Allowing internal/system URL: $uri")
            return context.components.appLinksInterceptor.onLoadRequest(
                engineSession, uri, lastUri, hasUserGesture, isSameDomain,
                isRedirect, isDirectNavigation, isSubframeRequest
            )
        }

        // Extract domain from URI for whitelist checking
        val requestDomain = extractDomain(uri)
        if (requestDomain == null) {
            Log.e(TAG, "Failed to extract domain from URI: $uri - BLOCKING")
            return RequestInterceptor.InterceptionResponse.Content(
                BlockedPageGenerator.createBlockedPageHtml(uri),
                encoding = "utf-8"
            )
        }

        Log.d(TAG, "Extracted domain: $requestDomain")

        // Determine if this is a main page navigation vs a resource request
        val isMainPageNavigation = isMainPageNavigation(
            uri, lastUri, hasUserGesture, isSameDomain, isRedirect,
            isDirectNavigation, isSubframeRequest
        )

        Log.d(TAG, "Is main page navigation: $isMainPageNavigation")

        if (isMainPageNavigation) {
            // This is a main page navigation - check whitelist strictly
            Log.d(TAG, "Checking main page navigation against whitelist")

            if (isUrlAllowed(uri)) {
                Log.d(TAG, "Main page navigation ALLOWED: $uri")
                // Update the current main page URL to track what site we're on
                currentMainPageUrl = uri
                Log.d(TAG, "Updated current main page URL to: $currentMainPageUrl")

                return context.components.appLinksInterceptor.onLoadRequest(
                    engineSession, uri, lastUri, hasUserGesture, isSameDomain,
                    isRedirect, isDirectNavigation, isSubframeRequest
                )
            } else {
                Log.d(TAG, "Main page navigation BLOCKED: $uri")
                return RequestInterceptor.InterceptionResponse.Content(
                    BlockedPageGenerator.createBlockedPageHtml(uri),
                    encoding = "utf-8"
                )
            }
        } else {
            // This is a resource request (iframe, image, script, etc.)
            Log.d(TAG, "Processing resource request")

            // If we're currently on an allowed main page, allow all resource requests
            if (currentMainPageUrl != null && isUrlAllowed(currentMainPageUrl!!)) {
                Log.d(TAG, "Resource request ALLOWED (current main page is allowed): $uri")
                return context.components.appLinksInterceptor.onLoadRequest(
                    engineSession, uri, lastUri, hasUserGesture, isSameDomain,
                    isRedirect, isDirectNavigation, isSubframeRequest
                )
            } else {
                Log.d(TAG, "Resource request BLOCKED (no allowed main page): $uri")
                return RequestInterceptor.InterceptionResponse.Content(
                    BlockedPageGenerator.createBlockedPageHtml(uri),
                    encoding = "utf-8"
                )
            }
        }
    }

    /**
     * Determines if this request represents a main page navigation vs a resource request
     */
    private fun isMainPageNavigation(
        uri: String,
        lastUri: String?,
        hasUserGesture: Boolean,
        isSameDomain: Boolean,
        isRedirect: Boolean,
        isDirectNavigation: Boolean,
        isSubframeRequest: Boolean
    ): Boolean {

        Log.d(TAG, "Analyzing navigation - lastUri: $lastUri")

        // Subframe requests (iframes) are never main page navigations
        if (isSubframeRequest) {
            Log.d(TAG, "Not main page navigation: is subframe request")
            return false
        }

        // Direct navigation (typing in address bar) is always main page navigation
        if (isDirectNavigation) {
            Log.d(TAG, "Is main page navigation: direct navigation")
            return true
        }

        // User gesture (clicking a link) that changes domain is main page navigation
        if (hasUserGesture && !isSameDomain) {
            Log.d(TAG, "Is main page navigation: user gesture + different domain")
            return true
        }

        // Redirects that change domain are main page navigation
        if (isRedirect && !isSameDomain) {
            Log.d(TAG, "Is main page navigation: redirect + different domain")
            return true
        }

        // If we don't have a current main page, treat as main page navigation
        if (currentMainPageUrl == null) {
            Log.d(TAG, "Is main page navigation: no current main page")
            return true
        }

        // Check if the domain significantly changed from current main page
        val currentDomain = extractDomain(currentMainPageUrl!!)
        val requestDomain = extractDomain(uri)

        if (currentDomain != null && requestDomain != null) {
            // If domains are different, this might be main page navigation
            if (!domainsMatch(currentDomain, requestDomain)) {
                Log.d(TAG, "Is main page navigation: domain changed from $currentDomain to $requestDomain")
                return true
            }

            // Special case: if current main page is from allowExactOnly list,
            // any URL change (even same domain) should be treated as main page navigation
            // because allowExactOnly should only allow the exact URL
            if (isCurrentMainPageFromExactOnly()) {
                Log.d(TAG, "Is main page navigation: current main page is from allowExactOnly, checking for URL changes")
                if (normalizeUrl(currentMainPageUrl!!) != normalizeUrl(uri)) {
                    Log.d(TAG, "Is main page navigation: URL changed and current page is allowExactOnly")
                    return true
                }
            }
        }

        Log.d(TAG, "Not main page navigation: appears to be resource request")
        return false
    }

    /**
     * Checks if two domains match (considering subdomain relationships)
     */
    private fun domainsMatch(domain1: String, domain2: String): Boolean {
        if (domain1 == domain2) return true

        // Check if one is a subdomain of the other
        return domain1.endsWith(".$domain2") || domain2.endsWith(".$domain1")
    }

    /**
     * Checks if a URL is allowed according to whitelist rules
     */
    private fun isUrlAllowed(url: String): Boolean {
        Log.d(TAG, "Checking if URL is allowed: $url")

        val domain = extractDomain(url)
        if (domain == null) {
            Log.e(TAG, "Cannot extract domain from URL: $url")
            return false
        }

        Log.d(TAG, "Checking domain: $domain")

        // Check allowSubdomains - allows domain and all subdomains
        for (allowedDomain in Whitelist.allowSubdomains) {
            Log.d(TAG, "Checking against allowSubdomains: $allowedDomain")
            if (domain == allowedDomain || domain.endsWith(".$allowedDomain")) {
                Log.d(TAG, "MATCH in allowSubdomains: $allowedDomain")
                return true
            }
        }

        // Check allowNoSubdomains - allows exact domain and paths but not subdomains
        for (allowedDomain in Whitelist.allowNoSubdomains) {
            Log.d(TAG, "Checking against allowNoSubdomains: $allowedDomain")
            if (domain == allowedDomain) {
                Log.d(TAG, "MATCH in allowNoSubdomains: $allowedDomain")
                return true
            }
        }

        // Check allowExactOnly - allows only exact URLs
        for (allowedUrl in Whitelist.allowExactOnly) {
            Log.d(TAG, "Checking against allowExactOnly: $allowedUrl")

            if (isExactUrlMatch(url, allowedUrl)) {
                Log.d(TAG, "MATCH in allowExactOnly: $allowedUrl")
                return true
            }
        }

        Log.d(TAG, "URL NOT ALLOWED: $url")
        return false
    }

    /**
     * Checks if the current main page URL matches any entry in allowExactOnly list
     */
    private fun isCurrentMainPageFromExactOnly(): Boolean {
        if (currentMainPageUrl == null) return false

        for (allowedUrl in Whitelist.allowExactOnly) {
            if (isExactUrlMatch(currentMainPageUrl!!, allowedUrl)) {
                Log.d(TAG, "Current main page matches allowExactOnly entry: $allowedUrl")
                return true
            }
        }
        return false
    }

    /**
     * Checks if a URL matches exactly with an allowed URL from whitelist
     * Handles different schemes (http/https) and www prefix variations
     */
    private fun isExactUrlMatch(requestUrl: String, allowedUrl: String): Boolean {
        try {
            val requestUri = URI(requestUrl)
            val requestHost = requestUri.host ?: return false
            val requestPath = requestUri.path ?: "/"

            // Normalize request path (remove trailing slash unless it's root)
            val normalizedRequestPath = if (requestPath == "/" || requestPath.isEmpty()) {
                "/"
            } else {
                requestPath.trimEnd('/')
            }

            // Try different variations of the allowed URL
            val allowedVariations = listOf(
                "http://$allowedUrl",
                "https://$allowedUrl",
                "http://www.$allowedUrl",
                "https://www.$allowedUrl"
            )

            for (allowedVariation in allowedVariations) {
                try {
                    val allowedUri = URI(allowedVariation)
                    val allowedHost = allowedUri.host ?: continue
                    val allowedPath = allowedUri.path ?: "/"

                    // Normalize allowed path
                    val normalizedAllowedPath = if (allowedPath == "/" || allowedPath.isEmpty()) {
                        "/"
                    } else {
                        allowedPath.trimEnd('/')
                    }

                    // Compare normalized hosts and paths
                    val hostMatch = requestHost.equals(allowedHost, ignoreCase = true)
                    val pathMatch = normalizedRequestPath == normalizedAllowedPath

                    Log.d(TAG, "Comparing: ${requestHost}${normalizedRequestPath} vs ${allowedHost}${normalizedAllowedPath} -> host: $hostMatch, path: $pathMatch")

                    if (hostMatch && pathMatch) {
                        Log.d(TAG, "Exact URL match found: $allowedVariation")
                        return true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse allowed URL variation: $allowedVariation", e)
                    continue
                }
            }

            return false

        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse request URL for exact matching: $requestUrl", e)
            return false
        }
    }

    /**
     * Extracts domain from URL, handling www. prefix normalization
     */
    private fun extractDomain(url: String): String? {
        try {
            val uri = URI(url)
            val host = uri.host

            if (host == null) {
                Log.e(TAG, "No host found in URL: $url")
                return null
            }

            // Remove www. prefix if present for consistent domain comparison
            val normalizedHost = if (host.startsWith("www.")) {
                host.substring(4)
            } else {
                host
            }

            Log.d(TAG, "Extracted and normalized domain: $host -> $normalizedHost")
            return normalizedHost

        } catch (e: URISyntaxException) {
            Log.e(TAG, "Failed to parse URL: $url", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error extracting domain from URL: $url", e)
            return null
        }
    }

    /**
     * Normalizes URL for exact comparison (removes trailing slashes, fragments, etc.)
     */
    private fun normalizeUrl(url: String): String {
        try {
            val uri = URI(url)

            // Build normalized URL with scheme, host, port (if non-default), and path
            val scheme = uri.scheme ?: "https"
            val host = uri.host ?: return url
            val port = if (uri.port != -1 &&
                !((scheme == "http" && uri.port == 80) ||
                        (scheme == "https" && uri.port == 443))) {
                ":${uri.port}"
            } else {
                ""
            }

            val path = uri.path?.let {
                if (it.isEmpty() || it == "/") "/" else it.trimEnd('/')
            } ?: "/"

            val normalized = "$scheme://$host$port$path"
            Log.d(TAG, "Normalized URL: $url -> $normalized")
            return normalized

        } catch (e: Exception) {
            Log.e(TAG, "Failed to normalize URL: $url", e)
            return url
        }
    }




    override fun onErrorRequest(
        session: EngineSession,
        errorType: ErrorType,
        uri: String?,
    ): RequestInterceptor.ErrorResponse {
        val errorPage = ErrorPages.createUrlEncodedErrorPage(
            context,
            errorType,
            uri,
            titleOverride = { type -> getErrorPageTitle(context, type) },
            descriptionOverride = { type -> getErrorPageDescription(context, type) },
        )
        return RequestInterceptor.ErrorResponse(errorPage)
    }

    override fun interceptsAppInitiatedRequests() = true
}

private fun getErrorPageTitle(context: Context, type: ErrorType): String? {
    if (type == ErrorType.ERROR_HTTPS_ONLY) {
        return context.getString(R.string.errorpage_httpsonly_title)
    }
    // Returning `null` here will let the component use its default title for this error type
    return null
}

private fun getErrorPageDescription(context: Context, type: ErrorType): String? {
    if (type == ErrorType.ERROR_HTTPS_ONLY) {
        return context.getString(
            R.string.errorpage_httpsonly_message,
            context.getString(R.string.app_name),
        )
    }
    // Returning `null` here will let the component use its default description for this error type
    return null
}
