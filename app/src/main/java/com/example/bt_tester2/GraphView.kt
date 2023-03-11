package com.example.bt_tester2

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.RelativeLayout
import java.util.Collections

class GraphView : RelativeLayout {
    var outputsize = 250
    var inputsize = 300

    var midline: Paint? = null
    var graphline = Paint(Paint.ANTI_ALIAS_FLAG)
    var dotpaint = Paint()
    var dothighlight = Paint()
    var rectangle = RectF()

    var inputdata = MutableList(inputsize) {0.0F}

    var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var path = Path()
    var xpadding = 100f
    var ypadding = 50f
    var newdatapoint = 0.0F
    var linesizex = 0f
    var sizex = 0f
    var sizey = 0f
    var xstepsize = 0f
    var ystepsize = 0f
    var xposstart = 0f
    var yposstart = 0f
    var xposstop = 0f
    var yposstop = 0f
    var xdiff = 0f
    var ydiff = 0f
    var ymiddleline = 0f
    var midlinexstart = 0f
    var midlinexend = 0f
    var init: Byte = 0



    //here the new datapoint gets entered
    fun setData(power: Float) {
        Collections.rotate(inputdata, -1)
        inputdata[inputdata.size-1] = -power
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setWillNotDraw(false)
        midline = Paint()
        midline!!.color = Color.rgb(51, 51, 51)
        midline!!.strokeWidth = 7f
        midline!!.isAntiAlias = true
        graphline = Paint()
        graphline.color = Color.WHITE
        graphline.strokeWidth = 10f
        graphline.isAntiAlias = true
        dothighlight = Paint()
        dothighlight.color = Color.rgb(192, 196, 204)
        dothighlight.isAntiAlias = true
        dotpaint = Paint()
        dotpaint.color = Color.rgb(20, 20, 20)
        dotpaint.isAntiAlias = true
        paint.strokeWidth = 1f
        paint.color = Color.rgb(65, 74, 77)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isAntiAlias = true

    }

    constructor(context: Context?) : super(context) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //init all data
        if (init.toInt() == 0) {
            linesizex = super.getMeasuredWidth().toFloat()
            sizex = super.getMeasuredWidth() - xpadding
            sizey = super.getMeasuredHeight() - ypadding
            xstepsize = sizex / outputsize
            ystepsize = sizey / 2 / 100
            ymiddleline = sizey / 2 + ypadding / 2
            midlinexstart = xpadding / 2
            midlinexend = linesizex - xpadding / 2
            init = 1
        }

        //Move data in array and calculate smoothness

//        var avg_newdatapoint = 0.0F
//        for (a in 1..smoothness) {
//            avg_newdatapoint += data[data.size - a]
//        }
//        data[data.size - 1] = avg_newdatapoint / smoothness
        //var newdatalist = lowPassFilter(data.toList(),0.1F)
        //var newdata = newdatalist.toFloatArray()

        val tempdata = lowPassFilter(inputdata, 0.1F).subList(50,300)


        //Draw gray background of Graph
        for (i in 0 until tempdata.size - 1) {
            xposstart = xstepsize * i + xpadding / 2
            yposstart = ystepsize * tempdata[i] + ymiddleline
            xposstop = xstepsize * (i + 1) + xpadding / 2
            yposstop = ystepsize * tempdata[i + 1] + ymiddleline
            xdiff = xposstart - xposstop
            ydiff = yposstart - yposstop
            path.reset()
            path.fillType = Path.FillType.EVEN_ODD
            path.moveTo(xposstart, yposstart)
            path.lineTo(xposstop, yposstop)
            path.lineTo(xposstop, ymiddleline)
            path.lineTo(xposstart, ymiddleline)
            path.close()
            canvas.drawPath(path, paint)
        }

        //Draw middle line, the "x axis" line
        canvas.drawLine(midlinexstart, ymiddleline, midlinexend, ymiddleline, midline!!)
        canvas.drawCircle(midlinexstart, ymiddleline, 4f, midline!!)
        canvas.drawCircle(midlinexend, ymiddleline, 4f, midline!!)

        //Draw the actual white graph line
        for (i in 1 until tempdata.size - 1) {

            xposstart = xstepsize * i + xpadding / 2
            yposstart = ystepsize * tempdata[i] + ymiddleline
            xposstop = xstepsize * (i + 1) + xpadding / 2
            yposstop = ystepsize * tempdata[i + 1] + ymiddleline
            xdiff = xposstart - xposstop
            ydiff = yposstart - yposstop
            val angle = Math.atan((ydiff / xdiff).toDouble())
            xposstop = xposstop + Math.cos(angle).toFloat() * 3
            yposstop = yposstop + Math.sin(angle).toFloat() * 3
            canvas.drawLine(xposstart, yposstart, xposstop, yposstop, graphline)
            if (i == 1) canvas.drawCircle(xposstart, yposstart, 5f, graphline)
        }
        //draw some circles at the end of the graph
        canvas.drawCircle(xposstop, yposstop, 20f, dothighlight)
        canvas.drawCircle(xposstop, yposstop, 10f, dotpaint)

    }

    fun lowPassFilter(data: List<Float>, alpha: Float): List<Float> {
        val filteredData = mutableListOf<Float>()
        var prev = data[0]
        for (value in data) {
            prev = alpha * value + (1 - alpha) * prev
            filteredData.add(prev)
        }
        return filteredData
    }
}