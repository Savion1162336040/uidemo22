package com.pengyeah.flowview.func

/**
 *  Created by pupu on 2020/9/1
 *  佛祖开光，永无bug
 *  God bless U
 */
open class BaseFuncImpl : IFunc {


    constructor(initValue: Float, inParamMax: Float) {
        this.initValue = initValue
        this.inParamMax = inParamMax
    }

    override var initValue: Float
    override var inParamMax: Float

    override fun execute(inParam: Float): Float {
        return 0F
    }

    override fun toString(): String {
        return "BaseFuncImpl(initValue=$initValue, inParamMax=$inParamMax)"
    }


}