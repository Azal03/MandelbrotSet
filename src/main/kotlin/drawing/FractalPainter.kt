package drawing

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toComposeImageBitmap
import drawing.convertation.ColorType
import drawing.convertation.Converter
import drawing.convertation.Plane
import drawing.convertation.colorFunc
import math.Complex
import math.fractals.AlgebraicFractal
import tools.ActionStack
import java.awt.image.BufferedImage
import kotlin.concurrent.thread
import kotlin.math.abs

class FractalPainter(private val fractal: AlgebraicFractal) : Painter {

    var plane: Plane? = null
    override var width: Int
        get() = plane?.width?.toInt() ?: 0
        set(value) { plane?.width = value.toFloat() }
    override var height: Int
        get() = plane?.height?.toInt() ?: 0
        set(value) { plane?.height = value.toFloat() }


    var colorFuncID: ColorType = ColorType.Zero
    val actionStack = ActionStack(this)
    var img = BufferedImage(
        1,
        1,
        BufferedImage.TYPE_INT_ARGB,
    )
    var refresh = true

    var xMax = 1.0
    var xMin = -2.0
    var yMin = -1.0
    var yMax = 1.0
//
//    val dwh: Double
//        get() = width * 1.0 / height

//    fun proportions(){
//        val xLen = abs(xMax - xMin)
//        val yLen = abs(yMax - yMin)
//        if(abs(xLen/yLen - dwh) > 1E-6){
//            if(xLen/yLen < dwh){
//                val dx = yLen * dwh - xLen
//                xMax += dx/2
//                xMin -= dx/2
//            }
//            if(xLen/yLen > dwh){
//                val dy = xLen / dwh - yLen
//                yMax += dy/2
//                yMin -= dy/2
//            }
//        }
//    }
//
    private fun scoping(){
        val X = abs(xMax - xMin) / width
        val Y = abs(yMax - yMin) / height
        if(Y > X) {
            val dx = (width * Y- abs(xMax - xMin))/2.0
            plane?.let {plane->
                plane.xMin =  xMin - dx
                plane.xMax = xMax + dx
                plane.yMax = yMax
                plane.yMin = yMin
            }

        }
        else {
            val dy = (height * X- abs((yMax - yMin)))/2.0
            plane?.let {plane->
                plane.yMin =  yMin - dy
                plane.yMax = yMax + dy
                plane.xMax = xMax
                plane.xMin = xMin
            }
        }
    }

    override fun paint(scope: DrawScope) {
        if (refresh) {
            refresh = false
            img = BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB,
            )
            getImageFromPlane(img)
        }
        scope.drawImage(img.toComposeImageBitmap())
    }

    fun getImageFromPlane(img:BufferedImage):BufferedImage{
        scoping()
        plane?.let { plane ->
            val tc = Runtime.getRuntime().availableProcessors()
            List(tc) { t ->
                thread {
                    for (i in t..<width step tc)
                        for (j in 0..<height) {
                            val x = Complex(
                                Converter.xScr2Crt(i.toFloat(), plane),
                                Converter.yScr2Crt(j.toFloat(), plane)
                            )
                            val colorFunk = colorFunc(colorFuncID.value)
                            img.setRGB(i, j, colorFunk(fractal.isInSet(x)).toArgb())
                        }
                }
            }.forEach { it.join() }
        }
        return img
    }

}