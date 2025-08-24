package org.mozilla.focus.whitelist

import org.json.JSONObject

object Whitelist {

    // Start with empty lists
    var allowSubdomains: List<String> = emptyList()
    var allowNoSubdomains: List<String> = emptyList()
    var allowExactOnly: List<String> = emptyList()

    /**
     * Load whitelist from decrypted JSON string.
     */
    fun loadFromJson(json: String) {
        val obj = JSONObject(json)

            allowSubdomains = obj.optJSONArray("allowSubdomains")?.let { arr ->
            List(arr.length()) { arr.getString(it).trim() }.filter { it.isNotEmpty() }
        } ?: emptyList()

        allowNoSubdomains = obj.optJSONArray("allowNoSubdomains")?.let { arr ->
            List(arr.length()) { arr.getString(it).trim() }.filter { it.isNotEmpty() }
        } ?: emptyList()

        allowExactOnly = obj.optJSONArray("allowExactOnly")?.let { arr ->
            List(arr.length()) { arr.getString(it).trim() }.filter { it.isNotEmpty() }
        } ?: emptyList()
    }
}
