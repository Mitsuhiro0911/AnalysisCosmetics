package application

import org.dom4j.DocumentException
import org.dom4j.Node
import java.io.IOException

@Throws(DocumentException::class, IOException::class)
fun main(args: Array<String>) {
    val cal = Calculator()
    val write = Write()
    // コーパスの商品数を計算
    val productNum = cal.calProductNum()

    // TODO:Parser.ktを作成し、メソッドとして切り出す
    // 全成分情報を抽出
    val allComponents: List<Node> = Constants.cosmeProductCorpas.selectNodes("//component")
    val componentList: MutableList<String> = mutableListOf()
    for (component in allComponents) {
        componentList.add(component.text)
    }

    // 抽出した全成分情報に同義語統一処理を行う
    val unifiedList: MutableList<String> = PreProcessing().unitySynonym(componentList)
    // 成分を含有する商品数の尺度でIDF値を計算
    val idfMap: LinkedHashMap<String, Double> = cal.calIDF(productNum, unifiedList)
    // 商品ごとの素性ベクトルを計算
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
