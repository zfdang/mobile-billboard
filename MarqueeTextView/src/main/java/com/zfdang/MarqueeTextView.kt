package com.zfdang

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.ceil

/**
 * 文本横向滚动，跑马灯效果
 * - 设置的内容不要太长，建议不要超过 [String] 最大长度的 1/5
 * @author JianXin
 * @date 2021-07-22 11:59
 */
open class MarqueeTextView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val TAG = "MarqueeTextView"
        const val BLANK = " "
        const val REPEAT_SINGLE = 1 //一次结束
        const val REPEAT_SINGLE_LOOP = 0 //单个循序
        const val REPEAT_FILL_LOOP = -1 // 填充后循环
    }

    /***
     * 滚动速度
     */
    var speed = 1f
        set(value) {
            if (value <= 0) {
                field = 0f
                stop()
            } else {
                field = value
            }
        }

    /**
     * 文本内容
     */
    var text = ""
        set(value) {
            if (value.isEmpty()) {
                return
            }
            field = value
            var targetContent = value.trim()
            if (isResetLocation) { // 控制重新设置文本内容的时候，是否初始化xLocation。
                xLocation = width * leftMarginPercentage
            }

            // 根据text之间的距离，补足空格
            val endingBlanks = getEndingBlanks()
            targetContent += endingBlanks

            // 根据模式计算宽度
            if (repeat == REPEAT_FILL_LOOP) {
                mFinalDrawText = ""
                //计算文本的宽度
                mSingleContentWidth = getTextWidth(targetContent)
                if (mSingleContentWidth > 0) {
                    // 最大可见内容项数
                    val maxVisibleCount = ceil(width / mSingleContentWidth.toDouble()).toInt() + 1
                    repeat(maxVisibleCount) {
                        mFinalDrawText += targetContent
                    }
                }
                mContentWidth = getTextWidth(mFinalDrawText)
            } else {
                if (xLocation < 0 && repeat == REPEAT_SINGLE) {
                    if (abs(xLocation) > mContentWidth) {
                        xLocation = width * leftMarginPercentage
                    }
                }
                mFinalDrawText = targetContent
                mContentWidth = getTextWidth(mFinalDrawText)
                mSingleContentWidth = mContentWidth
            }
            textHeight = getTextHeight()
            invalidate()
        }

    /**
     * 最终绘制显示的文本
     */
    private var mFinalDrawText: String = ""

    /**
     * 文字颜色
     */
    @ColorInt
    var textColor = Color.BLACK
        set(value) {
            if (value != field) {
                field = value
                textPaint.color = textColor
                invalidate()
            }
        }

    /**
     * 字体大小
     */
    @Px
    var textSize = 12f
        set(value) {
            if (value > 0 && value != field) {
                field = value
                textPaint.textSize = value
                // call setText to reset
                if (text.isNotEmpty()) {
                    text = text
                }
            }
        }

    /**item间距，*/
    @Px
    var textItemDistance = 50f
        set(value) {
            if (field == value) {
                return
            }
            field = if (value < 0f) 0f else value
            // call setText to reset
            if (text.isNotEmpty()) {
                text = text
            }
        }

    /**
     * 滚动模式
     */
    var repeat = REPEAT_SINGLE_LOOP
        set(value) {
            if (value != field) {
                field = value
                resetInit = true
                // call setText to reset
                text = text
            }
        }

    /**
     * 开始的位置距离左边，0~1，0:最左边，1:最右边，0.5:中间。
     */
    @FloatRange(from = 0.0, to = 1.0)
    var leftMarginPercentage = 0.0f
        set(value) {
            if (value == field) {
                return
            }
            field = when {
                value < 0f -> 0f
                value > 1f -> 1f
                else -> value
            }
        }

    /**
     * 是否重置文本绘制的位置，默认为true
     */
    var isResetLocation = true

    private var xLocation = 0f // 文本的x坐标

