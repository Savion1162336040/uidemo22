package com.pengyeah.card3d.func

import android.util.Log
import com.pengyeah.flowview.func.BaseFuncImpl

/**
 *  Created by pengyeah on 2020/10/10
 *  佛祖开光，永无bug
 *  God bless U
 */
class CardRotateFunc : BaseFuncImpl {

    constructor()

    override fun execute(inParam: Float): Float {
        if (inParam > inParamMax) {
            Log.e("savion", String.format("计算角度2，出参最小:%s==%s==%s", inParam, inParamMax, inParamMin))
            return outParamMin
        } else if (inParam < inParamMin) {
            Log.e("savion", String.format("计算角度2，出参最大:%s==%s==%s", inParam, inParamMax, inParamMin))
            return outParamMax
        } else {
            //斜率
            val rate = (outParamMin - outParamMax) / (inParamMax - inParamMin)
            val result = outParamMax + inParam * rate;
            Log.e("savion", String.format("计算角度2，出参斜率:%s==%s==%s==%s__%s", inParam, inParamMax, inParamMin, rate, result))
            return result
        }
    }
}