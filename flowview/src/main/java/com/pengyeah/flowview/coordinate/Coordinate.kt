package com.pengyeah.flowview.coordinate

import com.pengyeah.flowview.func.BaseFuncImpl
import com.pengyeah.flowview.func.IFunc

data class Point(
        var x: Int = 0,
        var y: Int = 0,
        var func: IFunc<Int, Int> = BaseFuncImpl()
)