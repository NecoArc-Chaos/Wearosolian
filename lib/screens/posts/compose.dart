import 'package:auto_route/auto_route.dart';
import 'package:collection/collection.dart';
import 'package:dio/dio.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:gap/gap.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:island/models/file.dart';
import 'package:island/models/post.dart';
import 'package:island/pods/config.dart';
import 'package:island/pods/network.dart';
import 'package:island/screens/creators/publishers.dart';
import 'package:island/screens/posts/detail.dart';
import 'package:island/services/file.dart';
import 'package:island/services/responsive.dart';
import 'package:island/widgets/alert.dart';
import 'package:island/widgets/app_scaffold.dart';
import 'package:island/widgets/content/cloud_files.dart';
import 'package:island/widgets/content/attachment_preview.dart';
import 'package:island/widgets/post/publishers_modal.dart';
import 'package:material_symbols_icons/symbols.dart';
import 'package:pasteboard/pasteboard.dart';
import 'package:styled_widget/styled_widget.dart';

@RoutePage()
class PostEditScreen extends HookConsumerWidget {
  final String id;
  const PostEditScreen({super.key, @PathParam('id') required this.id});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final post = ref.watch(postProvider(id));
    return post.when(
      data: (post) => PostComposeScreen(originalPost: post),
      loading:
          () => AppScaffold(
            appBar: AppBar(leading: const PageBackButton()),
            body: const Center(child: CircularProgressIndicator()),
          ),
      error:
          (e, _) => AppScaffold(
            appBar: AppBar(leading: const PageBackButton()),
            body: Text('Error: $e', textAlign: TextAlign.center),
          ),
    );
  }
}

