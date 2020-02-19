package ru.skillbranch.devintensive.ui.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import ru.skillbranch.devintensive.R
import ru.skillbranch.devintensive.extensions.dpToPx
import ru.skillbranch.devintensive.extensions.pxToDp
import ru.skillbranch.devintensive.utils.Utils

/* Custom circle ImageView with borders */
class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_BORDER_COLOR = Color.WHITE
        private const val DEFAULT_BORDER_WIDTH = 2
        private const val DEFAULT_SIZE = 40
        private const val DEFAULT_INITIALS = ""
        private const val DEFAULT_INITIALS_TEXT_COLOR = Color.WHITE
        private const val DEFAULT_INITIALS_BACKGROUND_COLOR = R.attr.colorAccent

        val bgColors = arrayOf(
            Color.parseColor("#7BC862"),
            Color.parseColor("#E17076"),
            Color.parseColor("#FAA774"),
            Color.parseColor("#6EC9CB"),
            Color.parseColor("#65AADD"),
            Color.parseColor("#A695E7"),
            Color.parseColor("#EE7AAE"),
            Color.parseColor("#2196F3")
        )
    }

    @ColorInt
    private var borderColor: Int = DEFAULT_BORDER_COLOR
    @Px
    private var borderWidth: Float = context.dpToPx(DEFAULT_BORDER_WIDTH)

    private var initials: String = DEFAULT_INITIALS
    private var initialsTextColor: Int = DEFAULT_INITIALS_TEXT_COLOR
    private var initialsBackgroundColor: Int = DEFAULT_INITIALS_BACKGROUND_COLOR

    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()
    private val borderRect = Rect()

    private var isImageMode = true

    init {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView)
            borderColor =
                typedArray.getColor(
                    R.styleable.CircleImageView_cv_borderColor,
                    DEFAULT_BORDER_COLOR
                )
            borderWidth = typedArray.getDimension(
                R.styleable.CircleImageView_cv_borderWidth,
                context.dpToPx(DEFAULT_BORDER_WIDTH)
            )
            initials =
                typedArray.getString(R.styleable.CircleImageView_cv_initials) ?: DEFAULT_INITIALS

            typedArray.recycle()
        }

        // Get colorAccent and set it to initialsBackgroundColor
        /*1st way*/
//        val attributes = intArrayOf(R.attr.colorAccent)
//        val typedArray = context.obtainStyledAttributes(R.style.AppTheme, attributes)
//        initialsBackgroundColor = typedArray.getColor(0, DEFAULT_INITIALS_BACKGROUND_COLOR)
//        typedArray.recycle()
        /*2nd way*/
//        val color = Utils.getColorFromResource(context, R.attr.colorAccent)
        /*Not used because of realisation in the drawAvatar*/
//        initialsBackgroundColor = color

        scaleType = ScaleType.CENTER_CROP
        setup()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(initSize, initSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0) return
        with(viewRect) {
            left = 0
            top = 0
            right = w
            bottom = h
        }
        prepareShader(w, h)
    }

    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)

        isImageMode = initials.isBlank()

        if (drawable != null && isImageMode) {
            drawImage(canvas)
        } else {
            drawInitials(canvas)
        }

//        drawImage(canvas)

        // Resize rect
        val halfBorder = (borderWidth / 2).toInt()
        borderRect.set(viewRect)
        borderRect.inset(halfBorder, halfBorder)

        canvas.drawOval(borderRect.toRectF(), borderPaint)
    }

    private fun setup() {
        with(borderPaint) {
            style = Paint.Style.STROKE
            color = borderColor
            strokeWidth = borderWidth
        }
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        if (isImageMode) prepareShader(width, height)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (isImageMode) prepareShader(width, height)
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        if (isImageMode) prepareShader(width, height)
    }

    private fun prepareShader(w: Int, h: Int) {
        if (w == 0 || drawable == null) return
        val srcBm = drawable.toBitmap(w, h, Bitmap.Config.ARGB_8888)
        imagePaint.shader = BitmapShader(srcBm, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    private fun resolveDefaultSize(spec: Int): Int {
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> context.dpToPx(DEFAULT_SIZE).toInt()
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec)
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun drawImage(canvas: Canvas) {
        canvas.drawOval(viewRect.toRectF(), imagePaint)
    }

    private fun drawInitials(canvas: Canvas) {
        // Draw background color
        initialsPaint.color = initialsToColor(initials)
        canvas.drawOval(viewRect.toRectF(), initialsPaint)

        // Draw text
        with(initialsPaint) {
            color = initialsTextColor
            textAlign = Paint.Align.CENTER
            textSize = height * 0.33f
        }
        val offsetY = (initialsPaint.descent() + initialsPaint.ascent()) / 2
        canvas.drawText(initials, viewRect.exactCenterX(), viewRect.exactCenterY() - offsetY, initialsPaint)
    }

    private fun initialsToColor(letters: String): Int {
        val byte = if (!letters.isNullOrEmpty()) letters[0].toByte() else 0
        val len = bgColors.size
        val deg = byte / len.toDouble()
        val index = ((deg % 1) * len).toInt()
        return bgColors[index]
    }

    @Dimension
    fun getBorderWidth(): Int {
        return context.pxToDp(borderWidth)
    }

    fun setBorderWidth(@Dimension dp: Int) {
        borderWidth = context.dpToPx(dp)
        borderPaint.strokeWidth = borderWidth
        invalidate()
    }

    fun getBorderColor(): Int {
        return borderColor
    }

    fun setBorderColor(hex: String) {
        borderColor = Color.parseColor(hex)
        borderPaint.color = borderColor
        invalidate()
    }

    fun setBorderColor(@ColorRes colorId: Int) {
        borderColor = ContextCompat.getColor(context, colorId)
        borderPaint.color = borderColor
        invalidate()
    }

    fun setInitials(initials: String?) {
        if (initials.isNullOrEmpty()){
            this.initials = ""
        } else {
            this.initials = initials
        }
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.isImageMode = isImageMode
        savedState.borderWidth = borderWidth
        savedState.borderColor = borderColor
        savedState.initials = initials
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state)
            isImageMode = state.isImageMode
            borderWidth = state.borderWidth
            borderColor = state.borderColor
            initials = state.initials

            with(borderPaint) {
                color = borderColor
                strokeWidth = borderWidth
            }
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private class SavedState : BaseSavedState, Parcelable {
        var isImageMode = false
        var borderWidth = 0f
        var borderColor = 0
        var initials = ""

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel) : super(src) {
            // restore state from parcel
            isImageMode = src.readInt() == 1
            borderWidth = src.readFloat()
            borderColor = src.readInt()
            initials = src.readString().toString()
        }

        override fun writeToParcel(dst: Parcel, flags: Int) {
            // write state to parcel
            super.writeToParcel(dst, flags)
            dst.writeInt(if (isImageMode) 1 else 0)
            dst.writeFloat(borderWidth)
            dst.writeInt(borderColor)
            dst.writeString(initials)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}