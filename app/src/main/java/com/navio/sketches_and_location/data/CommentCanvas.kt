package com.navio.sketches_and_location.data

data class CommentCanvas(var x: Float, var y: Float, val text: String): OperationCanvas() {
    //Modify original position
    override fun modifyPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
    //Returns a displaced copy of this object
    override fun copyOperation() : OperationCanvas {
        return CommentCanvas(x + copyOffset, y + copyOffset, text)
    }
}
