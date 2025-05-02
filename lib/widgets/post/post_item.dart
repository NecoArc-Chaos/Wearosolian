import 'package:auto_route/auto_route.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_quill/flutter_quill.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:island/models/post.dart';
import 'package:island/pods/network.dart';
import 'package:island/pods/userinfo.dart';
import 'package:island/route.gr.dart';
import 'package:island/widgets/alert.dart';
import 'package:island/widgets/content/cloud_file_collection.dart';
import 'package:island/widgets/content/cloud_files.dart';
import 'package:island/widgets/quill_content.dart';
import 'package:material_symbols_icons/symbols.dart';
import 'package:styled_widget/styled_widget.dart';
import 'package:super_context_menu/super_context_menu.dart';

class PostItem extends HookConsumerWidget {
  final SnPost item;
  final EdgeInsets? padding;
  final bool isOpenable;
  final Function? onRefresh;
  const PostItem({
    super.key,
    required this.item,
    this.padding,
    this.isOpenable = true,
    this.onRefresh,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final renderingPadding =
        padding ?? EdgeInsets.symmetric(horizontal: 12, vertical: 16);

    final user = ref.watch(userInfoProvider);
    final isAuthor = useMemoized(
      () => user.hasValue && user.value!.id == item.publisher.accountId,
      [user],
    );

    return ContextMenuWidget(
      menuProvider: (_) {
        return Menu(
          children: [
            if (isAuthor)
              MenuAction(
                title: 'edit'.tr(),
                image: MenuImage.icon(Symbols.edit),
                callback: () {
                  context.router.push(PostEditRoute(id: item.id)).then((value) {
                    if (value != null) {
                      onRefresh?.call();
                    }
                  });
                },
              ),
            if (isAuthor)
              MenuAction(
                title: 'delete'.tr(),
                image: MenuImage.icon(Symbols.delete),
                callback: () {
                  showConfirmAlert(
                    'deletePostHint'.tr(),
                    'deletePost'.tr(),
                  ).then((confirm) {
                    if (confirm) {
                      final client = ref.watch(apiClientProvider);
                      client
                          .delete('/posts/${item.id}')
                          .catchError((err) {
                            showErrorAlert(err);
                            return err;
                          })
                          .then((_) {
                            onRefresh?.call();
                          });
                    }
                  });
                },
              ),
            if (isAuthor) MenuSeparator(),
            MenuAction(
              title: 'copyLink'.tr(),
              image: MenuImage.icon(Symbols.link),
              callback: () {
                Clipboard.setData(
                  ClipboardData(text: 'https://solsynth.dev/posts/${item.id}'),
                );
              },
            ),
          ],
        );
      },
      child: Material(
        child: Padding(
          padding: renderingPadding,
          child: Column(
            spacing: 8,
            children: [
              Row(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                spacing: 12,
                children: [
                  ProfilePictureWidget(item: item.publisher.picture),
                  Expanded(
                    child: GestureDetector(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(item.publisher.nick).bold(),
                          if (item.content?.isNotEmpty ?? false)
                            QuillContent(
                              document: Document.fromJson(item.content!),
                            ),
                        ],
                      ),
                      onTap: () {
                        if (isOpenable) {
                          context.router.push(PostDetailRoute(id: item.id));
                        }
                      },
                    ),
                  ),
                ],
              ),
              if (item.attachments.isNotEmpty)
                CloudFileList(files: item.attachments),
            ],
          ),
        ),
      ),
    );
  }
}
