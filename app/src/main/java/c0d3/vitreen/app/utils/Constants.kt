package c0d3.vitreen.app.utils

class Constants {
    companion object {
        const val VTAG: String = "VITREEN_DEBUG"

        const val KEY_CATEGORY = "category"
        const val KEY_TITLE = "title"
        const val KEY_PRICE = "price"
        const val KEY_LOCATION = "location"
        const val KEY_DESCRIPTION = "description"
        const val KEY_EMAIL = "email"
        const val KEY_DISCUSSION_ID = "discussionId"

        const val GALLERY_REQUEST = 12254
        const val LOCALISATION_REQUEST = 15266

        const val IMAGES_LIMIT_USER = 3
        const val IMAGES_LIMIT_PROFESSIONAL = 5
        const val IMAGE_SIZE = 5242880L // = 1024 * 1024 * 5
        const val DOCUMENTS_LIMIT = 25L
        const val REPORT_THRESHOLD = 50

        const val USERS_COLLECTION = "users"
        const val PRODUCTS_COLLECTION = "products"
        const val LOCATIONS_COLLECTION = "locations"
        const val CATEGORIES_COLLECTION = "categories"
        const val DISCUSSIONS_COLLECTION = "discussions"
    }
}