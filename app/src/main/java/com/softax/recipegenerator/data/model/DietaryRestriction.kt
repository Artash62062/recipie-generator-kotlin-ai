package com.softax.recipegenerator.data.model

enum class DietaryRestriction(val displayName: String) {
    NONE("None"),
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    GLUTEN_FREE("Gluten Free"),
    DAIRY_FREE("Dairy Free"),
    NUT_FREE("Nut Free"),
    KETO("Keto"),
    PALEO("Paleo"),
    HALAL("Halal"),
    KOSHER("Kosher")
}
