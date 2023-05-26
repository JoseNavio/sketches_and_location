package com.navio.sketches_and_location.data

abstract class OperationCanvas {

    protected val copyOffset = 100f

    //Modify object render position
    abstract fun modifyPosition(x: Float, y: Float)

    //Returns a displaced copy of this object
    abstract fun copyOperation(): OperationCanvas
}