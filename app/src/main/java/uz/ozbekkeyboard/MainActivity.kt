package uz.ozbekkeyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val ink = Color.rgb(30, 36, 46)
    private val muted = Color.rgb(105, 112, 122)
    private val accent = Color.rgb(35, 112, 220)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }
    private fun showHome() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(Color.rgb(246, 248, 252))
        }
        page.addView(TextView(this).apply {
            text = "O‘ZBEK KEYBOARD"; textSize = 27f
            setTextColor(ink); typeface = Typeface.DEFAULT_BOLD
        })
        page.addView(TextView(this).apply {
            text = "O‘zbek lotin alifbosi uchun zamonaviy klaviatura"
            textSize = 15f; setTextColor(muted); setPadding(0, 8, 0, 20)
        })
        page.addView(TextView(this).apply {
            text = "Oʻ   Gʻ   Sh   Ch   Ng\n\nQulay yozing. Chiroyli ko‘rinish. Maxfiylik."
            textSize = 22f; gravity = Gravity.CENTER; setTextColor(ink)
            setPadding(16, 28, 16, 28); background = rounded(Color.WHITE, 22f)
        }, LinearLayout.LayoutParams(-1, -2))
        page.addView(TextView(this).apply {
            text = "MAVZUNI TANLANG"; textSize = 12f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(muted); setPadding(2, 24, 0, 8)
        })
        val prefs = getSharedPreferences("keyboard", Context.MODE_PRIVATE)
        val themes = listOf("☀️  Classic", "🌙  Dark", "💜  Neon", "🌿  Mint")
        themes.forEachIndexed { index, name ->
            page.addView(actionButton(name + if (prefs.getInt("theme", 0) == index) "   ✓" else "", Color.WHITE, ink) {
                prefs.edit().putInt("theme", index).apply()
                showHome()
            }, LinearLayout.LayoutParams(-1, 48).apply { bottomMargin = 7 })
        }
        page.addView(actionButton("Klaviaturani yoqish", accent, Color.WHITE) {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }, LinearLayout.LayoutParams(-1, 50).apply { topMargin = 18 })
        page.addView(actionButton("Klaviaturani tanlash", Color.rgb(35,145,110), Color.WHITE) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }, LinearLayout.LayoutParams(-1, 50).apply { topMargin = 10 })
        page.addView(TextView(this).apply {
            text = "Imkoniyatlar: Oʻ/Gʻ/Sh/Ch/Ng tugmalari, 4 ta rang mavzusi, raqam va belgilar, emoji, clipboarddan joylash. Ilova yozgan matningizni internetga yubormaydi."
            textSize = 13f; setTextColor(muted); setPadding(2, 22, 2, 0)
        })
        setContentView(page)
    }
    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color); cornerRadius = radius; setStroke(1, Color.rgb(225,230,238))
    }
    private fun actionButton(label: String, backgroundColor: Int, textColor: Int, click: () -> Unit) =
        Button(this).apply {
            text = label; isAllCaps = false; textSize = 15f; setTextColor(textColor)
            background = rounded(backgroundColor, 16f); stateListAnimator = null
            setOnClickListener { click() }
        }
}
