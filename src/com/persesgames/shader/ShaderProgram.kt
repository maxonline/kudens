package com.persesgames.shader

import org.khronos.webgl.*

/**
 * User: rnentjes
 * Date: 17-4-16
 * Time: 15:15
 */

class VertextAttributeInfo(val locationName: String, val numElements: Int) {
    var location = 0
    var offset = 0
}

class ShaderProgram(val webgl: WebGLRenderingContext, val mode: Int, vertexShaderSource: String, fragmentShaderSource: String, val vainfo: Array<VertextAttributeInfo>) {

    var shaderProgram: WebGLProgram
    var vertex: WebGLShader
    var fragment: WebGLShader

    var verticesBlockSize = 0
    var currentIndex = 0
    var verticesLength = 0
    var vertices = Float32Array(0)

    var attribBuffer: WebGLBuffer

    init {
        vertex = webgl.createShader(WebGLRenderingContext.VERTEX_SHADER) ?: throw IllegalStateException("Unable to request vertex shader from webgl context!")
        webgl.shaderSource(vertex, vertexShaderSource)
        webgl.compileShader(vertex)

        fragment = webgl.createShader(WebGLRenderingContext.FRAGMENT_SHADER) ?: throw IllegalStateException("Unable to request fragment shader from webgl context!")
        webgl.shaderSource(fragment, fragmentShaderSource)
        webgl.compileShader(fragment)

        shaderProgram = webgl.createProgram() ?: throw IllegalStateException("Unable to request shader program from webgl context!")
        webgl.attachShader(shaderProgram, vertex)
        webgl.attachShader(shaderProgram, fragment)
        webgl.linkProgram(shaderProgram)

        if (webgl.getShaderParameter(vertex, WebGLRenderingContext.COMPILE_STATUS) == false) {
            println(webgl.getShaderInfoLog(vertex))
            throw IllegalStateException("Unable to compile vertex shader!")
        }

        if (webgl.getShaderParameter(fragment, WebGLRenderingContext.COMPILE_STATUS) == false) {
            println(webgl.getShaderInfoLog(fragment))
            throw IllegalStateException("Unable to compile fragment shader!")
        }

        if (webgl.getProgramParameter(shaderProgram, WebGLRenderingContext.LINK_STATUS) == false) {
            println(webgl.getProgramInfoLog(shaderProgram))
            throw IllegalStateException("Unable to compile program!")
        }

        webgl.useProgram(shaderProgram)

        this.verticesBlockSize = 0;

        // set attribute locations...
        for (info in vainfo.iterator()) {
            info.location = webgl.getAttribLocation(shaderProgram, info.locationName)
            info.offset = verticesBlockSize;

            verticesBlockSize += info.numElements;
            println("attrib: ${info.locationName}, info.location: ${info.location}, info.offset: ${info.offset}");
        }

        println("verticesBlockSize $verticesBlockSize");

        this.currentIndex = 0;

        // create vertices buffer
        verticesLength = 4096 - (4096 % verticesBlockSize);
        vertices = Float32Array(verticesLength);

        println("vertices.length ${vertices.length}");

        attribBuffer = webgl.createBuffer() ?: throw IllegalStateException("Unable to create webgl buffer!")
        webgl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, attribBuffer);

        println("ShaderProgram constructor done");
    }

    fun queueVertices(verts: Float32Array) {
        if((currentIndex + verts.length) >= verticesLength) {
            flush();
        }

        vertices.set(verts, currentIndex)
        currentIndex += verts.length
    }

    fun begin() {
        webgl.useProgram(shaderProgram);
        webgl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, attribBuffer);
        currentIndex = 0;

        // set attribute locations...
        for (info in vainfo.iterator()) {
            webgl.enableVertexAttribArray(info.location);
            webgl.vertexAttribPointer(info.location, info.numElements, WebGLRenderingContext.FLOAT, false, verticesBlockSize * 4, info.offset * 4);
        }

    }

    fun flush() {
        if (currentIndex > 0) {
            webgl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, vertices, WebGLRenderingContext.DYNAMIC_DRAW);
            webgl.drawArrays(mode, 0, (currentIndex / verticesBlockSize).toInt());
            currentIndex = 0;
        }
    }

    fun end() {
        flush()
        webgl.useProgram(null)
    }

    fun getAttribLocation(location: String) = webgl.getAttribLocation(shaderProgram, location);

    fun getUniformLocation(location: String) = webgl.getUniformLocation(shaderProgram, location);

    fun setUniform1f(location: String, value: Float) { flush(); webgl.uniform1f(getUniformLocation(location), value); }
    fun setUniform4f(location: String, v1: Float, v2: Float, v3: Float, v4: Float) { flush(); webgl.uniform4f(getUniformLocation(location), v1, v2, v3, v4); }
    fun setUniform1i(location: String, value: Int) { flush(); webgl.uniform1i(getUniformLocation(location), value); }
    fun setUniformMatrix4fv(location: String, value: Float32Array) { flush(); webgl.uniformMatrix4fv(getUniformLocation(location), false, value); }

}
