package c0d3.vitreen.app.utils

class Constants {
    companion object {
        const val TAG: String = "VITREEN_DEBUG"

        const val KEY_CATEGORY = "category"
        const val KEY_TITLE = "title"
        const val KEY_PRICE = "price"
        const val KEY_LOCATION = "location"
        const val KEY_DESCRIPTION = "description"
        const val KEY_EMAIL = "email"

        const val KEY_PRODUCT_ID = "KEY_PRODUCT_ID"
        const val GALLERY_REQUEST: Int = 12254
        const val LOCALISATION_REQUEST: Int = 15266
        const val GALLERY_REQUEST_TAG: String = "Select picture"
        const val IMAGES_LIMIT_USER: Int = 3
        const val IMAGES_LIMIT_PROFESSIONAL: Int = 5
        const val DOCUMENTS_LIMIT = 25L
        const val IMAGE_SIZE = 5242880L // = 1024 * 1024 * 5
        const val USERS_COLLECTION = "users"
        const val PRODUCTS_COLLECTION = "products"
        const val LOCATIONS_COLLECTION = "locations"
        const val CATEGORIES_COLLECTION = "categories"
        const val DISCUSSION_COLLECTION = "discussions"
        const val FAKE_EMAIL = "@temp.vitreen.com"
        const val FAKE_PASSWORD = ":q8T!@&b//n}E%)"
    }
}