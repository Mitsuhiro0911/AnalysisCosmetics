package application

import net.sf.javaml.core.DenseInstance
import net.sf.javaml.distance.CosineSimilarity
import org.dom4j.Document
import org.dom4j.Node
import org.dom4j.io.SAXReader
import kotlin.math.log10

class Calculator{
    private val cs = CosineSimilarity()

    /**
     * cosme_product.xmlに登録されている商品数を計算する
     *
     * @return cosme_product.xmlに登録されている商品
     */
    fun calProductNum(): Int{
        val productCount: List<Node> = Constants.cosmeProductCorpas.selectNodes("//product")
        return productCount.count()
    }

    /**
     * ある成分を含んでいる商品数で算出したIDF値のマップを返す
     *
     * @param[productNum] 全商品数
     * @param[unifiedList] 洗顔料成分一覧(重複排除後)
     * @return ある成分を含んでいる商品数で算出したIDF値のマップ
     */
    fun calIDF(productNum: Int, unifiedList: MutableList<String>): LinkedHashMap<String, Double>{
        val idfMap: LinkedHashMap<String, Double> = linkedMapOf()
        // 重複数をカウントしてマップに情報を格納
        for (i in 0 until unifiedList.size) {
            val idf: Double = log10(productNum / unifiedList.count { it == unifiedList.get(i) }.toDouble())
            idfMap.set(unifiedList.get(i), idf)
        }
        return idfMap
    }

    /**
     * 2つの商品[x],[y]間のコサイン類似度を計算する
     *
     * @param[x],[y] 商品の素性ベクトル
     * @return 商品同士のコサイン類似度
     */
    fun calCosSimilarity(x: DoubleArray, y: DoubleArray): Double{
        return this.cs.measure(DenseInstance(x), DenseInstance(y))
    }

    /**
     * 商品の素性ベクトルを計算する
     *
     * @param[productNum] 全商品数
     * @param[unifiedList] 洗顔料成分一覧(重複排除後)
     * @param[idfMap] ある成分を含んでいる商品数で算出したIDF値のマップ
     * @return 商品の素性ベクトル
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