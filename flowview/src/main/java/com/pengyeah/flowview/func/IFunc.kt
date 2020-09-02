package com.pengyeah.flowview.func

/**
 *  Created by pupu on 2020/9/1
 *  佛祖开光，永无bug
 *  God bless U
 */
interface IFunc {

    /**
     * 初始值
     */
    var initValue: Float

    /**
     * 入参的阈值
     */
    var inParamMax: Float

    fun execute(inParam: Float): Float
}