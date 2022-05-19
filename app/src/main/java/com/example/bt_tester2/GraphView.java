package com.example.bt_tester2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RelativeLayout;




public class GraphView extends RelativeLayout {




    Paint midline;
    Paint graphline = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint dotpaint =  new Paint();
    Paint dothighlight = new Paint();
    RectF rectangle = new RectF();
    int[] data = new int[250];
    int timer = 0;
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Path path = new Path();
    Path graphpath = new Path();

    float xpadding = 100;
    float ypadding = 50;
    int newdatapoint = 0;
    final int smoothness = 3;

    float linesizex;
    float sizex, sizey;
    float xstepsize, ystepsize;

    float xposstart = 0, yposstart = 0, xposstop = 0, yposstop = 0, xdiff = 0, ydiff = 0;
    float ymiddleline;
    float midlinexstart;
    float midlinexend;


    byte init = 0;




    //here the new datapoint gets entered
    public void setData(int power){
        newdatapoint = -power;
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWillNotDraw(false);

        midline = new Paint();
        midline.setColor(Color.rgb(51,51,51));
        midline.setStrokeWidth(7);
        midline.setAntiAlias(true);


        graphline = new Paint();
        graphline.setColor(Color.WHITE);
        graphline.setStrokeWidth(10);
        graphline.setAntiAlias(true);

        dothighlight = new Paint();
        dothighlight.setColor(Color.rgb(192,196,204));
        dothighlight.setAntiAlias(true);

        dotpaint = new Paint();
        dotpaint.setColor(Color.rgb(20,20,20));
        dotpaint.setAntiAlias(true);

        paint.setStrokeWidth(1);
        paint.setColor(Color.rgb(65,74,77));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);


    }

    public GraphView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        //init all data
        if(init == 0) {
            linesizex = super.getMeasuredWidth();
            sizex = super.getMeasuredWidth() - xpadding;
            sizey = super.getMeasuredHeight() - ypadding;
            xstepsize = (float) sizex / data.length;
            ystepsize = (float) sizey / 2 / 100;
            ymiddleline = sizey / 2 + ypadding / 2;
            midlinexstart = xpadding / 2;
            midlinexend = linesizex - xpadding / 2;
            init = 1;
        }

        //Move data in array and calculate smoothness
        if (data.length - 1 >= 0) System.arraycopy(data, 1, data, 0, data.length - 1);
        data[data.length-1] = newdatapoint;
        int avg_newdatapoint=0;
        for(int a = 1; a <= smoothness; a++){
            avg_newdatapoint+=data[data.length-a];
        }
        data[data.length-1] = avg_newdatapoint/smoothness;

        //Draw gray background of Graph
        for(int i = 0; i < data.length-1; i++){
            xposstart = xstepsize*i + xpadding/2;
            yposstart = ystepsize*data[i]+ymiddleline;
            xposstop = xstepsize*(i+1) + xpadding/2;
            yposstop = ystepsize*data[i+1]+ymiddleline;
            xdiff = xposstart-xposstop;
            ydiff = yposstart-yposstop;

            path.reset();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(xposstart,yposstart);
            path.lineTo(xposstop,yposstop);
            path.lineTo(xposstop,ymiddleline);
            path.lineTo(xposstart,ymiddleline);
            path.close();
            canvas.drawPath(path,paint);
        }

        //Draw middle line, the "x axis" line
        canvas.drawLine(midlinexstart,ymiddleline,midlinexend,ymiddleline,midline);
        canvas.drawCircle(midlinexstart,ymiddleline,4,midline);
        canvas.drawCircle(midlinexend,ymiddleline,4,midline);

        //Draw the actual white graph line
        for(int i = 1; i < data.length-1; i++){
            xposstart = xstepsize*i + xpadding/2;
            yposstart = ystepsize*data[i]+ymiddleline;
            xposstop = xstepsize*(i+1) + xpadding/2;
            yposstop = ystepsize*data[i+1]+ymiddleline;
            xdiff = xposstart-xposstop;
            ydiff = yposstart-yposstop;

            double angle = Math.atan(ydiff/xdiff);
            xposstop = xposstop+(float)Math.cos(angle)*3;
            yposstop = yposstop+(float)Math.sin(angle)*3;

            canvas.drawLine(xposstart,yposstart,xposstop,yposstop,graphline);

            if (i==1) canvas.drawCircle(xposstart,yposstart,5,graphline);
        }
        //draw some circles at the end of the graph
        canvas.drawCircle(xposstop,yposstop,20,dothighlight);
        canvas.drawCircle(xposstop,yposstop,10,dotpaint);

        timer++;
    }
}
