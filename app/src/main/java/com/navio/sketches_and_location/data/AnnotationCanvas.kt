package com.navio.sketches_and_location.data

data class AnnotationCanvas(
    var x: Float,
    var y: Float,
    var comments: MutableList<CommentCanvas>
) : OperationCanvas() {
    //Modify annotation original position
    override fun modifyPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
    //Returns a displaced copy of this object
    override fun copyOperation(): OperationCanvas {
        return AnnotationCanvas(x + copyOffset, y + copyOffset, comments)
    }
}