@RoutePage()
class PostComposeScreen extends HookConsumerWidget {
  final SnPost? originalPost;
  final SnPost? repliedPost;
  final SnPost? forwardedPost;
  const PostComposeScreen({
    super.key,
    this.originalPost,
    this.repliedPost,
    this.forwardedPost,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final publishers = ref.watch(publishersManagedProvider);

    final currentPublisher = useState<SnPublisher?>(null);

    useEffect(() {
      if (publishers.value?.isNotEmpty ?? false) {
        currentPublisher.value = publishers.value!.first;
      }
      return null;
    }, [publishers]);

    // Contains the XFile, ByteData, or SnCloudFile
    final attachments = useState<List<UniversalFile>>(
      originalPost?.attachments
              .map(
                (e) => UniversalFile(
                  data: e,
                  type: switch (e.mimeType?.split('/').firstOrNull) {
                    'image' => UniversalFileType.image,
                    'video' => UniversalFileType.video,
                    'audio' => UniversalFileType.audio,
                    _ => UniversalFileType.file,
                  },
                ),
              )
              .toList() ??
          [],
    );
    final titleController = useTextEditingController(text: originalPost?.title);
    final descriptionController = useTextEditingController(
      text: originalPost?.description,
    );
    final contentController = useTextEditingController(
      text:
          originalPost?.content ??
          (forwardedPost != null ? '> ${forwardedPost!.content}\n\n' : null),
    );

    // Add visibility state with default value from original post or 0 (public)
    final visibility = useState<int>(originalPost?.visibility ?? 0);

    final submitting = useState(false);

    Future<void> pickPhotoMedia() async {
      final result = await ref
          .watch(imagePickerProvider)
          .pickMultiImage(requestFullMetadata: true);
      if (result.isEmpty) return;
      attachments.value = [
        ...attachments.value,
        ...result.map(
          (e) => UniversalFile(data: e, type: UniversalFileType.image),
        ),
      ];
    }

    Future<void> pickVideoMedia() async {
      final result = await ref
          .watch(imagePickerProvider)
          .pickVideo(source: ImageSource.gallery);
      if (result == null) return;
      attachments.value = [
        ...attachments.value,
        UniversalFile(data: result, type: UniversalFileType.video),
      ];
    }

    final attachmentProgress = useState<Map<int, double>>({});

    Future<void> uploadAttachment(int index) async {
      final attachment = attachments.value[index];
      if (attachment is SnCloudFile) return;
      final baseUrl = ref.watch(serverUrlProvider);
      final token = await getToken(ref.watch(tokenProvider));
      if (token == null) throw ArgumentError('Token is null');
      try {
        attachmentProgress.value = {...attachmentProgress.value, index: 0};
        final cloudFile =
            await putMediaToCloud(
              fileData: attachment,
              atk: token,
              baseUrl: baseUrl,
              filename: attachment.data.name ?? 'Post media',
              mimetype:
                  attachment.data.mimeType ??
                  switch (attachment.type) {
                    UniversalFileType.image => 'image/unknown',
                    UniversalFileType.video => 'video/unknown',
                    UniversalFileType.audio => 'audio/unknown',
                    UniversalFileType.file => 'application/octet-stream',
                  },
              onProgress: (progress, estimate) {
                attachmentProgress.value = {
                  ...attachmentProgress.value,
                  index: progress,
                };
              },
            ).future;
        if (cloudFile == null) {
          throw ArgumentError('Failed to upload the file...');
        }
        final clone = List.of(attachments.value);
        clone[index] = UniversalFile(data: cloudFile, type: attachment.type);
        attachments.value = clone;
      } catch (err) {
        showErrorAlert(err);
      } finally {
        attachmentProgress.value = attachmentProgress.value..remove(index);
      }
    }

    Future<void> deleteAttachment(int index) async {
      final attachment = attachments.value[index];
      if (attachment.isOnCloud) {
        final client = ref.watch(apiClientProvider);
        await client.delete('/files/${attachment.data.id}');
      }
      final clone = List.of(attachments.value);
      clone.removeAt(index);
      attachments.value = clone;
    }

    Future<void> performAction() async {
      try {
        submitting.value = true;

        await Future.wait(
          attachments.value
              .where((e) => e.isOnDevice)
              .mapIndexed((idx, e) => uploadAttachment(idx)),
        );

        final client = ref.watch(apiClientProvider);
        await client.request(
          originalPost == null ? '/posts' : '/posts/${originalPost!.id}',
          data: {
            'title': titleController.text,
            'description': descriptionController.text,
            'content': contentController.text,
            'visibility':
                visibility.value, // Add visibility field to API request
            'attachments':
                attachments.value
                    .where((e) => e.isOnCloud)
                    .map((e) => e.data.id)
                    .toList(),
            if (repliedPost != null) 'replied_post_id': repliedPost!.id,
            if (forwardedPost != null) 'forwarded_post_id': forwardedPost!.id,
          },
          options: Options(
            headers: {'X-Pub': currentPublisher.value?.name},
            method: originalPost == null ? 'POST' : 'PATCH',
          ),
        );
        if (context.mounted) {
          context.maybePop(true);
        }
      } catch (err) {
        showErrorAlert(err);
      } finally {
        submitting.value = false;
      }
    }

    Future<void> handlePaste() async {
      final clipboard = await Pasteboard.image;
      if (clipboard == null) return;

      attachments.value = [
        ...attachments.value,
        UniversalFile(
          data: XFile.fromData(clipboard, mimeType: "image/jpeg"),
          type: UniversalFileType.image,
        ),
      ];
    }

    void handleKeyPress(RawKeyEvent event) {
      if (event is! RawKeyDownEvent) return;

      final isPaste = event.logicalKey == LogicalKeyboardKey.keyV;
      final isModifierPressed = event.isMetaPressed || event.isControlPressed;

      if (isPaste && isModifierPressed) {
        handlePaste();
      }
    }

    void showVisibilityModal() {
      showDialog(
        context: context,
        builder:
            (context) => AlertDialog(
              title: Text('postVisibility'.tr()),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  ListTile(
                    leading: Icon(Symbols.public),
                    title: Text('postVisibilityPublic'.tr()),
                    onTap: () {
                      visibility.value = 0;
                      Navigator.pop(context);
                    },
                    selected: visibility.value == 0,
                  ),
                  ListTile(
                    leading: Icon(Symbols.group),
                    title: Text('postVisibilityFriends'.tr()),
                    onTap: () {
                      visibility.value = 1;
                      Navigator.pop(context);
                    },
                    selected: visibility.value == 1,
                  ),
                  ListTile(
                    leading: Icon(Symbols.link_off),
                    title: Text('postVisibilityUnlisted'.tr()),
                    onTap: () {
                      visibility.value = 2;
                      Navigator.pop(context);
                    },
                    selected: visibility.value == 2,
                  ),
                  ListTile(
                    leading: Icon(Symbols.lock),
                    title: Text('postVisibilityPrivate'.tr()),
                    onTap: () {
                      visibility.value = 3;
                      Navigator.pop(context);
                    },
                    selected: visibility.value == 3,
                  ),
                ],
              ),
            ),
      );
    }

