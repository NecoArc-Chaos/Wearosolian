import 'package:auto_route/auto_route.dart';
import 'package:dio/dio.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:gap/gap.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:island/models/sticker.dart';
import 'package:island/pods/network.dart';
import 'package:island/route.gr.dart';
import 'package:island/widgets/alert.dart';
import 'package:island/widgets/app_scaffold.dart';
import 'package:material_symbols_icons/symbols.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:styled_widget/styled_widget.dart';
import 'package:very_good_infinite_list/very_good_infinite_list.dart';

part 'stickers.g.dart';

@RoutePage()
class StickersScreen extends HookConsumerWidget {
  final String pubName;
  const StickersScreen({super.key, @PathParam("name") required this.pubName});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stickersState = ref.watch(stickerPacksProvider);
    final stickersNotifier = ref.watch(stickerPacksProvider.notifier);

    return AppScaffold(
      appBar: AppBar(
        title: const Text('stickers').tr(),
        actions: [
          IconButton(
            onPressed: () {
              context.router.push(NewStickerPacksRoute(pubName: pubName)).then((
                value,
              ) {
                if (value != null) {
                  stickersNotifier.refresh();
                }
              });
            },
            icon: const Icon(Symbols.add_circle),
          ),
          const Gap(8),
        ],
      ),
      body: stickersState.when(
        data:
            (stickers) => RefreshIndicator(
              onRefresh: stickersNotifier.refresh,
              child: InfiniteList(
                padding: EdgeInsets.zero,
                itemCount: stickers.length,
                hasReachedMax: stickersNotifier.isReachedMax,
                isLoading: stickersNotifier.isLoading,
                onFetchData: stickersNotifier.fetchMore,
                itemBuilder: (context, index) {
                  return ListTile(
                    title: Text(stickers[index].name),
                    subtitle: Text(stickers[index].description),
                    trailing: const Icon(Symbols.chevron_right),
                    onTap: () {
                      context.router.push(
                        StickerPackDetailRoute(
                          pubName: pubName,
                          id: stickers[index].id,
                        ),
                      );
                    },
                  );
                },
              ),
            ),
        loading: () => const CircularProgressIndicator(),
        error: (error, stack) => Text('Error: $error'),
      ),
    );
  }
}

final stickerPacksProvider = StateNotifierProvider<
  StickerPacksNotifier,
  AsyncValue<List<SnStickerPack>>
>((ref) {
  return StickerPacksNotifier(ref.watch(apiClientProvider));
});

class StickerPacksNotifier
    extends StateNotifier<AsyncValue<List<SnStickerPack>>> {
  final Dio _apiClient;
  StickerPacksNotifier(this._apiClient) : super(const AsyncValue.loading()) {
    fetchStickers();
  }

  int offset = 0;
  int take = 20;
  int total = 0;

  bool isLoading = false;
  bool get isReachedMax =>
      state.valueOrNull != null && state.valueOrNull!.length >= total;

  Future<void> fetchStickers() async {
    if (isLoading) return;
    isLoading = true;

    try {
      final response = await _apiClient.get(
        '/stickers?offset=$offset&take=$take',
      );
      if (response.statusCode == 200) {
        total = int.parse(response.headers.value('X-Total') ?? '0');
        final newStickers =
            response.data
                .map((e) => SnStickerPack.fromJson(e))
                .cast<SnStickerPack>()
                .toList();

        state = AsyncValue.data(
          state.valueOrNull != null
              ? [...state.value!, ...newStickers]
              : newStickers,
        );
        offset += take;
      } else {
        state = AsyncValue.error('Failed to load stickers', StackTrace.current);
      }
    } catch (err, stackTrace) {
      state = AsyncValue.error(err, stackTrace);
    } finally {
      isLoading = false;
    }
  }

  Future<void> fetchMore() async {
    if (state.valueOrNull == null || state.valueOrNull!.length >= total) return;
    await fetchStickers();
  }

  Future<void> refresh() async {
    offset = 0;
    state = const AsyncValue.loading();
    await fetchStickers();
  }
}

