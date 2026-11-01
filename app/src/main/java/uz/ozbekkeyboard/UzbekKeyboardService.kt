package uz.ozbekkeyboard

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class UzbekKeyboardService : InputMethodService() {
    private var upper = false
    private var symbols = false
    private val bg = Color.rgb(239, 241, 245)
    private val key = Color.WHITE
    private val special = Color.rgb(218, 225, 235)
    private val blue = Color.rgb(35, 112, 220)

    override fun onCreateInputView(): View = buildKeyboard()

    private fun buildKeyboard(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(5, 7, 5, 9)
            setBackgroundColor(bg)
        }
        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        top.addView(TextView(this).apply {
            text = "O‘ZBEK"
            textSize = 13f
            setTextColor(blue)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10, 0, 0, 0)
        }, LinearLayout.LayoutParams(0, 38, 1f))
        top.addView(keyButton("⇧", special) { upper = !upper; refresh() }, LinearLayout.LayoutParams(48, 38))
        top.addView(keyButton("⚙", special) { requestShowSelf(0) }, LinearLayout.LayoutParams(48, 38))
        root.addView(top)

        val rows = if (!symbols) listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","ğ","h","j","k","l"),
            listOf("⇧","z","x","ç","c","v","b","n","m","⌫"),
            listOf("?123",",","☻","space","ö","ş","↵")
        ) else listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("@","#","\$","%","&","-","+","(",")","/"),
            listOf("=","*","\"","' ",":",";","!","⌫"),
            listOf("ABC",",","☻","space","‘","’","↵")
        )
        rows.forEachIndexed { index, row ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            row.forEach { value ->
                val weight = when (value) {
                    "⇧", "⌫", "?123", "ABC", "↵" -> 1.35f
                    "space" -> 3.0f
                    "☻" -> 1.0f
                    else -> 1f
                }
                line.addView(keyButton(display(value),
                    if (value in listOf("⇧","⌫","?123","ABC","↵")) special else key
                ) { press(value) }, LinearLayout.LayoutParams(0, if (index == 3) 47 else 45, weight).apply {
                    setMargins(2, 3, 2, 3)
                })
            }
            root.addView(line)
        }
        return root
    }

    private fun display(value: String): String =
        if (value == "space") "␣" else if (upper && value.length == 1 && value[0].isLetter()) value.uppercase() else value

    private fun press(value: String) {
        val ic = currentInputConnection ?: return
        when (value) {
            "⇧" -> upper = !upper
            "⌫" -> ic.deleteSurroundingText(1, 0)
            "↵" -> {
                val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
                if (action != null && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED)
                    ic.performEditorAction(action)
                else ic.commitText("\n", 1)
            }
            "?123" -> symbols = true
            "ABC" -> symbols = false
            "☻" -> ic.commitText("😊", 1)
            "space" -> ic.commitText(" ", 1)
            "," -> ic.commitText(",", 1)
            "." -> ic.commitText(".", 1)
            else -> {
                ic.commitText(if (upper && value.length == 1) value.uppercase() else value, 1)
                if (upper) upper = false
            }
        }
        refresh()
    }

    private fun refresh() { setInputView(buildKeyboard()) }

    private fun keyButton(text: String, color: Int, click: () -> Unit) =
        Button(this).apply {
            this.text = text
            textSize = if (text.length > 1) 13f else 19f
            isAllCaps = false
            setTextColor(Color.rgb(35, 40, 48))
            background = GradientDrawable().apply {
                setColor(color)
                cornerRadius = 11f
                setStroke(1, Color.rgb(225, 228, 233))
            }
            setPadding(0, 0, 0, 0)
            stateListAnimator = null
            setOnClickListener { click() }
        }
}
