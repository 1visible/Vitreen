package c0d3.vitreen.app.models

import c0d3.vitreen.app.models.dto.DiscussionDTO
import java.io.Serializable

data class Discussion(
    val userId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productOwner: String = "",
    val messages: ArrayList<Message> = ArrayList(),
) : Entity(), Serializable {

    fun toDTO(): DiscussionDTO {
        val discussionDTO = DiscussionDTO(productId, productName, messages.last())
        discussionDTO.id = this.id

        return discussionDTO
    }

}
