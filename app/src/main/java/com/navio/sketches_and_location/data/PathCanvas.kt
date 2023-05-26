package com.navio.sketches_and_location.data

import android.graphics.Matrix
import android.graphics.Path

data class PathCanvas(var x: Float, var y: Float, val path: Path) : OperationCanvas() {

    private fun setPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    //We translate the path stored applying it an offset matrix
    override fun modifyPosition(x: Float, y: Float) {
        //Offset
        val offsetX = x - this.x
        val offsetY = y - this.y
        //Matrix
        val matrix = Matrix()
        matrix.setTranslate(offsetX, offsetY)

        path.transform(matrix)
        //Original position set to new position
        setPosition(x, y)
    }
    //Returns a displaced copy of this object
    override fun copyOperation(): OperationCanvas {
        val newPath = Path(path)

        // Apply translation to the copied path
        val matrix = Matrix()
        matrix.setTranslate(copyOffset, copyOffset)
        newPath.transform(matrix)

        // Create a new instance of PathCanvas with the translated path
        return PathCanvas(x + copyOffset, y + copyOffset, newPath)
    }
}
