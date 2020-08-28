package com.pengyeah.flowview.func

interface IFunc<T,R> {

    fun execute(vararg params:T):R
}