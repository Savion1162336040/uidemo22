package com.pengyeah.flowview.func

class Func1<T : Int, R : Int> : BaseFuncImpl<T, R>() {


    override fun execute(vararg params: T): R {
        return params[0] as R
    }
}