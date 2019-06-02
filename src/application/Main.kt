package application

import org.dom4j.DocumentException
import org.dom4j.Node
import java.io.IOException
import kotlin.math.log10

@Throws(DocumentException::class, IOException::class)
fun main(args: Array<String>) {
    val cal = Calculator()
    val write = Write()
    val idfMap: LinkedHashMap<String, Double> = linkedMapOf()

    val productCount: List<Node> = Constants.cosmeProductCorpas.selectNodes("//product")
    val productNum = productCount.count()

    //素性ベクトルの型を作成
    val allComponents: List<Node> = Constants.cosmeProductCorpas.selectNodes("//component")
    val componentList: MutableList<String> = mutableListOf()
    for (component in allComponents) {
        componentList.add(component.text)
    }
    // listをログ出力
    val listHeading = "----cosme_product.xmlの成分を連結したリスト(重複排除前)----"
    write.writeListLog(componentList, "data/log/1_word_list/word_list1.txt" , listHeading)

    // 同義語を統一する
    val unifiedList: MutableList<String> = PreProcessing().unitySynonym(componentList)

    // unifiedListをログ出力
    val unifiedListHeading = "----cosme_product.xmlの成分一覧(重複排除後)----"
    write.writeListLog(unifiedList, "data/log/2_unified_word_list/unified_word_list1.txt", unifiedListHeading)

    // 成分一覧から重複を排除し、重複数をカウントしている
    for (i in 0 until unifiedList.size) {
        val idf: Double = log10(productNum / unifiedList.count { it == unifiedList.get(i) }.toDouble())
        idfMap.set(unifiedList.get(i), idf)
    }

    // idfMapをログ出力
    val idfMapHeading = "----IDF(log 全商品数➗その成分が含まれる商品数)----"
    write.writeMapLog(idfMap, "data/log/3_idf/idf1.txt", idfMapHeading)


    val productMapList = cal.calFeatureVector(productNum, unifiedList, idfMap)

    val productMapListHeading = "----各商品の素性ベクトル----"
    write.writeVectorLog(productMapList, "data/log/4_feature_vector/feature_vector1.txt", productMapListHeading)

    // Java-mlでクラスタリグする際に用いる.dataファイルを出力する
    // write.writeVector(productMapList, document)

    //二次元配列
    val cosArray = Array(productMapList.size, { arrayOfNulls<Int>(productMapList.size) })
    for (i in 0 until productMapList.size) {
        val vector1 = productMapList.get(i).values.toDoubleArray()
        for (j in 0 until productMapList.size) {
            val vector2 = productMapList.get(j).values.toDoubleArray()
            if (i == j) {
                cosArray[i][j] = 0
                break
            }
            if (cosArray[i][j] == null) {
                cosArray[i][j] = (cal.calCosSimilarity(vector1, vector2) * Constants.NORM).toInt()
                cosArray[j][i] = (cal.calCosSimilarity(vector1, vector2) * Constants.NORM).toInt()
            }
        }
    }
    write.writeCosineSimilarity(cosArray)
    //多次元尺度構成法(MDS)により商品同士のコサイン類似度を2次元にプロットするPythonプログラムを実行
    val p = Runtime.getRuntime().exec("python3 /Users/Nakamura/開発/退避用/mds.py ${productNum}" )
}