@riverpod
Future<SnStickerPack?> stickerPack(Ref ref, String? packId) async {
  if (packId == null) return null;
  final apiClient = ref.watch(apiClientProvider);
  final resp = await apiClient.get('/stickers/$packId');
  return SnStickerPack.fromJson(resp.data);
}

@RoutePage()
class NewStickerPacksScreen extends HookConsumerWidget {
  final String pubName;
  const NewStickerPacksScreen({
    super.key,
    @PathParam("name") required this.pubName,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return EditStickerPacksScreen(pubName: pubName);
  }
}

@RoutePage()
class EditStickerPacksScreen extends HookConsumerWidget {
  final String pubName;
  final String? packId;
  const EditStickerPacksScreen({
    super.key,
    @PathParam("name") required this.pubName,
    @PathParam("packId") this.packId,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final formKey = useMemoized(() => GlobalKey<FormState>(), []);
    final initialPack = ref.watch(stickerPackProvider(packId));

    final nameController = useTextEditingController();
    final descriptionController = useTextEditingController();
    final prefixController = useTextEditingController();

    useEffect(() {
      if (initialPack.value != null) {
        nameController.text = initialPack.value!.name;
        descriptionController.text = initialPack.value!.description;
        prefixController.text = initialPack.value!.prefix;
      }
      return null;
    }, [initialPack]);

    final submitting = useState(false);

    Future<void> submit() async {
      if (!(formKey.currentState?.validate() ?? false)) return;

      try {
        submitting.value = true;
        final apiClient = ref.watch(apiClientProvider);
        final resp = await apiClient.request(
          '/stickers',
          data: {
            'name': nameController.text,
            'description': descriptionController.text,
            'prefix': prefixController.text,
          },
          options: Options(
            method: packId == null ? 'POST' : 'PATCH',
            headers: {'X-Pub': pubName},
          ),
        );
        if (!context.mounted) return;
        context.router.maybePop(SnStickerPack.fromJson(resp.data));
      } catch (err) {
        showErrorAlert(err);
      } finally {
        submitting.value = false;
      }
    }

    return AppScaffold(
      appBar: AppBar(
        title:
            Text(packId == null ? 'createStickerPack' : 'editStickerPack').tr(),
      ),
      body: Column(
        children: [
          Form(
            key: formKey,
            child: Column(
              spacing: 8,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                TextFormField(
                  controller: nameController,
                  decoration: InputDecoration(
                    labelText: 'name'.tr(),
                    border: const UnderlineInputBorder(),
                  ),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'fieldCannotBeEmpty'.tr();
                    }
                    return null;
                  },
                  onTapOutside:
                      (_) => FocusManager.instance.primaryFocus?.unfocus(),
                ),
                TextFormField(
                  controller: descriptionController,
                  decoration: InputDecoration(
                    labelText: 'description'.tr(),
                    border: const UnderlineInputBorder(),
                  ),
                  minLines: 3,
                  maxLines: null,
                  onTapOutside:
                      (_) => FocusManager.instance.primaryFocus?.unfocus(),
                ),
                TextFormField(
                  controller: prefixController,
                  decoration: InputDecoration(
                    labelText: 'stickerPackPrefix'.tr(),
                    border: const UnderlineInputBorder(),
                    helperText: 'deleteStickerHint'.tr(),
                  ),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'fieldCannotBeEmpty'.tr();
                    }
                    return null;
                  },
                  onTapOutside:
                      (_) => FocusManager.instance.primaryFocus?.unfocus(),
                ),
              ],
            ),
          ),
          const Gap(12),
          Align(
            alignment: Alignment.centerRight,
            child: TextButton.icon(
              onPressed: submitting.value ? null : submit,
              icon: const Icon(Symbols.save),
              label: Text(packId == null ? 'create'.tr() : 'saveChanges'.tr()),
            ),
          ),
        ],
      ).padding(horizontal: 24, vertical: 16),
    );
  }
}
