package application

import org.dom4j.Node

class PreProcessing{
    /**
     * 同義語辞書を用いて、表記揺れを解消する
     *
     * @param [list] 同義語の統一処理が行われる前のリスト
     * @return [unifiedList] 同義語の統一処理が行われたリスト
     */
    fun unitySynonym(list: MutableList<String>): MutableList<String> {

        val unifiedList: MutableList<String> = mutableListOf()
        for (i in 0..list.size - 1) {
            val test: List<Node> =
                Constants.cosmeComponentDictionary.selectNodes("//component[text()='" + list.get(i) + "']/ancestor-or-self::*/representation//component")
                unifiedList.add(test.get(0).text)
        }
        return unifiedList
    }
}