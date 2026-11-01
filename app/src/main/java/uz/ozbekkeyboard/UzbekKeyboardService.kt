package uz.ozbekkeyboard

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class UzbekKeyboardService : InputMethodService() {
    private var upper = false
    private var capsLock = false
    private var symbols = false
    private var emojiPage = false
    private var themeIndex = 0
    private data class Palette(val background: Int, val key: Int, val special: Int, val accent: Int, val text: Int)
    private val palettes = listOf(
        Palette(Color.rgb(242,244,248), Color.WHITE, Color.rgb(220,228,240), Color.rgb(35,112,220), Color.rgb(30,36,46)),
        Palette(Color.rgb(22,25,32), Color.rgb(48,53,64), Color.rgb(67,76,94), Color.rgb(95,165,255), Color.WHITE),
        Palette(Color.rgb(35,25,48), Color.rgb(65,48,82), Color.rgb(92,68,115), Color.rgb(214,155,255), Color.WHITE),
        Palette(Color.rgb(225,245,239), Color.rgb(250,255,252), Color.rgb(190,225,213), Color.rgb(25,135,100), Color.rgb(25,55,45))
    )
    private val palette get() = palettes[themeIndex]

    override fun onCreate() {
        super.onCreate()
        themeIndex = getSharedPreferences("keyboard", Context.MODE_PRIVATE).getInt("theme", 0).coerceIn(0, palettes.lastIndex)
    }
    override fun onCreateInputView(): View = buildKeyboard()

    private fun buildKeyboard(): View {
        val p = palette
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(3, 5, 3, 8)
            setBackgroundColor(p.background)
        }
        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        toolbar.addView(TextView(this).apply {
            text = "O‘ZBEK KEYBOARD  •  " + listOf("CLASSIC","DARK","NEON","MINT")[themeIndex]
            textSize = 12f; setTextColor(p.accent); typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL; setPadding(8, 0, 0, 0)
        }, LinearLayout.LayoutParams(0, 44, 1f))
        toolbar.addView(keyButton("📋", p.special, 16) { pasteClipboard() }, LinearLayout.LayoutParams(48, 42))
        toolbar.addView(keyButton("🎨", p.special, 17) { cycleTheme() }, LinearLayout.LayoutParams(48, 42))
        toolbar.addView(keyButton(if (upper || capsLock) "⇧" else "↑", p.special, 20) {
            upper = !upper
            refresh()
        }, LinearLayout.LayoutParams(48, 42))
        root.addView(toolbar)

        if (emojiPage) {
            val emojis = listOf("😀","😂","🥰","😍","😎","😭","😊","😘","👍","🙏","❤️","🔥","🎉","✨","💯","🤝","👏","💙","🌹","🤣")
            emojis.chunked(5).forEach { row -> addRow(root, row, 54) { press(it) } }
            addRow(root, listOf("ABC","space","⌫","↵"), 56) { press(it) }
            return root
        }

        val rows = if (!symbols) listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","ğ","h","j","k","l"),
            listOf("⇧","z","x","ç","c","v","b","n","m","⌫"),
            listOf("?123",",","☻","space","ö","ş",".","↵")
        ) else listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("@","#","$","%","&","-","+","(",")","/"),
            listOf("=","*","\"","' ",":",";","!","?","⌫"),
            listOf("ABC",",","☻","space","_","‘","’",".","↵")
        )
        rows.forEachIndexed { index, row -> addRow(root, row, if (index == 3) 56 else 55) { press(it) } }
        return root
    }

    private fun addRow(root: LinearLayout, values: List<String>, height: Int, action: (String) -> Unit) {
        val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        values.forEach { value ->
            val weight = when (value) {
                "space" -> 2.5f
                "⇧", "⌫", "?123", "ABC", "↵" -> 1.35f
                "☻" -> 0.95f
                else -> 1f
            }
            val special = value in listOf("⇧","⌫","?123","ABC","↵","space","☻")
            line.addView(keyButton(display(value), if (special) palette.special else palette.key, if (value.length > 2) 14 else 20) {
                action(value)
            }, LinearLayout.LayoutParams(0, height, weight).apply { setMargins(2, 3, 2, 3) })
        }
        root.addView(line)
    }

    private fun display(value: String): String {
        if (value == "space") return "O‘ZBEK"
        if (value == "☻") return "😊"
        return if (upper || capsLock) value.uppercase() else value
    }

    private fun press(value: String) {
        val ic = currentInputConnection ?: return
        when (value) {
            "⇧" -> { if (upper) capsLock = !capsLock else upper = true }
            "⌫" -> ic.deleteSurroundingText(1, 0)
            "↵" -> {
                val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
                if (action != null && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED)
                    ic.performEditorAction(action) else ic.commitText("\n", 1)
            }
            "?123" -> symbols = true
            "ABC" -> { symbols = false; emojiPage = false }
            "☻" -> emojiPage = true
            "space" -> { ic.commitText(" ", 1); if (!capsLock) upper = false }
            else -> {
                ic.commitText(if (upper || capsLock) value.uppercase() else value, 1)
                if (!capsLock) upper = false
            }
        }
        refresh()
    }

    private fun pasteClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip?.getItemAt(0)?.coerceToText(this)
        if (text != null) currentInputConnection?.commitText(text, 1)
        else Toast.makeText(this, "Bufer bo‘sh", Toast.LENGTH_SHORT).show()
    }
    private fun cycleTheme() {
        themeIndex = (themeIndex + 1) % palettes.size
        getSharedPreferences("keyboard", Context.MODE_PRIVATE).edit().putInt("theme", themeIndex).apply()
        refresh()
    }
    private fun refresh() { setInputView(buildKeyboard()) }

    private fun keyButton(label: String, color: Int, size: Int, click: () -> Unit) =
        Button(this).apply {
            text = label; textSize = size.toFloat(); isAllCaps = false
            setTextColor(palette.text); typeface = Typeface.DEFAULT
            background = GradientDrawable().apply {
                setColor(color); cornerRadius = 13f
                setStroke(1, if (themeIndex == 1) Color.rgb(75,82,96) else Color.rgb(215,221,231))
            }
            setPadding(0, 0, 0, 0); minHeight = 0; minimumHeight = 0
            stateListAnimator = null
            setOnClickListener { click() }
        }
}
