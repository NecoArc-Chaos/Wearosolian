import 'dart:math' as math;
import 'dart:ui';

import 'package:dismissible_page/dismissible_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_blurhash/flutter_blurhash.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:gap/gap.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:island/models/file.dart';
import 'package:island/pods/config.dart';
import 'package:island/widgets/content/cloud_files.dart';
import 'package:photo_view/photo_view.dart';
import 'package:styled_widget/styled_widget.dart';
import 'package:uuid/uuid.dart';

class CloudFileList extends HookConsumerWidget {
  final List<SnCloudFile> files;
  final double maxHeight;
  final double maxWidth;
  final double? minWidth;
  final bool disableZoomIn;
  const CloudFileList({
    super.key,
    required this.files,
    this.maxHeight = 360,
    this.maxWidth = double.infinity,
    this.minWidth,
    this.disableZoomIn = false,
  });

  double calculateAspectRatio() {
    double total = 0;
    for (var ratio in files.map(
      (e) =>
          e.fileMeta?['ratio'] ??
          ((e.mimeType?.startsWith('image') ?? false) ? 1 : 16 / 9),
    )) {
      total += ratio;
    }
    return total / files.length;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final heroTags = useMemoized(
      () => List.generate(
        files.length,
        (index) => 'cloud-files#${const Uuid().v4()}',
      ),
      [files],
    );

    if (files.isEmpty) return const SizedBox.shrink();
    if (files.length == 1) {
      final isImage = files.first.mimeType?.startsWith('image') ?? false;
      return ConstrainedBox(
        constraints: BoxConstraints(
          maxHeight: maxHeight,
          minWidth: minWidth ?? 0,
        ),
        child: AspectRatio(
          aspectRatio: calculateAspectRatio(),
          child: ClipRRect(
            borderRadius: const BorderRadius.all(Radius.circular(16)),
            child: _CloudFileListEntry(
              file: files.first,
              heroTag: heroTags.first,
              isImage: isImage,
              disableZoomIn: disableZoomIn,
              onTap: () {
                if (!isImage) {
                  return;
                }
                if (!disableZoomIn) {
                  context.pushTransparentRoute(
                    CloudFileZoomIn(item: files.first, heroTag: heroTags.first),
                  );
                }
              },
            ),
          ),
        ),
      ).padding(horizontal: 3);
    }

    final allImages =
        !files.any(
          (e) => e.mimeType == null || !e.mimeType!.startsWith('image'),
        );

    if (allImages) {
      return ConstrainedBox(
        constraints: BoxConstraints(maxHeight: maxHeight, minWidth: maxWidth),
        child: AspectRatio(
          aspectRatio: calculateAspectRatio(),
          child: CarouselView(
            itemExtent: math.min(
              MediaQuery.of(context).size.width * 0.85,
              maxWidth * 0.85,
            ),
            itemSnapping: true,
            shape: RoundedRectangleBorder(
              borderRadius: const BorderRadius.all(Radius.circular(16)),
            ),
            children: [
              for (var i = 0; i < files.length; i++)
                _CloudFileListEntry(
                  file: files[i],
                  heroTag: heroTags[i],
                  isImage: files[i].mimeType?.startsWith('image') ?? false,
                  disableZoomIn: disableZoomIn,
                ),
            ],
            onTap: (i) {
              if (!(files[i].mimeType?.startsWith('image') ?? false)) {
                return;
              }
              if (!disableZoomIn) {
                context.pushTransparentRoute(
                  CloudFileZoomIn(item: files[i], heroTag: heroTags[i]),
                );
              }
            },
          ),
        ),
      );
    }

    return ConstrainedBox(
      constraints: BoxConstraints(maxHeight: maxHeight, minWidth: maxWidth),
      child: AspectRatio(
        aspectRatio: calculateAspectRatio(),
        child: ListView.separated(
          scrollDirection: Axis.horizontal,
          itemCount: files.length,
          padding: EdgeInsets.symmetric(horizontal: 3),
          itemBuilder: (context, index) {
            return ClipRRect(
              borderRadius: const BorderRadius.all(Radius.circular(16)),
              child: _CloudFileListEntry(
                file: files[index],
                heroTag: heroTags[index],
                isImage: files[index].mimeType?.startsWith('image') ?? false,
                disableZoomIn: disableZoomIn,
                onTap: () {
                  if (!(files[index].mimeType?.startsWith('image') ?? false)) {
                    return;
                  }
                  if (!disableZoomIn) {
                    context.pushTransparentRoute(
                      CloudFileZoomIn(
                        item: files[index],
                        heroTag: heroTags[index],
                      ),
                    );
                  }
                },
              ),
            );
          },
          separatorBuilder: (_, _) => const Gap(8),
        ),
      ),
    );
  }
}

class CloudFileZoomIn extends HookConsumerWidget {
  final SnCloudFile item;
  final String heroTag;
  const CloudFileZoomIn({super.key, required this.item, required this.heroTag});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final serverUrl = ref.watch(serverUrlProvider);
    final photoViewController = useMemoized(() => PhotoViewController(), []);

    return DismissiblePage(
      isFullScreen: true,
      backgroundColor: Colors.transparent,
      direction: DismissiblePageDismissDirection.down,
      onDismissed: () {
        Navigator.of(context).pop();
      },
      child: Stack(
        children: [
          Positioned.fill(
            child: PhotoView(
              backgroundDecoration: BoxDecoration(
                color: Colors.black.withOpacity(0.9),
              ),
              controller: photoViewController,
              heroAttributes: PhotoViewHeroAttributes(tag: heroTag),
              imageProvider: CloudImageWidget.provider(
                fileId: item.id,
                serverUrl: serverUrl,
              ),
            ),
          ),
          // Close button
          Positioned(
            top: MediaQuery.of(context).padding.top + 20,
            right: 20,
            child: IconButton(
              icon: Icon(Icons.close, color: Colors.white),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ),
        ],
      ),
    );
  }
}

class _CloudFileListEntry extends StatelessWidget {
  final SnCloudFile file;
  final String heroTag;
  final bool isImage;
  final bool disableZoomIn;
  final VoidCallback? onTap;

  const _CloudFileListEntry({
    required this.file,
    required this.heroTag,
    required this.isImage,
    required this.disableZoomIn,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final content = Stack(
      children: [
        if (isImage)
          Positioned.fill(
            child:
                file.fileMeta?['blur'] != null
                    ? BlurHash(hash: file.fileMeta?['blur'])
                    : ImageFiltered(
                      imageFilter: ImageFilter.blur(sigmaX: 10, sigmaY: 10),
                      child: CloudFileWidget(item: file, noBlurhash: true),
                    ),
          ),
        if (isImage)
          CloudFileWidget(
            item: file,
            heroTag: heroTag,
            noBlurhash: true,
          ).center()
        else
          CloudFileWidget(item: file, heroTag: heroTag),
      ],
    );

    if (onTap != null) {
      return InkWell(
        borderRadius: const BorderRadius.all(Radius.circular(16)),
        onTap: onTap,
        child: content,
      );
    }

    return content;
  }
}
