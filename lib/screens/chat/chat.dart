import 'package:auto_route/auto_route.dart';
import 'package:dio/dio.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:island/models/chat.dart';
import 'package:island/models/file.dart';
import 'package:island/pods/config.dart';
import 'package:island/pods/network.dart';
import 'package:island/route.gr.dart';
import 'package:island/services/file.dart';
import 'package:island/widgets/alert.dart';
import 'package:island/widgets/app_scaffold.dart';
import 'package:island/widgets/content/cloud_files.dart';
import 'package:material_symbols_icons/symbols.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:styled_widget/styled_widget.dart';

part 'chat.g.dart';

@riverpod
Future<List<SnChat>> chatroomsJoined(Ref ref) async {
  final client = ref.watch(apiClientProvider);
  final resp = await client.get('/chat');
  return resp.data.map((e) => SnChat.fromJson(e)).cast<SnChat>().toList();
}

@RoutePage()
class ChatListScreen extends HookConsumerWidget {
  const ChatListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final chats = ref.watch(chatroomsJoinedProvider);

    return AppScaffold(
      appBar: AppBar(title: Text('chat').tr()),
      floatingActionButton: FloatingActionButton(
        key: Key("chat-page-fab"),
        onPressed: () {
          context.pushRoute(NewChatRoute());
        },
        child: const Icon(Symbols.add),
      ),
      body: chats.when(
        data:
            (items) => RefreshIndicator(
              onRefresh:
                  () => Future.sync(() {
                    ref.invalidate(chatroomsJoinedProvider);
                  }),
              child: ListView.builder(
                padding: EdgeInsets.zero,
                itemCount: items.length,
                itemBuilder: (context, index) {
                  final item = items[index];
                  return ListTile(
                    leading:
                        item.picture == null
                            ? CircleAvatar(
                              child: Text(item.name[0].toUpperCase()),
                            )
                            : ProfilePictureWidget(fileId: item.pictureId),
                    title: Text(item.name),
                    subtitle: Text(item.description),
                    onTap: () {
                      context.pushRoute(ChatRoomRoute(id: item.id));
                    },
                  );
                },
              ),
            ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stack) => Center(child: Text('Error: $error')),
      ),
    );
  }
}

@riverpod
Future<SnChat?> chatroom(Ref ref, int? identifier) async {
  if (identifier == null) return null;
  final client = ref.watch(apiClientProvider);
  final resp = await client.get('/chat/$identifier');
  return SnChat.fromJson(resp.data);
}

@RoutePage()
class NewChatScreen extends StatelessWidget {
  const NewChatScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return EditChatScreen();
  }
}

@RoutePage()
class EditChatScreen extends HookConsumerWidget {
  final int? id;
  const EditChatScreen({super.key, @PathParam("id") this.id});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final formKey = useMemoized(() => GlobalKey<FormState>(), []);

    final submitting = useState(false);

    final nameController = useTextEditingController();
    final descriptionController = useTextEditingController();
    final picture = useState<SnCloudFile?>(null);
    final background = useState<SnCloudFile?>(null);

    final chat = ref.watch(chatroomProvider(id));

    useEffect(() {
      if (chat.value != null) {
        nameController.text = chat.value!.name;
        descriptionController.text = chat.value!.description;
        picture.value = chat.value!.picture;
        background.value = chat.value!.background;
      }
      return;
    }, [chat]);

    void setPicture(String position) async {
      final result = await ref
          .read(imagePickerProvider)
          .pickImage(source: ImageSource.gallery);
      if (result == null) return;

      submitting.value = true;
      try {
        final baseUrl = ref.watch(serverUrlProvider);
        final atk = await getFreshAtk(
          ref.watch(tokenPairProvider),
          baseUrl,
          onRefreshed: (atk, rtk) {
            setTokenPair(ref.watch(sharedPreferencesProvider), atk, rtk);
            ref.invalidate(tokenPairProvider);
          },
        );
        if (atk == null) throw ArgumentError('Access token is null');
        final cloudFile =
            await putMediaToCloud(
              fileData: result,
              atk: atk,
              baseUrl: baseUrl,
              filename: result.name,
              mimetype: result.mimeType ?? 'image/jpeg',
            ).future;
        if (cloudFile == null) {
          throw ArgumentError('Failed to upload the file...');
        }
        switch (position) {
          case 'picture':
            picture.value = cloudFile;
          case 'background':
            background.value = cloudFile;
        }
      } catch (err) {
        showErrorAlert(err);
      } finally {
        submitting.value = false;
      }
    }

    Future<void> performAction() async {
      if (!formKey.currentState!.validate()) return;

      submitting.value = true;
      try {
        final client = ref.watch(apiClientProvider);
        final resp = await client.request(
          id == null ? '/chat' : '/chat/$id',
          data: {
            'name': nameController.text,
            'description': descriptionController.text,
            'background_id': background.value?.id,
            'picture_id': picture.value?.id,
          },
          options: Options(method: id == null ? 'POST' : 'PATCH'),
        );
        if (context.mounted) {
          context.maybePop(SnChat.fromJson(resp.data));
        }
      } catch (err) {
        showErrorAlert(err);
      } finally {
        submitting.value = false;
      }
    }

    return AppScaffold(
      appBar: AppBar(
        title: Text(id == null ? 'createChatRoom' : 'editChatRoom').tr(),
        leading: const PageBackButton(),
      ),
      body: Column(
        children: [
          AspectRatio(
            aspectRatio: 16 / 7,
            child: Stack(
              clipBehavior: Clip.none,
              fit: StackFit.expand,
              children: [
                GestureDetector(
                  child: Container(
                    color: Theme.of(context).colorScheme.surfaceContainerHigh,
                    child:
                        background.value != null
                            ? CloudFileWidget(
                              item: background.value!,
                              fit: BoxFit.cover,
                            )
                            : const SizedBox.shrink(),
                  ),
                  onTap: () {
                    setPicture('background');
                  },
                ),
                Positioned(
                  left: 20,
                  bottom: -32,
                  child: GestureDetector(
                    child: ProfilePictureWidget(
                      fileId: picture.value?.id,
                      radius: 40,
                      fallbackIcon: Symbols.group,
                    ),
                    onTap: () {
                      setPicture('picture');
                    },
                  ),
                ),
              ],
            ),
          ).padding(bottom: 32),
          Form(
            key: formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                TextFormField(
                  controller: nameController,
                  decoration: const InputDecoration(labelText: 'Name'),
                  onTapOutside:
                      (_) => FocusManager.instance.primaryFocus?.unfocus(),
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: descriptionController,
                  decoration: const InputDecoration(labelText: 'Description'),
                  minLines: 3,
                  maxLines: null,
                  onTapOutside:
                      (_) => FocusManager.instance.primaryFocus?.unfocus(),
                ),
                const SizedBox(height: 16),
                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton.icon(
                    onPressed: submitting.value ? null : performAction,
                    label: const Text('Save'),
                    icon: const Icon(Symbols.save),
                  ),
                ),
              ],
            ).padding(all: 24),
          ),
        ],
      ),
    );
  }
}
