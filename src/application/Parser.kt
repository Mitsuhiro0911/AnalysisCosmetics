package application

import org.dom4j.Node

class Parser{

    /**
     * cosme_product.xmlに登録されている全成分情報を抽出する
     *
     * @return cosme_product.xmlに登録されている全成分情報
     */
    fun extractAllComponent(): MutableList<String>{
        val allComponents: List<Node> = Constants.cosmeProductCorpas.selectNodes("//component")
        val componentList: MutableList<String> = mutableListOf()
        for (component in allComponents) {
            componentList.add(component.text)
        }
        return componentList
    }

}