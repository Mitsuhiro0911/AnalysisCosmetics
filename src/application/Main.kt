package application

import org.dom4j.DocumentException
import org.dom4j.Node
import java.io.IOException

@Throws(DocumentException::class, IOException::class)
fun main(args: Array<String>) {
    val cal = Calculator()
    val write = Write()

    val productNum = cal.calProductNum()

    // TODO:Parser.ktを作成し、メソッドとして切り出す
    // 素性ベクトルの型を作成
    val allComponents: List<Node> = Constants.cosmeProductCorpas.selectNodes("//component")
    val componentList: MutableList<String> = mutableListOf()
    for (component in allComponents) {
        componentList.add(component.text)
    }

    // 同義語を統一する
    val unifiedList: MutableList<String> = PreProcessing().unitySynonym(componentList)

    val idfMap: LinkedHashMap<String, Double> = cal.calIDF(productNum, unifiedList)

    val productMapList = cal.calFeatureVector(productNum, unifiedList, idfMap)

    // Java-mlでクラスタリグする際に用いる.dataファイルを出力する
    // write.writeVector(productMapList, document)

    // 二次元配列
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
    write.writeLog(componentList, componentList, idfMap, productMapList)
    write.writeCosineSimilarity(cosArray)
    // 多次元尺度構成法(MDS)により商品同士のコサイン類似度を2次元にプロットするPythonプログラムを実行
    val p = Runtime.getRuntime().exec("python3 /Users/Nakamura/開発/退避用/mds.py ${productNum}" )
}