    // Helper method to get the appropriate icon for each visibility status
    IconData getVisibilityIcon(int visibilityValue) {
      switch (visibilityValue) {
        case 1: // Friends
          return Symbols.group;
        case 2: // Unlisted
          return Symbols.link_off;
        case 3: // Private
          return Symbols.lock;
        default: // Public (0) or unknown
          return Symbols.public;
      }
    }

    // Helper method to get the translation key for each visibility status
    String getVisibilityText(int visibilityValue) {
      switch (visibilityValue) {
        case 1: // Friends
          return 'postVisibilityFriends';
        case 2: // Unlisted
          return 'postVisibilityUnlisted';
        case 3: // Private
          return 'postVisibilityPrivate';
        default: // Public (0) or unknown
          return 'postVisibilityPublic';
      }
    }

    return AppScaffold(
      appBar: AppBar(
        leading: const PageBackButton(),
        title:
            isWideScreen(context)
                ? Text(originalPost != null ? 'editPost'.tr() : 'newPost'.tr())
                : null,
        actions: [
          if (isWideScreen(context))
            Tooltip(
              message: 'keyboard_shortcuts'.tr(),
              child: IconButton(
                icon: const Icon(Symbols.keyboard),
                onPressed: () {
                  showDialog(
                    context: context,
                    builder:
                        (context) => AlertDialog(
                          title: Text('keyboard_shortcuts'.tr()),
                          content: Column(
                            mainAxisSize: MainAxisSize.min,
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Ctrl/Cmd + Enter: ${'submit'.tr()}'),
                              Text('Ctrl/Cmd + V: ${'paste'.tr()}'),
                              Text('Ctrl/Cmd + I: ${'add_image'.tr()}'),
                              Text('Ctrl/Cmd + Shift + V: ${'add_video'.tr()}'),
                            ],
                          ),
                          actions: [
                            TextButton(
                              onPressed: () => Navigator.of(context).pop(),
                              child: Text('close'.tr()),
                            ),
                          ],
                        ),
                  );
                },
              ),
            ),
          IconButton(
            onPressed: submitting.value ? null : performAction,
            icon:
                submitting.value
                    ? SizedBox(
                      width: 28,
                      height: 28,
                      child: const CircularProgressIndicator(
                        color: Colors.white,
                        strokeWidth: 2.5,
                      ),
                    ).center()
                    : originalPost != null
                    ? const Icon(Symbols.edit)
                    : const Icon(Symbols.upload),
          ),
          const Gap(8),
        ],
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (repliedPost != null)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              color: Theme.of(
                context,
              ).colorScheme.surfaceVariant.withOpacity(0.5),
              child: Row(
                children: [
                  const Icon(Symbols.reply, size: 16),
                  const Gap(8),
                  Expanded(
                    child: Text(
                      '${'reply'.tr()}: ${repliedPost!.publisher.nick}',
                      style: Theme.of(context).textTheme.bodySmall,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ),
          if (forwardedPost != null)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              color: Theme.of(
                context,
              ).colorScheme.surfaceVariant.withOpacity(0.5),
              child: Row(
                children: [
                  const Icon(Symbols.forward, size: 16),
                  const Gap(8),
                  Expanded(
                    child: Text(
                      '${'forward'.tr()}: ${forwardedPost!.publisher.nick}',
                      style: Theme.of(context).textTheme.bodySmall,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ),
          Expanded(
            child: Row(
              spacing: 12,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                GestureDetector(
                  child: ProfilePictureWidget(
                    fileId: currentPublisher.value?.picture?.id,
                    radius: 20,
                    fallbackIcon:
                        currentPublisher.value == null
                            ? Symbols.question_mark
                            : null,
                  ),
                  onTap: () {
                    showModalBottomSheet(
                      isScrollControlled: true,
                      context: context,
                      builder: (context) => PublisherModal(),
                    ).then((value) {
                      if (value is SnPublisher) currentPublisher.value = value;
                    });
                  },
                ).padding(top: 16),
                Expanded(
                  child: SingleChildScrollView(
                    padding: EdgeInsets.symmetric(vertical: 16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            OutlinedButton(
                              onPressed: () {
                                showVisibilityModal();
                              },
                              style: OutlinedButton.styleFrom(
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(20),
                                ),
                                side: BorderSide(
                                  color: Theme.of(
                                    context,
                                  ).colorScheme.primary.withOpacity(0.5),
                                ),
                                padding: EdgeInsets.symmetric(horizontal: 16),
                                visualDensity: const VisualDensity(
                                  vertical: -2,
                                  horizontal: -4,
                                ),
                              ),
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Icon(
                                    getVisibilityIcon(visibility.value),
                                    size: 16,
                                    color:
                                        Theme.of(context).colorScheme.primary,
                                  ),
                                  const SizedBox(width: 6),
                                  Text(
                                    getVisibilityText(visibility.value).tr(),
                                    style: TextStyle(
                                      fontSize: 14,
                                      color:
                                          Theme.of(context).colorScheme.primary,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ).padding(bottom: 6),
                        TextField(
                          controller: titleController,
                          decoration: InputDecoration.collapsed(
                            hintText: 'postTitle'.tr(),
                          ),
                          style: TextStyle(fontSize: 16),
                          onTapOutside:
                              (_) =>
                                  FocusManager.instance.primaryFocus?.unfocus(),
                        ),
                        TextField(
                          controller: descriptionController,
                          decoration: InputDecoration.collapsed(
                            hintText: 'postDescription'.tr(),
                          ),
                          style: TextStyle(fontSize: 16),
                          onTapOutside:
                              (_) =>
                                  FocusManager.instance.primaryFocus?.unfocus(),
                        ),
                        const Gap(8),
                        RawKeyboardListener(
                          focusNode: FocusNode(),
                          onKey: handleKeyPress,
                          child: TextField(
                            controller: contentController,
                            style: TextStyle(fontSize: 14),
                            decoration: InputDecoration(
                              border: InputBorder.none,
                              hintText: 'postPlaceholder'.tr(),
                              isDense: true,
                            ),
                            maxLines: null,
                            onTapOutside:
                                (_) =>
                                    FocusManager.instance.primaryFocus
                                        ?.unfocus(),
                          ),
                        ),
                        const Gap(8),
                        LayoutBuilder(
                          builder: (context, constraints) {
                            final isWide = isWideScreen(context);
                            return isWide
                                ? Wrap(
                                  spacing: 8,
                                  runSpacing: 8,
                                  children: [
                                    for (
                                      var idx = 0;
                                      idx < attachments.value.length;
                                      idx++
                                    )
                                      SizedBox(
                                        width: constraints.maxWidth / 2 - 4,
                                        child: AttachmentPreview(
                                          item: attachments.value[idx],
                                          progress:
                                              attachmentProgress.value[idx],
                                          onRequestUpload:
                                              () => uploadAttachment(idx),
                                          onDelete: () => deleteAttachment(idx),
                                          onMove: (delta) {
                                            if (idx + delta < 0 ||
                                                idx + delta >=
                                                    attachments.value.length) {
                                              return;
                                            }
                                            final clone = List.of(
                                              attachments.value,
                                            );
                                            clone.insert(
                                              idx + delta,
                                              clone.removeAt(idx),
                                            );
                                            attachments.value = clone;
                                          },
                                        ),
                                      ),
                                  ],
                                )
                                : Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  spacing: 8,
                                  children: [
                                    for (
                                      var idx = 0;
                                      idx < attachments.value.length;
                                      idx++
                                    )
                                      AttachmentPreview(
                                        item: attachments.value[idx],
                                        progress: attachmentProgress.value[idx],
                                        onRequestUpload:
                                            () => uploadAttachment(idx),
                                        onDelete: () => deleteAttachment(idx),
                                        onMove: (delta) {
                                          if (idx + delta < 0 ||
                                              idx + delta >=
                                                  attachments.value.length) {
                                            return;
                                          }
                                          final clone = List.of(
                                            attachments.value,
                                          );
                                          clone.insert(
                                            idx + delta,
                                            clone.removeAt(idx),
                                          );
                                          attachments.value = clone;
                                        },
                                      ),
                                  ],
                                );
                          },
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ).padding(horizontal: 16),
          ),
          Material(
            elevation: 4,
            child: Row(
              children: [
                IconButton(
                  onPressed: pickPhotoMedia,
                  icon: const Icon(Symbols.add_a_photo),
                  color: Theme.of(context).colorScheme.primary,
                ),
                IconButton(
                  onPressed: pickVideoMedia,
                  icon: const Icon(Symbols.videocam),
                  color: Theme.of(context).colorScheme.primary,
                ),
              ],
            ).padding(
              bottom: MediaQuery.of(context).padding.bottom + 16,
              horizontal: 16,
              top: 8,
            ),
          ),
        ],
      ),
    );
  }
}
