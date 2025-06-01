import 'package:auto_route/auto_route.dart';
import 'package:croppy/croppy.dart' hide cropImage;
import 'package:dropdown_button2/dropdown_button2.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:island/pods/config.dart';
import 'package:island/pods/network.dart';
import 'package:island/pods/userinfo.dart';
import 'package:island/services/file.dart';
import 'package:island/widgets/alert.dart';
import 'package:island/widgets/app_scaffold.dart';
import 'package:island/widgets/content/cloud_files.dart';
import 'package:material_symbols_icons/symbols.dart';
import 'package:styled_widget/styled_widget.dart';

const kServerSupportedLanguages = {'en-US': 'en-us', 'zh-CN': 'zh-hans'};

@RoutePage()
class UpdateProfileScreen extends HookConsumerWidget {
  const UpdateProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(userInfoProvider);

    final submitting = useState(false);

    void updateProfilePicture(String position) async {
      showLoadingModal(context);
      var result = await ref
          .read(imagePickerProvider)
          .pickImage(source: ImageSource.gallery);
      if (result == null) {
        if (context.mounted) hideLoadingModal(context);
        return;
      }
      if (!context.mounted) return;
      hideLoadingModal(context);
      result = await cropImage(
        context,
        image: result,
        allowedAspectRatios: [
          if (position == 'background')
            CropAspectRatio(height: 7, width: 16)
          else
            CropAspectRatio(height: 1, width: 1),
        ],
      );
      if (result == null) {
        if (context.mounted) hideLoadingModal(context);
        return;
      }
      if (!context.mounted) return;
      showLoadingModal(context);

      submitting.value = true;
      try {
        final baseUrl = ref.watch(serverUrlProvider);
        final token = await getToken(ref.watch(tokenProvider));
        if (token == null) throw ArgumentError('Token is null');
        final cloudFile =
            await putMediaToCloud(
              fileData: result,
              atk: token,
              baseUrl: baseUrl,
              filename: result.name,
              mimetype: result.mimeType ?? 'image/jpeg',
            ).future;
        if (cloudFile == null) {
          throw ArgumentError('Failed to upload the file...');
        }
        final client = ref.watch(apiClientProvider);
        await client.patch(
          '/accounts/me/profile',
          data: {'${position}_id': cloudFile.id},
        );
        final userNotifier = ref.read(userInfoProvider.notifier);
        userNotifier.fetchUser();
      } catch (err) {
        showErrorAlert(err);
      } finally {
        submitting.value = false;
        if (context.mounted) hideLoadingModal(context);
      }
    }

    final formKeyBasicInfo = useMemoized(GlobalKey<FormState>.new, const []);
    final usernameController = useTextEditingController(text: user.value!.name);
    final nicknameController = useTextEditingController(text: user.value!.nick);
    final language = useState(user.value!.language);

    void updateBasicInfo() async {
      if (!formKeyBasicInfo.currentState!.validate()) return;

      submitting.value = true;
      try {
        final client = ref.watch(apiClientProvider);
        await client.patch(
          '/accounts/me',
          data: {
            'name': usernameController.text,
            'nick': nicknameController.text,
            'language': language.value,
          },
        );
        final userNotifier = ref.read(userInfoProvider.notifier);
        userNotifier.fetchUser();
      } catch (err) {
        showErrorAlert(err);
      } finally {
        submitting.value = false;
      }
    }

    final formKeyProfile = useMemoized(GlobalKey<FormState>.new, const []);
    final bioController = useTextEditingController(
      text: user.value!.profile.bio,
    );

    void updateProfile() async {
      if (!formKeyProfile.currentState!.validate()) return;

      submitting.value = true;
      try {
        final client = ref.watch(apiClientProvider);
        await client.patch(
          '/accounts/me/profile',
          data: {'bio': bioController.text},
        );
        final userNotifier = ref.read(userInfoProvider.notifier);
        userNotifier.fetchUser();
      } catch (err) {
        showErrorAlert(err);
      } finally {
        submitting.value = false;
      }
    }