//    单个显示内容的宽度
    private var mSingleContentWidth: Float = 0f

    /** 最终绘制的内容的宽度 */
    private var mContentWidth = 0f

    /**是否继续滚动*/
    var isRolling = false
        private set

    /**画笔*/
    protected val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var textHeight = 0f
    private var resetInit = true

    private val mHandler by lazy { MyHandler(this) }

    /**
     * 是否用户主动调用，默认 true
     */
    var isRollByUser = true

    init {
        initAttrs(attrs)
        initPaint()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (resetInit && text.isNotEmpty()) {
            textItemDistance = textItemDistance
            xLocation = width * leftMarginPercentage
            resetInit = false
        }
        val absLocation = abs(xLocation)
        when (repeat) {
            REPEAT_SINGLE -> if (mContentWidth < absLocation) {
                stop()
            }
            REPEAT_SINGLE_LOOP -> if (mContentWidth <= absLocation) {
                //一轮结束
                xLocation = width.toFloat()
            }
            REPEAT_FILL_LOOP -> if (xLocation < 0 && mSingleContentWidth <= absLocation) {
                xLocation = mSingleContentWidth - absLocation
            }
            else ->
                if (mContentWidth < absLocation) {
                    //也就是说文字已经到头了
                    stop()
                }
        }
        //绘制文本
        if (mFinalDrawText.isNotBlank()) {
            canvas.drawText(mFinalDrawText, xLocation, height / 2 + textHeight / 2, textPaint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isRollByUser) {
            startInternal(true)
        }
    }

    override fun onDetachedFromWindow() {
        if (isRolling) {
            stopInternal(false)
        }
        super.onDetachedFromWindow()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility != VISIBLE) {
            stopInternal(false)
        } else {
            if (!isRollByUser) {
                startInternal(false)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        text = text
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MarqueeTextView)
        textColor = a.getColor(R.styleable.MarqueeTextView_android_textColor, textColor)
        isResetLocation = a.getBoolean(R.styleable.MarqueeTextView_marqueeResetLocation, true)
        speed = a.getFloat(R.styleable.MarqueeTextView_marqueeSpeed, 1f)
        textSize = a.getDimension(R.styleable.MarqueeTextView_android_textSize, 12f)
        textItemDistance = a.getDimension(R.styleable.MarqueeTextView_marqueeItemDistance, 50f)
        leftMarginPercentage = a.getFloat(
            R.styleable.MarqueeTextView_marqueeLeftMarginPercentage,
            0f
        )
        repeat = a.getInt(R.styleable.MarqueeTextView_marqueeRepeat, REPEAT_SINGLE_LOOP)
        text = a.getText(R.styleable.MarqueeTextView_android_text)?.toString() ?: ""
        a.recycle()
    }

    /**
     * 刻字机修改
     */
    private fun initPaint() {
        textPaint.apply {
            style = Paint.Style.FILL
            color = textColor
            textSize = textSize
            isAntiAlias = true
            density = 1 / resources.displayMetrics.density
        }
    }

    /**
     * 切换开始暂停
     */
    fun toggle() {
        if (isRolling) {
            stop()
        } else {
            start()
        }
    }

    /**
     * 继续滚动
     */
    fun start() {
        startInternal(true)
    }

    /**
     * [isRollByUser] 是否用户主动调用
     */
    protected fun startInternal(isRollByUser: Boolean) {
        this.isRollByUser = isRollByUser
        stop()
        if (text.isNotBlank()) {
            mHandler.sendEmptyMessage(MyHandler.WHAT_RUN)
            isRolling = true
        }
    }

    /**
     * 停止滚动
     */
    fun stop() {
        stopInternal(true)
    }

    /**
     * [isRollByUser] 是否用户主动调用
     */
    protected fun stopInternal(isRollByUser: Boolean) {
        this.isRollByUser = isRollByUser
        isRolling = false
        mHandler.removeMessages(MyHandler.WHAT_RUN)
    }

    /**
     * 计算出一个空格的宽度
     * @return
     */
    private fun getBlankWidth(): Float {
        return getTextWidth(BLANK)
    }

    private fun getTextWidth(text: String?): Float {
        if (text.isNullOrEmpty()) {
            return 0f
        }
        return textPaint.measureText(text)
    }

    /**
     * 文本高度
     */
    private fun getTextHeight(): Float {
        val fontMetrics = textPaint.fontMetrics
        return abs(fontMetrics.bottom - fontMetrics.top) / 2
    }

    private fun getEndingBlanks(): String {
        val oneBlankWidth = getBlankWidth() //空格的宽度
        var count = 1
        if (textItemDistance > 0 && oneBlankWidth != 0f) {
            count = (ceil(textItemDistance / oneBlankWidth).toInt()) //粗略计算空格数量
        }
        val builder = StringBuilder(count)
        for (i in 0..count) {
            builder.append(BLANK)//间隔字符串
        }
        return builder.toString()
    }

    /**
     * handler
     *
     * @author JianXin
     * @date 2021-07-22 11:43
     */
    private class MyHandler(view: MarqueeTextView) : Handler(Looper.getMainLooper()) {

        companion object {
            internal const val WHAT_RUN = 1001
        }

        private val mRef = WeakReference(view)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == WHAT_RUN) {
                mRef.get()?.apply {
                    if (speed > 0) {
                        xLocation -= speed
                        invalidate()
                        // 10 毫秒绘制一次
                        sendEmptyMessageDelayed(WHAT_RUN, 10)
                    }
                }
            }
        }
    }

}