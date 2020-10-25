package com.pengyeah.tear.func

import BaseFuncImpl

class CrimpSizeFunc : BaseFuncImpl {
    constructor()

    override fun execute(offset: Float): Float {
        if (offset >= inParamMin && offset <= inParamMax / 4) {
            val rate = (outParamMax - outParamMin) / (inParamMax / 4 - inParamMin)
            return offset * rate
        } else if (offset < inParamMin) {
            return outParamMin
        } else if (offset > inParamMax / 4 && offset <= inParamMax / 2) {
            val rate = (outParamMax - outParamMin) / (inParamMax / 4 - inParamMin)
            return outParamMax - (offset - inParamMax / 4) * rate
        } else if (offset > inParamMax / 2 && offset <= inParamMax * 3 / 4) {
            val rate = (outParamMax - outParamMin) / (inParamMax / 4 - inParamMin)
            return (offset - inParamMax / 2) * rate
        } else {
            return outParamMax
        }
        return super.execute(offset)
    }
}