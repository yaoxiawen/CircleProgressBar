package com.example.circleprogressbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import kotlin.math.min


class CircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_MAX = 100
        private const val DEFAULT_PROGRESS = 0
        private const val DEFAULT_START_ANGLE = -90f
        private const val DEFAULT_END_ANGLE = 270f
        private const val DEFAULT_REVERSE = false
        private const val DEFAULT_ROUND_CAP = true
        private val DEFAULT_RING_WIDTH = UiUtils.dp2px(5f).toFloat()
        private val DEFAULT_RING_COLOR = Color.parseColor("#0888FF")
        private val DEFAULT_RING_BACKGROUND_COLOR = Color.parseColor("#EFEFEF")
    }

    //进度最大值
    private var max = DEFAULT_MAX

    //当前进度
    private var progress = DEFAULT_PROGRESS

    //绘制开始的角度
    private var startAngle = DEFAULT_START_ANGLE

    //绘制结束的角度
    private var endAngle = DEFAULT_END_ANGLE

    //进度条是否逆时针滚动，默认为false，即顺时针滚动
    private var reverse = DEFAULT_REVERSE

    //是否设置画笔帽为Paint.Cap.ROUND，即圆形样式，默认为true
    private var roundCap = DEFAULT_ROUND_CAP

    //圆环宽度
    private var ringWidth = DEFAULT_RING_WIDTH

    //圆环颜色
    private var ringColor = DEFAULT_RING_COLOR

    //圆环背景色
    private var ringBackgroungColor = DEFAULT_RING_BACKGROUND_COLOR

    private lateinit var mArcPaint: Paint        //绘制圆环
    private lateinit var mRectF: RectF           //圆环对应的RectF

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    init {
        attrs?.let {
            parseAttribute(getContext(), it)
        }
        initPaint()
    }

    //获取布局属性并设置属性默认值
    private fun parseAttribute(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar)
        max = ta.getInt(R.styleable.CircleProgressBar_ringMax, DEFAULT_MAX)
        if (max <= 0) {
            max = DEFAULT_MAX
        }
        startAngle = ta.getFloat(R.styleable.CircleProgressBar_startAngle, DEFAULT_START_ANGLE)
        endAngle = ta.getFloat(R.styleable.CircleProgressBar_endAngle, DEFAULT_END_ANGLE)
        reverse = ta.getBoolean(R.styleable.CircleProgressBar_reverse, DEFAULT_REVERSE)
        roundCap = ta.getBoolean(R.styleable.CircleProgressBar_roundCap, DEFAULT_ROUND_CAP)
        progress = ta.getInt(R.styleable.CircleProgressBar_progress, DEFAULT_PROGRESS)
        ringWidth =
            ta.getDimension(R.styleable.CircleProgressBar_ringWidth, DEFAULT_RING_WIDTH)
        ringColor = ta.getColor(R.styleable.CircleProgressBar_ringColor, DEFAULT_RING_COLOR)
        ringBackgroungColor = ta.getColor(
            R.styleable.CircleProgressBar_ringBackgroundColor,
            DEFAULT_RING_BACKGROUND_COLOR
        )
        ta.recycle()
    }

    //初始化画笔
    private fun initPaint() {
        mArcPaint = Paint()
        mArcPaint.isAntiAlias = true
    }

    //重写onMeasure支持wrap_content
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec))
    }

    private fun measure(measureSpec: Int): Int {
        var result = 0
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = UiUtils.dp2px(90f)
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        return result
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = measuredWidth
        mHeight = measuredHeight
        mRectF = RectF(
            ringWidth / 2.0f, ringWidth / 2.0f,
            mWidth - ringWidth / 2.0f, mHeight - ringWidth / 2.0f
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawArc(it)
        }
    }


    //绘制圆环
    private fun drawArc(canvas: Canvas) {
        with(mArcPaint) {
            color = ringBackgroungColor
            style = Paint.Style.STROKE
            strokeWidth = ringWidth
            if (roundCap) {
                strokeCap = Paint.Cap.ROUND
            }
        }
        //绘制圆环背景
        canvas.drawArc(mRectF, startAngle, endAngle - startAngle, false, mArcPaint)
        with(mArcPaint) {
            color = ringColor
            if (roundCap) {
                strokeCap = Paint.Cap.ROUND
            }
        }
        var sweepAngle = progress.toFloat() / max * (endAngle - startAngle)
        if (reverse) {
            sweepAngle = -sweepAngle  //逆时针滚动
        }
        canvas.drawArc(mRectF, startAngle, sweepAngle, false, mArcPaint)
    }

    fun setMaxValue(max: Int) {
        this.max = max
        invalidate()
    }

    fun setProgress(progress: Int) {
        this.progress = correctProgress(progress)
        invalidate()
    }

    private fun correctProgress(progress: Int): Int {
        var progress = progress
        if (progress > max) {  //处理错误输入progress大于max的情况
            if (progress % max == 0) {
                progress = max
            } else {
                progress %= max
            }
        }
        return progress
    }
}