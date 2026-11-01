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
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private val ink = Color.rgb(27, 35, 49)
    private val muted = Color.rgb(105, 116, 132)
    private val blue = Color.rgb(42, 112, 225)
    private val prefs by lazy { getSharedPreferences("keyboard", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun showHome() {
        val scroll = ScrollView(this).apply { setBackgroundColor(Color.rgb(244,247,252)) }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 34, 22, 30)
        }
        page.addView(TextView(this).apply {
            text = "O‘ZBEK\nKEYBOARD"
            textSize = 32f
            setTextColor(ink)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            letterSpacing = 0.015f
        })
        page.addView(TextView(this).apply {
            text = "O‘zbekcha yozishning yangi va qulay usuli"
            textSize = 16f
            setTextColor(muted)
            setPadding(0, 8, 0, 22)
        })
        val preview = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(18, 22, 18, 22)
            background = shape(Color.rgb(28, 42, 67), 24f)
        }
        preview.addView(TextView(this).apply {
            text = "Ğ ğ     Ö ö     Ş ş     Ç ç"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })
        preview.addView(TextView(this).apply {
            text = "Yangi alifbo. Katta tugmalar. Oson yozish."
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(210,222,242))
            setPadding(0, 12, 0, 0)
        })
        page.addView(preview, LinearLayout.LayoutParams(-1, -2))
        page.addView(sectionTitle("KLAVIATURA MAVZUSI"))
        val themes = listOf(
            Triple("Classic", "☀", Color.rgb(242,244,248)),
            Triple("Dark", "☾", Color.rgb(37,42,53)),
            Triple("Neon", "✦", Color.rgb(65,45,83)),
            Triple("Mint", "❋", Color.rgb(218,242,231))
        )
        val selected = prefs.getInt("theme", 0)
        themes.forEachIndexed { index, item ->
            val chosen = selected == index
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 0, 16, 0)
                background = shape(if (chosen) Color.rgb(229,239,255) else Color.WHITE, 16f,
                    if (chosen) blue else Color.rgb(225,230,238))
                setOnClickListener {
                    prefs.edit().putInt("theme", index).apply()
                    showHome()
                }
            }
            row.addView(TextView(this).apply {
                text = item.second
                textSize = 23f
                setTextColor(blue)
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(48, -1))
            row.addView(TextView(this).apply {
                text = item.first
                textSize = 16f
                setTextColor(ink)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER_VERTICAL
            }, LinearLayout.LayoutParams(0, -1, 1f))
            row.addView(TextView(this).apply {
                text = if (chosen) "TANLANGAN ✓" else "TANLASH"
                textSize = 11f
                setTextColor(if (chosen) blue else muted)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            })
            page.addView(row, LinearLayout.LayoutParams(-1, 58).apply { bottomMargin = 9 })
        }
        page.addView(sectionTitle("BOSHLASH"))
        page.addView(actionTile("1", "Klaviaturani yoqish", "Android sozlamalaridan O‘ZBEK KEYBOARD ni yoqing", blue) {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })
        page.addView(actionTile("2", "Klaviaturani tanlash", "Yozish maydonida O‘ZBEK KEYBOARD ni belgilang", Color.rgb(24,145,111)) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }, LinearLayout.LayoutParams(-1, 86).apply { topMargin = 10 })
        page.addView(TextView(this).apply {
            text = "MAXFIYLIK"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(muted)
            setPadding(2, 24, 0, 6)
        })
        page.addView(TextView(this).apply {
            text = "Klaviatura yozgan matningizni internetga yubormaydi. Mavzuni klaviaturaning 🎨 tugmasi orqali ham almashtirishingiz mumkin."
            textSize = 14f
            setTextColor(muted)
            setPadding(16, 16, 16, 16)
            background = shape(Color.WHITE, 16f)
        })
        scroll.addView(page)
        setContentView(scroll)
    }

    private fun sectionTitle(label: String) = TextView(this).apply {
        text = label
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(muted)
        setPadding(2, 24, 0, 12)
    }

    private fun actionTile(number: String, title: String, subtitle: String, color: Int, click: () -> Unit) =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14, 10, 14, 10)
            background = shape(color, 18f)
            setOnClickListener { click() }
            addView(TextView(this@MainActivity).apply {
                text = number
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                background = shape(Color.argb(45,255,255,255), 14f)
            }, LinearLayout.LayoutParams(48, 52))
            val labels = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 0, 0, 0)
                addView(TextView(this@MainActivity).apply {
                    text = title
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.WHITE)
                })
                addView(TextView(this@MainActivity).apply {
                    text = subtitle
                    textSize = 11f
                    setTextColor(Color.rgb(235,242,255))
                    setPadding(0, 3, 0, 0)
                })
            }
            addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
            addView(TextView(this@MainActivity).apply {
                text = "›"
                textSize = 30f
                setTextColor(Color.WHITE)
            })
        }

    private fun shape(color: Int, radius: Float, stroke: Int = Color.TRANSPARENT) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            if (stroke != Color.TRANSPARENT) setStroke(2, stroke)
        }
}
