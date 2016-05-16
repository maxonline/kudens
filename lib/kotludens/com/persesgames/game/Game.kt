package com.persesgames.game

import com.persesgames.math.Matrix4
import com.persesgames.texture.Textures
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.browser.window

/**
 * Created by rnentjes on 19-4-16.
 */

class DefaultScreen: Screen() {
    override fun update(time: Float) {
    }

    override fun render() {
        // show loading  message?
        Game.gl().clearColor(1f, 1f, 0f, 1f)
        Game.gl().clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    }
}

enum class ViewType {
    PROJECTION,
    WIDTH,
    HEIGHT,
    ABSOLUTE
}

class View(
  var windowWidth: Int = 2000,
  var windowHeight: Int =1000,
  var width: Float = 1024f,
  var height: Float = 1024f,
  var angle: Float = 60f,
  var near: Float = -0.1f,
  var far: Float = -100f,
  var minAspectRatio: Float = 1f,
  var maxAspectRatio: Float = 1f,
  var viewType: ViewType = ViewType.WIDTH) {
    var vMatrix = Matrix4()
    var aspectRatio = 1f

    init {
        updateView()
    }

    fun updateView() {
        aspectRatio = windowWidth / windowHeight.toFloat()

        if (aspectRatio < minAspectRatio) {

        }

        if (aspectRatio > maxAspectRatio) {

        }

        when(viewType) {
            ViewType.ABSOLUTE -> {
                vMatrix.setOrthographicProjection(0f, width, 0f, height, near, far)
            }
            ViewType.WIDTH -> {
                height = width / aspectRatio

                vMatrix.setOrthographicProjection(-width / 2, width / 2, -height / 2, height / 2, near, far)
            }
            ViewType.HEIGHT -> {
                width = height * aspectRatio

                vMatrix.setOrthographicProjection(-width / 2, width / 2, -height / 2, height / 2, near, far)
            }
            ViewType.PROJECTION -> {
                vMatrix.setPerspectiveProjection(angle, aspectRatio, near, far);
            }
            else -> {
                throw IllegalStateException("ViewType $viewType not implemented!")
            }
        }

        println("width: $width, height: $height")
    }

    fun setToWidth(width: Float) {
        this.width = width
        this.viewType = ViewType.WIDTH

        updateView()
    }

    fun setToHeight(height: Float) {
        this.height = height
        this.viewType = ViewType.HEIGHT

        updateView()
    }

    fun setProjection(angle: Float) {
        this.angle = angle
        this.viewType = ViewType.PROJECTION

        updateView()
    }

    fun setNear(near: Float) {
        this.near = near

        updateView()
    }

    fun setFar(far: Float) {
        this.far = far

        updateView()
    }
}

class HTMLElements {
    var container: HTMLElement
    var webgl: WebGLRenderingContext
    var canvas2d: CanvasRenderingContext2D

    init {
        container = document.createElement("div") as HTMLElement

        val webGlCanvas = document.createElement("canvas") as HTMLCanvasElement
        val canvas = document.createElement("canvas") as HTMLCanvasElement

        container.setAttribute("style", "position: relative;")
        webGlCanvas.setAttribute("style", "position: absolute; left: 0px; top: 0px;" )
        canvas.setAttribute("style", "position: absolute; left: 0px; top: 0px; z-index: 10; width: 1000px; height: 500px;" )

        document.body!!.appendChild(container)
        container.appendChild(webGlCanvas)
        container.appendChild(canvas)

        webgl = webGlCanvas.getContext("webgl") as WebGLRenderingContext
        canvas2d = canvas.getContext("2d") as CanvasRenderingContext2D
    }
}

object Game {
    var started = false
    val view: View = View()
    val html: HTMLElements by lazy { HTMLElements() }
    var currentScreen: Screen = DefaultScreen()
    var start = Date().getTime()
    var currentTime = start
    var currentDelta = 0f

    fun gl() = html.webgl

    fun resize() {
        val canvas = gl().canvas

        // Check if the canvas is not the same size.
        val windowWidth = window.innerWidth.toInt()
        val windowHeight = window.innerHeight.toInt()

        if (view.windowWidth != windowWidth ||
            view.windowHeight != windowHeight) {
            view.windowWidth = windowWidth
            view.windowHeight = windowHeight

            view.updateView()

            val textCanvas = html.canvas2d.canvas

            // Make the canvas the same size
            canvas.width = view.width.toInt()
            canvas.height = view.height.toInt()

            textCanvas.width = view.width.toInt()
            textCanvas.height = view.height.toInt()

            html.canvas2d.fillStyle = "green"
            html.canvas2d.font = "bold 36pt Arial"
            html.canvas2d.fillText("Hello World!", 10.0, 40.0)

            gl().viewport(0, 0, view.width.toInt(), view.height.toInt())
            canvas.setAttribute("style", "position: absolute; left: 0px; top: 0px; z-index: 5; width: ${view.windowWidth}px; height: ${view.windowHeight}px;" )
            textCanvas.setAttribute("style", "position: absolute; left: 0px; top: 0px; z-index: 10; width: ${view.windowWidth}px; height: ${view.windowHeight}px;" )
        }
    }

    fun start(startScreen: Screen) {
        if (started) {
            throw IllegalStateException("You can only start a game once!")
        }

        setScreen(startScreen)

        // start game loop
        started = true
        gameLoop()
    }

    fun setScreen(screen: Screen) {
        currentScreen.closeResources()

        currentScreen = screen

        currentScreen.loadResources()
    }

    fun gameLoop() {
        if (!Textures.ready()) {
            Game.gl().clearColor(1f, 0f, 0f, 1f)
            Game.gl().clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
        } else {
            resize();

            val time = Date().getTime()
            currentDelta = (currentTime - time) / 1000f
            currentTime = time

            currentScreen.update(currentDelta);
            currentScreen.render();
        }

        window.requestAnimationFrame {
            gameLoop()
        }
    }

}
