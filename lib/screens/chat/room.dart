import 'package:auto_route/auto_route.dart';
import 'package:collection/collection.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:gap/gap.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:island/database/message.dart';
import 'package:island/database/message_repository.dart';
import 'package:island/models/chat.dart';
import 'package:island/models/file.dart';
import 'package:island/pods/message.dart';
import 'package:island/pods/network.dart';
import 'package:island/pods/websocket.dart';
import 'package:island/route.gr.dart';
import 'package:island/widgets/alert.dart';
import 'package:island/widgets/content/cloud_files.dart';
import 'package:material_symbols_icons/material_symbols_icons.dart';
import 'package:styled_widget/styled_widget.dart';
import 'package:uuid/uuid.dart';
import 'chat.dart';

final messageRepositoryProvider = FutureProvider.family<MessageRepository, int>(
  (ref, roomId) async {
    final room = await ref.watch(chatroomProvider(roomId).future);
    final identity = await ref.watch(chatroomIdentityProvider(roomId).future);
    final apiClient = ref.watch(apiClientProvider);
    final database = ref.watch(databaseProvider);
    return MessageRepository(room!, identity!, apiClient, database);
  },
);

// Provider for messages with pagination
final messagesProvider = StateNotifierProvider.family<
  MessagesNotifier,
  AsyncValue<List<LocalChatMessage>>,
  int
>((ref, roomId) => MessagesNotifier(ref, roomId));

