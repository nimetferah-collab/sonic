package com.nimet.sonicwallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/**
 * Uygulama açılınca kullanıcıya sistemin canlı duvar kağıdı seçim ekranını
 * doğrudan Hızlı Kirpi üzerinde açan tek ekranlı, basit bir başlangıç ekranı.
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildLayout())
    }

    private fun buildLayout(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0D47A1"))
            setPadding(64, 64, 64, 64)
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            setTextColor(Color.WHITE)
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = getString(R.string.intro_text)
            setTextColor(Color.parseColor("#E3F2FD"))
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 64)
        }

        val button = Button(this).apply {
            text = getString(R.string.set_wallpaper_button)
            setTextColor(Color.parseColor("#0D47A1"))
            setBackgroundColor(Color.parseColor("#FFC107"))
            setPadding(48, 32, 48, 32)
            setOnClickListener { openLiveWallpaperPicker() }
        }

        root.addView(title, matchParamsWrap())
        root.addView(subtitle, matchParamsWrap())
        root.addView(button, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        return root
    }

    private fun matchParamsWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun openLiveWallpaperPicker() {
        val component = ComponentName(this, SonicWallpaperService::class.java)
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Bazı cihazlarda doğrudan seçim ekranı yoktur; genel duvar kağıdı seçiciye düş.
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            } catch (e2: Exception) {
                Toast.makeText(
                    this,
                    "Ayarlar > Duvar Kağıdı > Canlı Duvar Kağıtları yolunu kullanabilirsin.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
