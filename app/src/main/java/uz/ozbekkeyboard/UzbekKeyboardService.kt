package uz.ozbekkeyboard

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.PopupWindow
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
        val density = resources.displayMetrics.density
        val keyboardHeightDp = (resources.displayMetrics.heightPixels / density * 0.38f).toInt().coerceIn(280, 320)
        val rowHeight = ((keyboardHeightDp - 82) / 4).coerceIn(48, 60)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(3), dp(5), dp(3), dp(7))
            setBackgroundColor(p.background)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(keyboardHeightDp))
        }
        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        toolbar.addView(TextView(this).apply {
            text = "O‘ZBEK KEYBOARD  •  " + listOf("CLASSIC","DARK","NEON","MINT")[themeIndex]
            textSize = 12f; setTextColor(p.accent); typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL; setPadding(8, 0, 0, 0)
        }, LinearLayout.LayoutParams(0, dp(42), 1f))
        toolbar.addView(keyButton("←", p.special, 18) { currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_LEFT)) }, LinearLayout.LayoutParams(dp(38), dp(44)))
        toolbar.addView(keyButton("→", p.special, 18) { currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT)) }, LinearLayout.LayoutParams(dp(38), dp(44)))
        toolbar.addView(keyButton("📋", p.special, 16) { pasteClipboard() }, LinearLayout.LayoutParams(dp(42), dp(44)))
        toolbar.addView(keyButton("🎨", p.special, 17) { cycleTheme() }, LinearLayout.LayoutParams(dp(42), dp(44)))
        toolbar.addView(keyButton(if (upper || capsLock) "⇧" else "↑", p.special, 20) {
            upper = !upper
            refresh()
        }, LinearLayout.LayoutParams(dp(46), dp(44)))
        root.addView(toolbar)

        if (emojiPage) {
            val emojis = listOf("😀","😂","🥰","😍","😎","😭","😊","😘","👍","🙏","❤️","🔥","🎉","✨","💯","🤝","👏","💙","🌹","🤣")
            emojis.chunked(5).forEach { row -> addRow(root, row, rowHeight) { press(it) } }
            addRow(root, listOf("ABC","space","⌫","↵"), rowHeight) { press(it) }
            return root
        }

        val rows = if (!symbols) listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("⇧","z","x","c","v","b","n","m","⌫"),
            listOf("?123",",","☻","space","ğ","ö","ş","ç","↵")
        ) else listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("@","#","$","%","&","-","+","(",")","/"),
            listOf("=","*","\"","' ",":",";","!","?","⌫"),
            listOf("ABC",",","☻","space","_","‘","’",".","↵")
        )
        rows.forEach { row -> addRow(root, row, rowHeight) { press(it) } }
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
            line.addView(keyButton(display(value), if (special) palette.special else palette.key, if (value.length > 2) 14 else 20, {
                action(value)
            }, {
                if (value.length == 1 && value[0].isLetter()) showAlternates(it, value)
                else if (value == "⌫") deleteWord()
            }), LinearLayout.LayoutParams(0, dp(height), weight).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) })
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
            "⌫" -> deleteOneCodePoint()
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
                if (!capsLock) upper = isSentenceStart()
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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun keyButton(label: String, color: Int, size: Int, click: () -> Unit, longClick: ((View) -> Unit)? = null) =
        TextView(this).apply {
            text = label
            textSize = size.toFloat()
            gravity = Gravity.CENTER
            setTextColor(palette.text)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            isClickable = true
            isFocusable = true
            minHeight = dp(48)
            setPadding(dp(2), 0, dp(2), 0)
            background = GradientDrawable().apply {
                setColor(color)
                cornerRadius = dp(11).toFloat()
                setStroke(dp(1), if (themeIndex == 1) Color.rgb(75,82,96) else Color.rgb(215,221,231))
            }
            setOnClickListener {
                performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                click()
            }
            setOnLongClickListener {
                performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                longClick?.invoke(this)
                longClick != null
            }
        }

    private fun showAlternates(anchor: View, value: String) {
        val choices = when (value.lowercase()) {
            "o" -> listOf("o‘","oʻ","ö","ō","ø")
            "g" -> listOf("g‘","gʻ","ğ","ġ")
            "c" -> listOf("ç","č","ć")
            "s" -> listOf("ş","š","ś")
            "a" -> listOf("ä","á","à","â","ā")
            "e" -> listOf("ë","é","è","ê","ē")
            "i" -> listOf("ï","í","ì","î","ī")
            "u" -> listOf("ü","ú","ù","û","ū")
            "n" -> listOf("ñ","ń")
            else -> return
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(5), dp(4), dp(5), dp(4))
            setBackgroundColor(palette.background)
        }
        val popup = PopupWindow(row, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        choices.forEach { choice ->
            row.addView(keyButton(if (upper || capsLock) choice.uppercase() else choice, palette.key, 19, {
                currentInputConnection?.commitText(if (upper || capsLock) choice.uppercase() else choice, 1)
                if (!capsLock) upper = false
                popup.dismiss()
                refresh()
            }), LinearLayout.LayoutParams(dp(48), dp(48)).apply { setMargins(dp(2),0,dp(2),0) })
        }
        popup.elevation = dp(8).toFloat()
        popup.showAsDropDown(anchor, 0, -dp(105))
    }

    private fun deleteOneCodePoint() {
        val ic = currentInputConnection ?: return
        if (Build.VERSION.SDK_INT >= 24) {
            ic.deleteSurroundingTextInCodePoints(1, 0)
        } else {
            val before = ic.getTextBeforeCursor(2, 0)?.toString().orEmpty()
            if (before.length >= 2 && Character.isSurrogatePair(before[before.length-2], before.last()))
                ic.deleteSurroundingText(2, 0)
            else ic.deleteSurroundingText(1, 0)
        }
    }

    private fun deleteWord() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(100, 0)?.toString().orEmpty()
        val trimmed = before.trimEnd()
        val word = trimmed.takeLastWhile { !it.isWhitespace() }
        val count = before.length - trimmed.length + word.length
        if (count > 0) ic.deleteSurroundingText(count, 0)
    }

    private fun isSentenceStart(): Boolean {
        val before = currentInputConnection?.getTextBeforeCursor(3, 0)?.toString().orEmpty()
        return before.isEmpty() || before.lastOrNull()?.let { it == '.' || it == '!' || it == '?' || it == '\\n' } == true
    }
}