class MessagesNotifier
    extends StateNotifier<AsyncValue<List<LocalChatMessage>>> {
  final Ref _ref;
  final int _roomId;
  int _currentPage = 0;
  static const int _pageSize = 20;
  bool _hasMore = true;

  MessagesNotifier(this._ref, this._roomId)
    : super(const AsyncValue.loading()) {
    loadInitial();
  }

  Future<void> loadInitial() async {
    try {
      final repository = await _ref.read(
        messageRepositoryProvider(_roomId).future,
      );
      final synced = await repository.syncMessages();
      final messages = await repository.listMessages(
        offset: 0,
        take: _pageSize,
        synced: synced,
      );
      state = AsyncValue.data(messages);
      _currentPage = 0;
      _hasMore = messages.length == _pageSize;
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> loadMore() async {
    if (!_hasMore || state is AsyncLoading) return;

    try {
      final currentMessages = state.value ?? [];
      _currentPage++;
      final repository = await _ref.read(
        messageRepositoryProvider(_roomId).future,
      );
      final newMessages = await repository.listMessages(
        offset: _currentPage * _pageSize,
        take: _pageSize,
      );

      if (newMessages.isEmpty || newMessages.length < _pageSize) {
        _hasMore = false;
      }

      state = AsyncValue.data([...currentMessages, ...newMessages]);
    } catch (err) {
      showErrorAlert(err);
      _currentPage--;
    }
  }

  Future<void> sendMessage(String content) async {
    try {
      final repository = await _ref.read(
        messageRepositoryProvider(_roomId).future,
      );

      final nonce = const Uuid().v4();

      final messageTask = repository.sendMessage(_roomId, content, nonce);
      final pendingMessage = repository.pendingMessages.values.firstWhereOrNull(
        (m) => m.roomId == _roomId && m.nonce == nonce,
      );
      if (pendingMessage != null) {
        final currentMessages = state.value ?? [];
        state = AsyncValue.data([pendingMessage, ...currentMessages]);
      }

      final message = await messageTask;

      final updatedMessages = state.value ?? [];
      if (pendingMessage != null) {
        final index = updatedMessages.indexWhere(
          (m) => m.id == pendingMessage.id,
        );
        if (index >= 0) {
          final newList = [...updatedMessages];
          newList[index] = message;
          state = AsyncValue.data(newList);
        }
      } else {
        state = AsyncValue.data([message, ...updatedMessages]);
      }
    } catch (err) {
      showErrorAlert(err);
    }
  }

  Future<void> retryMessage(String pendingMessageId) async {
    try {
      final repository = await _ref.read(
        messageRepositoryProvider(_roomId).future,
      );
      final updatedMessage = await repository.retryMessage(pendingMessageId);

      // Update the message in the list
      final currentMessages = state.value ?? [];
      final index = currentMessages.indexWhere((m) => m.id == pendingMessageId);
      if (index >= 0) {
        final newList = [...currentMessages];
        newList[index] = updatedMessage;
        state = AsyncValue.data(newList);
      }
    } catch (err) {
      showErrorAlert(err);
    }
  }

  Future<void> receiveMessage(SnChatMessage remoteMessage) async {
    try {
      final repository = await _ref.read(
        messageRepositoryProvider(_roomId).future,
      );

      // Skip if this message is not for this room
      if (remoteMessage.chatRoomId != _roomId) return;

      final localMessage = await repository.receiveMessage(remoteMessage);

      // Add the new message to the state
      final currentMessages = state.value ?? [];

      // Check if the message already exists (by id or nonce)
      final existingIndex = currentMessages.indexWhere(
        (m) =>
            m.id == localMessage.id ||
            (localMessage.nonce != null && m.nonce == localMessage.nonce),
      );

      if (existingIndex >= 0) {
        // Replace existing message
        final newList = [...currentMessages];
        newList[existingIndex] = localMessage;
        state = AsyncValue.data(newList);
      } else {
        // Add new message at the beginning (newest first)
        state = AsyncValue.data([localMessage, ...currentMessages]);
      }
    } catch (err) {
      showErrorAlert(err);
    }
  }

  Future<void> receiveMessageUpdate(SnChatMessage remoteMessage) async {
    try {
      final repository = await _ref.read(
        messageRepositoryProvider(_roomId).future,
      );

      // Skip if this message is not for this room
      if (remoteMessage.chatRoomId != _roomId) return;

      final updatedMessage = await repository.receiveMessageUpdate(
        remoteMessage,
      );

      // Update the message in the list
      final currentMessages = state.value ?? [];
      final index = currentMessages.indexWhere(
        (m) => m.id == updatedMessage.id,
      );

      if (index >= 0) {
        final newList = [...currentMessages];
        newList[index] = updatedMessage;
        state = AsyncValue.data(newList);
      }
    } catch (err) {
      showErrorAlert(err);
    }
  }

  Future<void> receiveMessageDeletion(String messageId) async {
    try {
      final repository = await _ref.read(
        messageRepositoryProvider(_roomId).future,
      );

      await repository.receiveMessageDeletion(messageId);

      // Remove the message from the list
      final currentMessages = state.value ?? [];
      final filteredMessages =
          currentMessages.where((m) => m.id != messageId).toList();

      if (filteredMessages.length != currentMessages.length) {
        state = AsyncValue.data(filteredMessages);
      }
    } catch (err) {
      showErrorAlert(err);
    }
  }

  Future<void> updateMessage(
    String messageId,
    String content, {
    List<SnCloudFile>? attachments,
    Map<String, dynamic>? meta,
  }) async {
    try {
      final repository = await _ref.read(
        messageRepositoryProvider(_roomId).future,
      );

      final updatedMessage = await repository.updateMessage(
        messageId,
        content,
        attachments: attachments,
        meta: meta,
      );

      // Update the message in the list
      final currentMessages = state.value ?? [];
      final index = currentMessages.indexWhere((m) => m.id == messageId);

      if (index >= 0) {
        final newList = [...currentMessages];
        newList[index] = updatedMessage;
        state = AsyncValue.data(newList);
      }
    } catch (err) {
      showErrorAlert(err);
    }
  }

  Future<void> deleteMessage(String messageId) async {
    try {
      final repository = await _ref.read(
        messageRepositoryProvider(_roomId).future,
      );

      await repository.deleteMessage(messageId);

      // Remove the message from the list
      final currentMessages = state.value ?? [];
      final filteredMessages =
          currentMessages.where((m) => m.id != messageId).toList();

      if (filteredMessages.length != currentMessages.length) {
        state = AsyncValue.data(filteredMessages);
      }
    } catch (err) {
      showErrorAlert(err);
    }
  }
}

@RoutePage()
class ChatRoomScreen extends HookConsumerWidget {
  final int id;
  const ChatRoomScreen({super.key, @PathParam("id") required this.id});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final chatRoom = ref.watch(chatroomProvider(id));
    final chatIdentity = ref.watch(chatroomIdentityProvider(id));
    final messages = ref.watch(messagesProvider(id));
    final messagesNotifier = ref.read(messagesProvider(id).notifier);
    final ws = ref.watch(websocketProvider);

    final messageController = useTextEditingController();
    final scrollController = useScrollController();

    // Add scroll listener for pagination
    useEffect(() {
      void onScroll() {
        if (scrollController.position.pixels >=
            scrollController.position.maxScrollExtent - 200) {
          messagesNotifier.loadMore();
        }
      }

      scrollController.addListener(onScroll);
      return () => scrollController.removeListener(onScroll);
    }, [scrollController]);

    // Add websocket listener
    // Add websocket listener for new messages
    useEffect(() {
      void onMessage(WebSocketPacket pkt) {
        if (!pkt.type.startsWith('messages')) return;
        final message = SnChatMessage.fromJson(pkt.data!);
        if (message.chatRoomId != chatRoom.value?.id) return;
        switch (pkt.type) {
          case 'messages.new':
            messagesNotifier.receiveMessage(message);
          case 'messages.update':
            messagesNotifier.receiveMessageUpdate(message);
          case 'messages.delete':
            messagesNotifier.receiveMessageDeletion(message.id);
        }
      }

      final subscription = ws.dataStream.listen(onMessage);
      return () => subscription.cancel();
    }, [ws, chatRoom]);

    void sendMessage() {
      if (messageController.text.trim().isNotEmpty) {
        messagesNotifier.sendMessage(messageController.text.trim());
        messageController.clear();
      }
    }

    return Scaffold(
      appBar: AppBar(
        title: chatRoom.when(
          data:
              (room) => Row(
                spacing: 8,
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  SizedBox(
                    height: 26,
                    width: 26,
                    child:
                        room?.pictureId != null
                            ? ProfilePictureWidget(
                              fileId: room?.pictureId,
                              fallbackIcon: Symbols.chat,
                            )
                            : CircleAvatar(
                              child: Text(
                                room?.name[0].toUpperCase() ?? '',
                                style: const TextStyle(fontSize: 12),
                              ),
                            ),
                  ),
                  Text(room?.name ?? 'unknown'.tr()).fontSize(19),
                ],
              ),
          loading: () => const Text('Loading...'),
          error: (_, __) => const Text('Error'),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.more_vert),
            onPressed: () {
              context.router.push(ChatDetailRoute(id: id));
            },
          ),
          const Gap(8),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: messages.when(
              data:
                  (messageList) =>
                      messageList.isEmpty
                          ? Center(child: Text('No messages yet'.tr()))
                          : ListView.builder(
                            padding: EdgeInsets.symmetric(vertical: 16),
                            controller: scrollController,
                            reverse: true, // Show newest messages at the bottom
                            itemCount: messageList.length,
                            itemBuilder: (context, index) {
                              final message = messageList[index];
                              return chatIdentity.when(
                                skipError: true,
                                data:
                                    (identity) => _MessageBubble(
                                      message: message,
                                      isCurrentUser:
                                          identity?.id == message.senderId,
                                    ),
                                loading:
                                    () => _MessageBubble(
                                      message: message,
                                      isCurrentUser: false,
                                    ),
                                error: (_, __) => const SizedBox.shrink(),
                              );
                            },
                          ),
              loading: () => const Center(child: CircularProgressIndicator()),
              error:
                  (error, stack) => Center(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text('Error: $error'),
                        ElevatedButton(
                          onPressed: () => messagesNotifier.loadInitial(),
                          child: Text('Retry'.tr()),
                        ),
                      ],
                    ),
                  ),
            ),
          ),
          chatRoom.when(
            data:
                (room) => _ChatInput(
                  messageController: messageController,
                  chatRoom: room!,
                  onSend: sendMessage,
                ),
            error: (_, __) => const SizedBox.shrink(),
            loading: () => const SizedBox.shrink(),
          ),
        ],
      ),
    );
  }
}

