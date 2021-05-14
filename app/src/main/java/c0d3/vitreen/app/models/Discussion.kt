package c0d3.vitreen.app.models

import c0d3.vitreen.app.models.dto.DiscussionDTO
import java.io.Serializable

data class Discussion(
    val userId: String = "",
    val ownerId: String = "",
    val productId: String = "",
    val productName: String = "",
    val messages: ArrayList<Message> = ArrayList(),
    val usersIds: ArrayList<String> = arrayListOf(userId, ownerId)
) : Entity(), Serializable {

    fun toDTO(): DiscussionDTO {
        val discussionDTO = DiscussionDTO(productId, productName, messages.last())
        discussionDTO.id = this.id

        return discussionDTO
    }

}
