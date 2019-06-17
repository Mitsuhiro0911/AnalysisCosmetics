package application

import org.dom4j.DocumentException
import java.io.IOException
import kotlin.math.log10
import kotlin.math.sqrt

@Throws(DocumentException::class, IOException::class)
fun main(args: Array<String>) {
    val cal = Calculator()
    val write = Write()
    // コーパスの商品数を計算
    val productNum = cal.calProductNum()
    // 全成分情報を抽出
    val componentList: MutableList<String> = Parser().extractAllComponent()
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
    // コサイン類似度の逆数を計算した二次元配列
    val reciprocalCosArray = Array(productMapList.size, { arrayOfNulls<Int>(productMapList.size) })
    // 商品の全組合せのコサイン類似度を計算
    for (i in 0 until productMapList.size) {
        val vector1 = productMapList.get(i).values.toDoubleArray()
        for (j in 0 until productMapList.size) {
            val vector2 = productMapList.get(j).values.toDoubleArray()
            if (i == j) {
                cosArray[i][j] = 0
                reciprocalCosArray[i][j] = 0
                break
            }
            if (cosArray[i][j] == null) {
                cosArray[i][j] = (cal.calCosSimilarity(vector1, vector2) * Constants.NORM).toInt()
                cosArray[j][i] = cosArray[i][j]
                reciprocalCosArray[i][j] = ((1.0 - cal.calCosSimilarity(vector1, vector2)) * 100).toInt()
                reciprocalCosArray[j][i] = reciprocalCosArray[i][j]
                println(1.0 - cal.calCosSimilarity(vector1, vector2))
            }
        }
    }

    write.writeLog(componentList, unifiedList, idfMap, productMapList)
    write.writeCosineSimilarity(reciprocalCosArray)
    // 多次元尺度構成法(MDS)により商品同士のコサイン類似度を2次元にプロットするPythonプログラムを実行
    val p = Runtime.getRuntime().exec("python3 /Users/Nakamura/開発/退避用/mds.py ${productNum}" )
}