class _ChatInput extends StatelessWidget {
  final TextEditingController messageController;
  final SnChat chatRoom;
  final VoidCallback onSend;

  const _ChatInput({
    required this.messageController,
    required this.chatRoom,
    required this.onSend,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      elevation: 8,
      color: Theme.of(context).colorScheme.surface,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 6, horizontal: 8),
        child: Row(
          children: [
            Expanded(
              child: TextField(
                controller: messageController,
                decoration: InputDecoration(
                  hintText: 'chatMessageHint'.tr(args: [chatRoom.name]),
                  border: InputBorder.none,
                  isDense: true,
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 12,
                    vertical: 4,
                  ),
                ),
                maxLines: null,
                onTapOutside:
                    (_) => FocusManager.instance.primaryFocus?.unfocus(),
                onSubmitted: (_) => onSend(),
              ),
            ),
            IconButton(
              icon: const Icon(Icons.send),
              color: Theme.of(context).colorScheme.primary,
              onPressed: onSend,
            ),
          ],
        ).padding(bottom: MediaQuery.of(context).padding.bottom),
      ),
    );
  }
}

class _MessageBubble extends StatelessWidget {
  final LocalChatMessage message;
  final bool isCurrentUser;

  const _MessageBubble({required this.message, required this.isCurrentUser});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: Row(
        mainAxisAlignment:
            isCurrentUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        children: [
          if (!isCurrentUser)
            ProfilePictureWidget(
              fileId:
                  message.toRemoteMessage().sender.account.profile.pictureId,
              radius: 18,
            ),
          const Gap(8),
          Flexible(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: BoxDecoration(
                color:
                    isCurrentUser
                        ? Theme.of(context).colorScheme.primary.withOpacity(0.8)
                        : Colors.grey.shade200,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    message.toRemoteMessage().content ?? '',
                    style: TextStyle(
                      color: isCurrentUser ? Colors.white : Colors.black,
                    ),
                  ),
                  const Gap(4),
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        DateFormat.Hm().format(message.createdAt.toLocal()),
                        style: TextStyle(
                          fontSize: 10,
                          color:
                              isCurrentUser ? Colors.white70 : Colors.black54,
                        ),
                      ),
                      const Gap(4),
                      if (isCurrentUser)
                        _buildStatusIcon(context, message.status),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const Gap(8),
          if (isCurrentUser)
            ProfilePictureWidget(
              fileId:
                  message.toRemoteMessage().sender.account.profile.pictureId,
              radius: 18,
            ),
        ],
      ),
    );
  }

  Widget _buildStatusIcon(BuildContext context, MessageStatus status) {
    switch (status) {
      case MessageStatus.pending:
        return const Icon(Icons.access_time, size: 12, color: Colors.white70);
      case MessageStatus.sent:
        return const Icon(Icons.check, size: 12, color: Colors.white70);
      case MessageStatus.failed:
        return Consumer(
          builder:
              (context, ref, _) => GestureDetector(
                onTap: () {
                  ref
                      .read(messagesProvider(message.roomId).notifier)
                      .retryMessage(message.id);
                },
                child: const Icon(
                  Icons.error_outline,
                  size: 12,
                  color: Colors.red,
                ),
              ),
        );
    }
  }
}
