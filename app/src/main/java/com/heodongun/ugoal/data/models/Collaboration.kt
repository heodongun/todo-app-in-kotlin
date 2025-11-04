package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String = "",
    val todoId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val text: String = "",
    val timestamp: Long = 0L,
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val mentions: List<String> = emptyList(), // @mentioned user IDs
    val attachments: List<Attachment> = emptyList()
)

@Serializable
data class SharedList(
    val id: String = "",
    val listId: String = "",
    val ownerId: String = "",
    val sharedWith: List<SharedUser> = emptyList(),
    val permissions: SharePermissions = SharePermissions(),
    val sharedAt: Long = 0L,
    val inviteLink: String? = null
)

@Serializable
data class SharedUser(
    val userId: String = "",
    val userName: String = "",
    val email: String = "",
    val acceptedAt: Long? = null,
    val permissions: SharePermissions = SharePermissions()
)

@Serializable
data class SharePermissions(
    val canView: Boolean = true,
    val canEdit: Boolean = false,
    val canDelete: Boolean = false,
    val canShare: Boolean = false,
    val canComment: Boolean = true
)

@Serializable
data class Attachment(
    val id: String = "",
    val name: String = "",
    val type: AttachmentType = AttachmentType.OTHER,
    val url: String = "",
    val size: Long = 0L, // bytes
    val uploadedBy: String = "",
    val uploadedAt: Long = 0L,
    val thumbnailUrl: String? = null
)

@Serializable
enum class AttachmentType {
    IMAGE,
    DOCUMENT,
    AUDIO,
    VIDEO,
    LINK,
    OTHER
}
