package com.navio.sketches_and_location.data

   data class AnnotationCanvas(
        var x: Float,
        var y: Float,
        var comments: MutableList<CommentCanvas>
    ) : OperationCanvas() {

       override fun modifyPosition(x: Float, y: Float) {
           this.x = x
           this.y = y
       }
   }