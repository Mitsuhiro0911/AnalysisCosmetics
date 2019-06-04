package application

import org.dom4j.Node
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

class Write {

    /**
     * 受け取った変数をログファイルに出力する
     *
     * @param ログ出力の対象
     */
    fun writeLog(componentList: MutableList<String>, unifiedList: MutableList<String>, idfMap: LinkedHashMap<String, Double>, productMapList: MutableList<LinkedHashMap<String, Double>>){
        // componentListをログ出力
        val listHeading = "----cosme_product.xmlの成分を連結したリスト(重複排除前)----"
        writeListLog(componentList, "data/log/1_word_list/word_list1.txt" , listHeading)

        // unifiedListをログ出力
        val unifiedListHeading = "----cosme_product.xmlの成分一覧(重複排除後)----"
        writeListLog(unifiedList, "data/log/2_unified_word_list/unified_word_list1.txt", unifiedListHeading)

        // idfMapをログ出力
        val idfMapHeading = "----IDF(log 全商品数➗その成分が含まれる商品数)----"
        writeMapLog(idfMap, "data/log/3_idf/idf1.txt", idfMapHeading)

        // 素性ベクトルをログに出力
        val productMapListHeading = "----各商品の素性ベクトル----"
        writeVectorLog(productMapList, "data/log/4_feature_vector/feature_vector1.txt", productMapListHeading)
    }

    /**
     * 受け取ったリストをログファイルに出力する
     *
     * @param [list] ログに出力するリスト
     * @param [path] ログファイルのパス・名前
     * @param [heading] ログファイル1行目の見出し
     */
    fun writeListLog(list:MutableList<String>, path: String, heading: String){
        renameLog(path)
        val fw = FileWriter(path)
        val pw = PrintWriter(BufferedWriter(fw))
        pw.println(heading)
        for (i in 0 until list.size) {
            pw.println(list.get(i))
        }
        pw.close()
    }

    /**
     * 受け取ったマップをログファイルに出力する
     *
     * @param [map] ログに出力するマップ
     * @param [path] ログファイルのパス・名前
     * @param [heading] ログファイル1行目の見出し
     */
    fun writeMapLog(map: LinkedHashMap<String, Double>, path: String, heading: String){
        renameLog(path)
        val fw = FileWriter(path)
        val pw = PrintWriter(BufferedWriter(fw))
        pw.println(heading)
        for(m in map) {
            pw.println(m)
        }
        pw.close()
    }

    /**
     * IDFによる重み付けをした各商品の素性ベクトルをログファイルに出力する
     *
     * @param [productMapList] 各商品の素性ベクトル
     * @param [path] ログファイルのパス・名前
     * @param [heading] ログファイル1行目の見出し
     */
    fun writeVectorLog(productMapList: MutableList<LinkedHashMap<String, Double>>, path: String, heading: String): Unit {
        renameLog(path)
        val productNameList: List<Node> = Constants.cosmeProductCorpas.selectNodes("//product//name")
        val fw = FileWriter(path)
        val pw = PrintWriter(BufferedWriter(fw))
        pw.println(heading)
        for (i in 0 until productMapList.size) {
            pw.print("${productNameList.get(i).text}")
            pw.println(productMapList.get(i))
            pw.println()
        }
        pw.close()
    }

//    /**
//     * IDFによる重み付けをした各商品の素性ベクトルをクラスタリングで利用するためのデータファイルに出力する
//     *
//     * @param [productMapList]
//     * 			各商品の素性ベクトル
//     */
//    fun writeVector(productMapList: MutableList<LinkedHashMap<String, Double>>, document: Document): Unit {
//        //val idList: List<Node> = document.selectNodes("//product/@id")
//        val productNameList: List<Node> = document.selectNodes("//product//name")
//        val fw = FileWriter("data/log/feature_vector.data")
//        val pw = PrintWriter(BufferedWriter(fw))
//        for (i in 0 until productMapList.size) {
//            // 先頭の「[」と末尾の「]」を除き、空白を削除する加工を行う
//            val processedStr = "${productMapList.get(i).values}".substring(
//                1, "${productMapList.get(i).values}".length - 1).replace(" ","")
//
//            pw.println("${processedStr},${productNameList.get(i).text}")
//        }
//        pw.close()
//    }

    /**
     * 各商品同士のコサイン類似度をMDS計算用のCSVファイルに出力する
     *
     * @param [cosArray] 各商品同士のコサイン類似度
     */
    fun writeCosineSimilarity(cosArray: Array<Array<Int?>>): Unit{
        val productNameList: List<Node> = Constants.cosmeProductCorpas.selectNodes("//product//name")
        val fw = FileWriter("data/output/cosine_similarity.csv")
        val pw = PrintWriter(BufferedWriter(fw))
        for (i in 0 until cosArray.size) {
            pw.print("${productNameList.get(i).text}")
            for (j in 0 until cosArray.size) {
                pw.print(",${cosArray[i][j]}")
            }
            pw.println()
        }
        pw.close()
    }

    /**
     * LOG＿NUM回分の実行結果をログとして残すため、過去のログファイルをリネームする
     *
     * @param [path] ログファイルのパス
     */
    fun renameLog(path: String){
        val logList: MutableList<File> = mutableListOf()
        for(i in 1 until Constants.LOG_NUM + 1) {
            var sb = StringBuilder(path)
            // .txtの前のインデックスを1つ進める
            sb = sb.deleteCharAt(sb.length - 5)
            sb = sb.insert(sb.length - 4, i.toString())

            logList.add(File(sb.toString()))
        }
        for(i in 0 until Constants.LOG_NUM - 1){
            logList.get(Constants.LOG_NUM - i - 2).renameTo(logList.get(Constants.LOG_NUM - i - 1))
        }
    }
}