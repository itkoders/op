package uz.ozbekkeyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.graphics.Color
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val ink = Color.rgb(32, 37, 45)
    private val muted = Color.rgb(105, 112, 122)
    private val accent = Color.rgb(35, 112, 220)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 42, 28, 28)
            setBackgroundColor(Color.rgb(248, 249, 251))
        }
        page.addView(TextView(this).apply {
            text = "O‘ZBEK KEYBOARD"
            textSize = 25f
            setTextColor(ink)
            typeface = Typeface.DEFAULT_BOLD
        })
        page.addView(TextView(this).apply {
            text = "Yangi o‘zbek alifbosi uchun sodda va qulay klaviatura"
            textSize = 15f
            setTextColor(muted)
            setPadding(0, 12, 0, 24)
        })
        page.addView(TextView(this).apply {
            text = "Ğ ğ     Ö ö     Ş ş     Ç ç"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(ink)
            setPadding(12, 24, 12, 24)
            setBackgroundColor(Color.WHITE)
        }, LinearLayout.LayoutParams(-1, -2))
        page.addView(TextView(this).apply {
            text = "Klaviaturani yoqish uchun quyidagi tugmani bosing. So‘ng ro‘yxatdan O‘ZBEK KEYBOARD ni tanlang. Keyin uni asosiy klaviatura sifatida belgilang."
            textSize = 15f
            setTextColor(ink)
            setPadding(0, 24, 0, 18)
        })
        page.addView(actionButton("1. Klaviaturani yoqish") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })
        page.addView(actionButton("2. Klaviaturani tanlash") {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 12 })
        page.addView(TextView(this).apply {
            text = "Maxfiylik: bu boshlang‘ich versiya internetga ulanmaydi va yozilgan matnni saqlamaydi."
            textSize = 13f
            setTextColor(muted)
            setPadding(0, 26, 0, 0)
        })
        setContentView(page)
    }

    private fun actionButton(label: String, click: () -> Unit) =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent))
            setOnClickListener { click() }
        }
}
