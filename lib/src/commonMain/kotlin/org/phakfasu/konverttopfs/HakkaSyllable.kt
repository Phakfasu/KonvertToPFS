package org.phakfasu.konverttopfs

data class HakkaSyllable(
    val initial: String, // internal representation: KPPY initial (but unified j/q/x to z/c/s)
    val rhyme: String,   // internal representation: KPPY rhyme (e.g. ii, a, io, ng, m, ab)
    val tone: Int        // 1=陰平, 2=陽平, 3=上聲, 4=去聲, 5=陰入, 6=陽入 — see SiyenTones
)
