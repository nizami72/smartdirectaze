package az.nizami.smartdirectaze.ai.internal;

/**
 * Chat memory id of the agent. Carries the shop, so tools take it from here and never from the model.
 *
 * @param conversationId unique per customer conversation, e.g. "wa:{instanceId}:{chatId}"
 */
record ConversationKey(Long shopId, String conversationId) {
}
