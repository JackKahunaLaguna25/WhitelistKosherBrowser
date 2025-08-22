/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import mozilla.components.browser.state.state.SessionState
import org.mozilla.focus.GleanMetrics.SettingsScreen
import org.mozilla.focus.R
import org.mozilla.focus.ext.requireComponents
import org.mozilla.focus.ext.showToolbar
import org.mozilla.focus.state.AppAction
import org.mozilla.focus.state.Screen
import org.mozilla.focus.telemetry.TelemetryWrapper
import org.mozilla.focus.utils.AppConstants
import org.mozilla.focus.utils.SupportUtils
import org.mozilla.focus.whatsnew.WhatsNew
import android.widget.Toast
import android.app.AlertDialog
import android.graphics.Typeface
import android.net.Uri
import android.util.Base64
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import org.json.JSONObject
import org.mozilla.focus.whitelist.Whitelist
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec



class SettingsFragment : BaseSettingsFragment() {
    // Opens a document picker to select the whitelist.enc file.
    private val openWhitelistFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null) {
                Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            try {
                val content = requireContext().contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
                    ?.trim()

                if (content.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "File is empty", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                val decrypted = decryptWhitelistBase64(content)
                val pretty = try {
                    JSONObject(decrypted).toString(2) // nice formatting
                } catch (_: Exception) {
                    // If it wasn't valid JSON for some reason, show raw text
                    decrypted
                }

                // ✅ Load into Whitelist
                Whitelist.loadFromJson(decrypted)

                // ✅ Confirmation toast
                Toast.makeText(requireContext(), "Whitelist loaded successfully", Toast.LENGTH_SHORT).show()

                // ✅ Optional: still show the decrypted data
                showWhitelistPreview(pretty)

            } catch (t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Failed to load. Please give a valid whitelist.enc file",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    /**
     * Decrypts data produced by your PC tool:
     * base64( IV(16 bytes) || AES-128-CBC(ciphertext) ), PKCS#7/PKCS5 padding.
     * Key must match the generator exactly: "1234567890abcdef"
     */
    private fun decryptWhitelistBase64(base64: String): String {
        val all = Base64.decode(base64, Base64.DEFAULT)
        require(all.size > 16) { "Invalid encrypted payload" }

        val iv = all.copyOfRange(0, 16)
        val cipherBytes = all.copyOfRange(16, all.size)

        val keyBytes = "1234567890abcdef".toByteArray(Charsets.UTF_8) // 16 bytes
        val key = SecretKeySpec(keyBytes, "AES")
        val ivSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val plain = cipher.doFinal(cipherBytes)
        return String(plain, Charsets.UTF_8)
    }

    private fun showWhitelistPreview(content: String) {
        val formattedText = try {
            val obj = JSONObject(content)
            buildString {
                obj.optJSONArray("allowSubdomains")?.let { arr ->
                    append("✅ allowSubdomains (מאפשר כל תתי דומיינים ונתיבים):\n")
                    for (i in 0 until arr.length()) append("   - ${arr.getString(i)}\n")
                    append("\n")
                }

                obj.optJSONArray("allowNoSubdomains")?.let { arr ->
                    append("⚠️ allowNoSubdomains (מאפשר רק הדומיין המדויק וכל הנתיבים שלו):\n")
                    for (i in 0 until arr.length()) append("   - ${arr.getString(i)}\n")
                    append("\n")
                }

                obj.optJSONArray("allowExactOnly")?.let { arr ->
                    append("🔒 allowExactOnly (מאפשר רק הדומיין המדויק והנתיב המדויק):\n")
                    for (i in 0 until arr.length()) append("   - ${arr.getString(i)}\n")
                    append("\n")
                }
            }
        } catch (e: Exception) {
            // במקרה של JSON לא חוקי, fallback לתצוגה גולמית
            content
        }

        val tv = TextView(requireContext()).apply {
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
            setPadding(48, 32, 48, 32)
            textSize = 14f
            text = formattedText
        }

        val scroll = ScrollView(requireContext()).apply { addView(tv) }

        AlertDialog.Builder(requireContext())
            .setTitle("Decrypted Whitelist")
            .setView(scroll)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }




    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.settings)
    }

    override fun onResume() {
        super.onResume()

        showToolbar(getString(R.string.menu_settings))
    }

    override fun onPreferenceTreeClick(preference: androidx.preference.Preference): Boolean {
        val resources = resources

        return when (preference.key) {
            resources.getString(R.string.pref_key_general_screen) -> {
                requireComponents.appStore.dispatch(AppAction.OpenSettings(Screen.Settings.Page.General))
                true
            }
            resources.getString(R.string.pref_key_privacy_security_screen) -> {
                requireComponents.appStore.dispatch(AppAction.OpenSettings(Screen.Settings.Page.Privacy))
                true
            }
            resources.getString(R.string.pref_key_search_screen) -> {
                requireComponents.appStore.dispatch(AppAction.OpenSettings(Screen.Settings.Page.Search))
                true
            }
            resources.getString(R.string.pref_key_advanced_screen) -> {
                requireComponents.appStore.dispatch(AppAction.OpenSettings(Screen.Settings.Page.Advanced))
                true
            }
            resources.getString(R.string.pref_key_mozilla_screen) -> {
                requireComponents.appStore.dispatch(AppAction.OpenSettings(Screen.Settings.Page.Mozilla))
                true
            }
// ...
            "pref_key_load_whitelist" -> {
                // Let user pick the whitelist.enc file (it's a text file containing base64)
                openWhitelistFile.launch(arrayOf("*/*")) // you can also try arrayOf("text/*")
                true
            }
// ...

            else -> super.onPreferenceTreeClick(preference)
        }
    }


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_settings_main, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.menu_whats_new) {
            whatsNewClicked()
            return true
        }
        return false
    }

    private fun whatsNewClicked() {
        val context = requireContext()

        SettingsScreen.whatsNewTapped.add()

        TelemetryWrapper.openWhatsNewEvent(WhatsNew.shouldHighlightWhatsNew(context))

        WhatsNew.userViewedWhatsNew(context)

        val sumoTopic = if (AppConstants.isKlarBuild) {
            SupportUtils.SumoTopic.WHATS_NEW_KLAR
        } else {
            SupportUtils.SumoTopic.WHATS_NEW_FOCUS
        }

        val url = SupportUtils.getSumoURLForTopic(context, sumoTopic)
        requireComponents.tabsUseCases.addTab(
            url,
            source = SessionState.Source.Internal.Menu,
            private = true,
        )
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
