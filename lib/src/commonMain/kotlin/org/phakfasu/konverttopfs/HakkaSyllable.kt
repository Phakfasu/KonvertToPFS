package org.phakfasu.konverttopfs

data class HakkaSyllable(
    val initial: String, // internal representation: KPPY initial (but unified j/q/x to z/c/s)
    val rhyme: String,   // internal representation: KPPY rhyme (e.g. ii, a, io, ng, m, ab)
    val tone: Int        // 1 to 6 (1=Yinping, 2=Yangping, 3=Shang, 4=Qu, 5=Yinru, 6=Yangru)
)
