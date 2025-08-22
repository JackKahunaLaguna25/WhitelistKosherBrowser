package org.mozilla.focus.whitelist

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object BlockedPageGenerator {

    fun createBlockedPageHtml(blockedUri: String): String {
        val allowSubdomainsHtml = if (Whitelist.allowSubdomains.isNotEmpty()) {
            Whitelist.allowSubdomains.joinToString("") {
                "<li><strong>$it</strong> (and all subdomains)</li>"
            }
        } else {
            "<li>None configured</li>"
        }

        val allowNoSubdomainsHtml = if (Whitelist.allowNoSubdomains.isNotEmpty()) {
            Whitelist.allowNoSubdomains.joinToString("") {
                "<li><strong>$it</strong> (exact domain only)</li>"
            }
        } else {
            "<li>None configured</li>"
        }

        val allowExactOnlyHtml = if (Whitelist.allowExactOnly.isNotEmpty()) {
            Whitelist.allowExactOnly.joinToString("") {
                "<li><strong>$it</strong> (exact URL only)</li>"
            }
        } else {
            "<li>None configured</li>"
        }

        return """
    <html>
    <head>
        <title>Blocked Site</title>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
    </head>
    <body>
        <h1>Website Blocked</h1>
        <p>Blocked URL: <strong>$blockedUri</strong></p>
        
        <h3>This Website is not in your whitelist! To change whitelist, open browser settings and load a whitelist. To generate a whitelist and for more details visit "github.com/JackKahunaLaguna25/WhitelistKosherBrowser"</h3>
        
        
        <h3>Sites with Subdomains Allowed</h3>
        <ul>$allowSubdomainsHtml</ul>
        
        <h3>Exact Domains Only</h3>
        <ul>$allowNoSubdomainsHtml</ul>
        
        <h3>Exact URLs Only</h3>
        <ul>$allowExactOnlyHtml</ul>
        
        <p><strong>How it works:</strong></p>
        <p>Subdomains allowed: You can visit the main site and all subdomains</p>
        <p>Exact domain: Only the specific domain is allowed</p>
        <p>Exact URL: Only the specific page/path is allowed</p>
    </body>
    </html>
    """.trimIndent()
    }
}