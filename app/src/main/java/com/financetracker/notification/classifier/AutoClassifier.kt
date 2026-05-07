package com.financetracker.notification.classifier

data class CategoryRule(val categoryId: Long, val keywords: List<String>)

class AutoClassifier {

    private val rules = listOf(
        CategoryRule(1, listOf("麦当劳", "肯德基", "汉堡王", "必胜客", "星巴克", "瑞幸",
            "喜茶", "奈雪", "蜜雪冰城", "茶颜悦色", "美团", "饿了么", "外卖", "餐厅",
            "饭店", "小吃", "超市", "便利店", "菜场", "水果", "面包", "蛋糕")),
        CategoryRule(2, listOf("淘宝", "京东", "拼多多", "唯品会", "苏宁", "天猫",
            "网易考拉", "当当", "得物", "闲鱼", "商场", "百货", "服装", "鞋")),
        CategoryRule(3, listOf("加油", "中石化", "中石油", "滴滴", "哈啰", "曹操出行",
            "地铁", "公交", "高铁", "火车", "机票", "航班", "停车场", "高速", "ETC")),
        CategoryRule(4, listOf("房租", "水电", "物业", "燃气", "暖气", "链家", "自如")),
        CategoryRule(5, listOf("医院", "药店", "挂号", "体检", "诊所", "医保")),
        CategoryRule(6, listOf("影院", "电影院", "KTV", "游戏", "视频会员", "旅游",
            "景点", "酒店", "网吧", "密室", "剧本杀")),
        CategoryRule(7, listOf("话费", "宽带", "流量", "联通", "移动", "电信", "手机充值")),
    )

    /** Returns the category ID that best matches the merchant name */
    fun classify(merchant: String): Long {
        for (rule in rules) {
            for (keyword in rule.keywords) {
                if (keyword in merchant) {
                    return rule.categoryId
                }
            }
        }
        return 8 // 其他支出
    }
}
