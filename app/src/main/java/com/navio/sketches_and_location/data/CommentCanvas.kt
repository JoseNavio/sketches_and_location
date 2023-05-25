package com.navio.sketches_and_location.data

data class CommentCanvas(val text: String, var x: Float, var y: Float): OperationCanvas() {
    override fun modifyPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}
