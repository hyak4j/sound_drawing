package com.hyak4j.sounddrawing.model

import android.graphics.Path

/*
    記錄每次筆劃的 顏色、粗度及軌跡
 */
class DrewPath(var color: Int, var brushSize: Int, var path: Path)