    return AppScaffold(
      appBar: AppBar(
        title: Text('updateYourProfile').tr(),
        leading: const PageBackButton(),
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.only(bottom: MediaQuery.of(context).padding.bottom),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
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
                          user.value!.profile.background?.id != null
                              ? CloudImageWidget(
                                fileId: user.value!.profile.background!.id,
                                fit: BoxFit.cover,
                              )
                              : const SizedBox.shrink(),
                    ),
                    onTap: () {
                      updateProfilePicture('background');
                    },
                  ),
                  Positioned(
                    left: 20,
                    bottom: -32,
                    child: GestureDetector(
                      child: ProfilePictureWidget(
                        fileId: user.value!.profile.picture?.id,
                        radius: 40,
                      ),
                      onTap: () {
                        updateProfilePicture('picture');
                      },
                    ),
                  ),
                ],
              ),
            ).padding(bottom: 32),
            Text('accountBasicInfo')
                .tr()
                .bold()
                .fontSize(18)
                .padding(horizontal: 24, top: 16, bottom: 12),
            Form(
              key: formKeyBasicInfo,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                spacing: 16,
                children: [
                  TextFormField(
                    decoration: InputDecoration(
                      labelText: 'username'.tr(),
                      helperText: 'usernameCannotChangeHint'.tr(),
                      prefixText: '@',
                    ),
                    controller: usernameController,
                    readOnly: true,
                    onTapOutside:
                        (_) => FocusManager.instance.primaryFocus?.unfocus(),
                  ),
                  TextFormField(
                    decoration: InputDecoration(labelText: 'nickname'.tr()),
                    controller: nicknameController,
                    onTapOutside:
                        (_) => FocusManager.instance.primaryFocus?.unfocus(),
                  ),
                  DropdownButtonFormField2<String>(
                    decoration: InputDecoration(
                      labelText: 'language'.tr(),
                      helperText: 'accountLanguageHint'.tr(),
                    ),
                    items: [
                      ...kServerSupportedLanguages.values.map(
                        (e) => DropdownMenuItem(value: e, child: Text(e)),
                      ),
                      if (!kServerSupportedLanguages.containsValue(
                        language.value,
                      ))
                        DropdownMenuItem(
                          value: language.value,
                          child: Text(language.value),
                        ),
                    ],
                    value: language.value,
                    onChanged: (value) {
                      language.value = value ?? language.value;
                    },
                    customButton: Row(
                      children: [
                        Expanded(child: Text(language.value)),
                        Icon(Symbols.arrow_drop_down),
                      ],
                    ),
                  ),
                  Align(
                    alignment: Alignment.centerRight,
                    child: TextButton.icon(
                      onPressed: submitting.value ? null : updateBasicInfo,
                      label: Text('saveChanges').tr(),
                      icon: const Icon(Symbols.save),
                    ),
                  ),
                ],
              ).padding(horizontal: 24),
            ),
            Text('accountProfile')
                .tr()
                .bold()
                .fontSize(18)
                .padding(horizontal: 24, top: 16, bottom: 8),
            Form(
              key: formKeyProfile,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                spacing: 16,
                children: [
                  TextFormField(
                    decoration: InputDecoration(labelText: 'bio'.tr()),
                    maxLines: null,
                    minLines: 3,
                    controller: bioController,
                    onTapOutside:
                        (_) => FocusManager.instance.primaryFocus?.unfocus(),
                  ),
                  Align(
                    alignment: Alignment.centerRight,
                    child: TextButton.icon(
                      onPressed: submitting.value ? null : updateProfile,
                      label: Text('saveChanges').tr(),
                      icon: const Icon(Symbols.save),
                    ),
                  ),
                ],
              ).padding(horizontal: 24),
            ),
          ],
        ),
      ),
    );
  }
}
