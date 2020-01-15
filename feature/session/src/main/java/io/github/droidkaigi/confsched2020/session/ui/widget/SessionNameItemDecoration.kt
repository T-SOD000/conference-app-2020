package io.github.droidkaigi.confsched2020.session.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.recyclerview.widget.RecyclerView
import io.github.droidkaigi.confsched2020.session.R

class SessionNameItemDecoration(
    context: Context,
    private val getGroupId: (Int) -> Long,
    private val getInitial: (Int) -> String
) : RecyclerView.ItemDecoration() {

    private val textPaint: TextPaint
    private val labelPadding: Int
    private val fontMetrics: Paint.FontMetrics

    init {
        val resource = context.resources

        val attrs = context.obtainStyledAttributes(
            R.style.TextAppearance_DroidKaigi_Headline6,
            R.styleable.SearchHeader
        )
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = attrs.getColorOrThrow(R.styleable.SearchHeader_android_textColor)
            textSize = attrs.getDimensionOrThrow(R.styleable.SearchHeader_android_textSize)
            try {
                typeface = ResourcesCompat.getFont(
                    context,
                    attrs.getResourceIdOrThrow(R.styleable.SearchHeader_android_fontFamily)
                )
            } catch (_: Exception) {
                // ignore
            }
        }
        attrs.recycle()

        fontMetrics = textPaint.fontMetrics

        labelPadding = resource.getDimensionPixelSize(R.dimen.session_time_space) / 2
    }

    override fun onDraw(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDraw(c, parent, state)

        val totalItemCount = state.itemCount
        val childCount = parent.childCount
        val lineHeight = textPaint.textSize + fontMetrics.descent
        var previousGroupId: Long
        var groupId: Long = EMPTY_ID

        (0 until childCount).forEach {
            val view = parent.getChildAt(it)
            val position = parent.getChildAdapterPosition(view)
            if (position < 0) return@forEach
            // Acquires the first character of the immediately preceding character and the Id of the character to be checked this time
            previousGroupId = groupId
            groupId = getGroupId(position)

            // If the current element is EMPTY or the same as the previous element,
            // there is nothing (if it differs from the previous element, proceed next)
            if (groupId == EMPTY_ID || previousGroupId == groupId) return@forEach

            // Get Initial and check if it is empty character
            val initial = getInitial(position)
            if (initial.isEmpty()) return@forEach

            // drawing
            val positionX = labelPadding - textPaint.measureText(initial) / 2
            val viewBottom = view.bottom + view.paddingBottom
            var positionY = view.top.coerceAtLeast(view.paddingTop) + labelPadding.toFloat()
            if (position + 1 < totalItemCount) {
                val nextGroupId = getGroupId(position + 1)
                if (nextGroupId != groupId && viewBottom < positionY + lineHeight) {
                    positionY = positionY.coerceAtMost(viewBottom.toFloat())
                }
            }
            c.drawText(initial, positionX, positionY, textPaint)
        }
    }

    companion object {
        const val EMPTY_ID: Long = -1
        const val DEFAULT_INITIAL = "*"
        const val DEFAULT_TITLE = "******"
    }
}