package com.jingtian.lchat.util

import java.util.Base64

class Translator {
    companion object {
        const val CAT = 1
        const val DOG = 2
        const val BASE64 = 3
        const val PLAIN = 4
        var suffix = lazy {
            arrayOf("!", ".", "?", "~")
        }
        var cat_charset = lazy {
            arrayOf("喵", "meow", "cat", "猫")
        }
        var dog_charset = lazy {
            arrayOf("汪", "woof", "dog", "狗")
        }
        private fun <T> buildMap(charset: Array<T>):Map<T, Int> {
            val map = mutableMapOf<T, Int>()
            charset.forEachIndexed { index, s ->
                map.put(s, index)
            }
            return map
        }
        var dog_map = lazy {
            buildMap(dog_charset.value)
        }
        var cat_map = lazy {
            buildMap(cat_charset.value)
        }
        val suffix_map = lazy {
            buildMap(suffix.value)
        }

        private fun encrypt(charset:Array<String>, text:String):String {
            val encryptedStr = StringBuilder()
            for (ch in text) {
                var n = ch.toInt()
                println(n);
                for(i in 0..3) {
                    var x = n and 0x3
                    var y = (n shr 2) and 0x3
                    encryptedStr.append(charset[x])
                    encryptedStr.append(suffix.value[y])
                    n = n shr 4
                    println(x + y *4)

                }
            }
            return encryptedStr.toString()
        }
        private fun decrypt(charmap:Map<String, Int>, text:String):String {
            val decryptedStr = StringBuilder()
            val len = text.length
            var index = 0
            var next = mutableMapOf<String, Int>()
            var min = Pair("", len)
            var count = 0
            var decryptChar:Char = 0.toChar()
            while (index < len) {
                for(it in charmap) {
                    val firstIndex = text.indexOf(it.key, index)
                    if((firstIndex >= 0) and (firstIndex < min.second)) {
                        min = Pair(it.key, firstIndex)
                    }
                    next[it.key] = firstIndex
                }
                val suffxindex = text[min.second+min.first.length].toString()
                val ch =
                    ((charmap[min.first] ?: 0) + 4 * (suffix_map.value[suffxindex] ?: 0))
                decryptChar += ch shl (count * 4)
//                println(min.first + ", " + charmap[min.first])
//                println(suffxindex + ", " + suffix_map.value[suffxindex])
//                println(ch shl (count * 4))
                count++
                if(count == 4) {
                    decryptedStr.append(decryptChar)
                    decryptChar = 0.toChar()
                    count = 0
                }
                index += min.first.length + 1
                min = Pair("", len)
            }
            return decryptedStr.toString()
        }
        fun toDog(str:String):String {
            return encrypt(dog_charset.value, str)
        }

        fun toCat(str:String):String {
            return encrypt(cat_charset.value, str)
        }

        fun toBase64(str:String):String {
            var ret = ""
            ret = Base64.getEncoder().encodeToString(str.toByteArray())
            return ret
        }

        fun fromDog(str:String):String {
            return decrypt(dog_map.value, str)
        }

        fun fromCat(str:String):String {
            return decrypt(cat_map.value, str)
        }

        fun fromBase64(str:String):String {
            var ret = ""
            ret = Base64.getDecoder().decode(str).decodeToString()
            return ret
        }
    }
}