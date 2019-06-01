package application

import net.sf.javaml.core.DenseInstance
import net.sf.javaml.distance.CosineSimilarity
import org.dom4j.Document
import org.dom4j.Node
import org.dom4j.io.SAXReader

class Calculator{
    private val cs = CosineSimilarity()

    /**
     * 2つの商品[x],[y]間のコサイン類似度を計算する
     *
     * @param [x],[y]
     * 			商品の素性ベクトル
     *
     * @return
     *          商品同士のコサイン類似度
     */
    fun calCosSimilarity(x: DoubleArray, y: DoubleArray): Double{
        return this.cs.measure(DenseInstance(x), DenseInstance(y))
    }

    /**
     * 商品の素性ベクトルを計算する
     *
     * @param
     *
     * @return
     */
    fun calFeatureVector(productNum: Int, unifiedList: MutableList<String>, idfMap: LinkedHashMap<String, Double>): MutableList<LinkedHashMap<String, Double>>{
        val productMapList = mutableListOf<LinkedHashMap<String, Double>>()
        for (i in 1..productNum) {
            val productInformation: String = "//product[@id=".plus(i).plus("]//component")
            val nodes: List<Node> = Constants.cosmeProductCorpas.selectNodes(productInformation)
            val productElementList = mutableListOf<String>()
            for (node in nodes) {
                productElementList.add(node.text)
            }
            val unifiedProductElementList: MutableList<String> = PreProcessing().unitySynonym(productElementList)
            val productMap: LinkedHashMap<String, Double> = linkedMapOf()
            for (j in 0 until unifiedList.size) {
                if (unifiedProductElementList.contains(unifiedList.get(j))) {
                    productMap.set(unifiedList.get(j), 1.0 * idfMap.getValue(unifiedList.get(j)))
                } else {
                    productMap.set(unifiedList.get(j), 0.0)
                }
            }
            productMapList.add(productMap)
        }
        return productMapList
    }
}