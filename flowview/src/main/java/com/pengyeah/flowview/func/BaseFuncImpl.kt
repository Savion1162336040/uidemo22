package com.pengyeah.flowview.func

/**
 *  Created by pupu on 2020/9/1
 *  佛祖开光，永无bug
 *  God bless U
 */
open class BaseFuncImpl : IFunc {

    /**
     * 初始值
     */
    var initValue: Float = 0F

    /**
     * 入参的阈值
     */
    var inParamMax: Float = 0F

    constructor(initValue: Float, inParamMax: Float) {
        this.initValue = initValue
        this.inParamMax = inParamMax
    }

    override fun execute(inParam: Float): Float {
        return 0F
    }

    override fun toString(): String {
        return "BaseFuncImpl(initValue=$initValue, inParamMax=$inParamMax)"
    }


}