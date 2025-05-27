import 'dart:io';

import 'package:auto_route/annotations.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:island/pods/network.dart';
import 'package:island/pods/userinfo.dart';
import 'package:island/screens/auth/captcha.dart';
import 'package:island/services/responsive.dart';
import 'package:island/widgets/alert.dart';
import 'package:island/widgets/app_scaffold.dart';
import 'package:material_symbols_icons/symbols.dart';
import 'package:styled_widget/styled_widget.dart';

@RoutePage()
class AccountSettingsScreen extends HookConsumerWidget {
  const AccountSettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDesktop =
        !kIsWeb && (Platform.isWindows || Platform.isMacOS || Platform.isLinux);
    final isWide = isWideScreen(context);

    Future<void> requestAccountDeletion() async {
      final confirm = await showConfirmAlert(
        'accountDeletionHint'.tr(),
        'accountDeletion'.tr(),
      );
      if (!confirm || !context.mounted) return;
      try {
        final client = ref.read(apiClientProvider);
        await client.delete('/accounts/me');
        if (context.mounted) {
          showSnackBar(context, 'accountDeletionSent'.tr());
        }
      } catch (err) {
        showErrorAlert(err);
      }
    }

    Future<void> requestResetPassword() async {
      final confirm = await showConfirmAlert(
        'accountPasswordChangeDescription'.tr(),
        'accountPassword'.tr(),
      );
      if (!confirm || !context.mounted) return;
      final captchaTk = await Navigator.of(
        context,
      ).push(MaterialPageRoute(builder: (context) => CaptchaScreen()));
      if (captchaTk == null) return;
      try {
        final userInfo = ref.read(userInfoProvider);
        final client = ref.read(apiClientProvider);
        await client.post(
          '/accounts/recovery/password',
          data: {'account': userInfo.value!.name, 'captcha_token': captchaTk},
        );
        if (context.mounted) {
          showSnackBar(context, 'accountPasswordChangeSent'.tr());
        }
      } catch (err) {
        showErrorAlert(err);
      }
    }

    // Group settings into categories for better organization
    final securitySettings = [
      ListTile(
        minLeadingWidth: 48,
        title: Text('accountPassword').tr(),
        subtitle: Text('accountPasswordDescription').tr().fontSize(12),
        contentPadding: const EdgeInsets.only(left: 24, right: 17),
        leading: const Icon(Symbols.password),
        trailing: const Icon(Symbols.chevron_right),
        onTap: () {
          requestResetPassword();
        },
      ),
      ListTile(
        minLeadingWidth: 48,
        title: Text('accountTwoFactor').tr(),
        subtitle: Text('accountTwoFactorDescription').tr().fontSize(12),
        contentPadding: const EdgeInsets.only(left: 24, right: 17),
        leading: const Icon(Symbols.security),
        trailing: const Icon(Symbols.chevron_right),
        onTap: () {
          // Navigate to two-factor authentication settings
          showDialog(
            context: context,
            builder:
                (context) => AlertDialog(
                  title: Text('accountTwoFactor').tr(),
                  content: Text('accountTwoFactorSetupDescription').tr(),
                  actions: [
                    TextButton(
                      onPressed: () => Navigator.of(context).pop(),
                      child: Text('Close').tr(),
                    ),
                    TextButton(
                      onPressed: () {
                        Navigator.of(context).pop();
                        // Add navigation to 2FA setup screen
                      },
                      child: Text('accountTwoFactorSetup').tr(),
                    ),
                  ],
                ),
          );
        },
      ),
    ];

    final privacySettings = [
      // ListTile(
      //   minLeadingWidth: 48,
      //   title: Text('accountPrivacy').tr(),
      //   subtitle: Text('accountPrivacyDescription').tr().fontSize(12),
      //   contentPadding: const EdgeInsets.only(left: 24, right: 17),
      //   leading: const Icon(Symbols.visibility),
      //   trailing: const Icon(Symbols.chevron_right),
      //   onTap: () {
      //     // Navigate to privacy settings
      //   },
      // ),
      ListTile(
        minLeadingWidth: 48,
        title: Text('accountDataExport').tr(),
        subtitle: Text('accountDataExportDescription').tr().fontSize(12),
        contentPadding: const EdgeInsets.only(left: 24, right: 17),
        leading: const Icon(Symbols.download),
        trailing: const Icon(Symbols.chevron_right),
        onTap: () async {
          final confirm = await showConfirmAlert(
            'accountDataExportConfirmation'.tr(),
            'accountDataExport'.tr(),
          );
          if (!confirm || !context.mounted) return;
          // Add data export logic
          showSnackBar(context, 'accountDataExportRequested'.tr());
        },
      ),
    ];

    final dangerZoneSettings = [
      ListTile(
        minLeadingWidth: 48,
        title: Text('accountDeletion').tr(),
        subtitle: Text('accountDeletionDescription').tr().fontSize(12),
        contentPadding: const EdgeInsets.only(left: 24, right: 17),
        leading: const Icon(Symbols.delete_forever, color: Colors.red),
        trailing: const Icon(Symbols.chevron_right),
        onTap: requestAccountDeletion,
      ),
    ];

    // Create a responsive layout based on screen width
    Widget buildSettingsList() {
      if (isWide) {
        // Two-column layout for wide screens
        return Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _SettingsSection(
                    title: 'accountSecurityTitle',
                    children: securitySettings,
                  ),
                  _SettingsSection(
                    title: 'accountPrivacyTitle',
                    children: privacySettings,
                  ),
                ],
              ),
            ),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _SettingsSection(
                    title: 'accountDangerZoneTitle',
                    children: dangerZoneSettings,
                  ),
                ],
              ),
            ),
          ],
        ).padding(horizontal: 16);
      } else {
        // Single column layout for narrow screens
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _SettingsSection(
              title: 'accountSecurityTitle',
              children: securitySettings,
            ),
            _SettingsSection(
              title: 'accountPrivacyTitle',
              children: privacySettings,
            ),
            _SettingsSection(
              title: 'accountDangerZoneTitle',
              children: dangerZoneSettings,
            ),
          ],
        );
      }
    }

    return AppScaffold(
      appBar: AppBar(
        title: Text('accountSettings').tr(),
        actions:
            isDesktop
                ? [
                  IconButton(
                    icon: const Icon(Symbols.help_outline),
                    onPressed: () {
                      // Show help dialog
                      showDialog(
                        context: context,
                        builder:
                            (context) => AlertDialog(
                              title: Text('accountSettingsHelp').tr(),
                              content: Text('accountSettingsHelpContent').tr(),
                              actions: [
                                TextButton(
                                  onPressed: () => Navigator.of(context).pop(),
                                  child: Text('Close').tr(),
                                ),
                              ],
                            ),
                      );
                    },
                  ),
                ]
                : null,
      ),
      body: Focus(
        autofocus: true,
        onKeyEvent: (node, event) {
          // Add keyboard shortcuts for desktop
          if (isDesktop &&
              event is KeyDownEvent &&
              event.logicalKey == LogicalKeyboardKey.escape) {
            Navigator.of(context).pop();
            return KeyEventResult.handled;
          }
          return KeyEventResult.ignored;
        },
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(vertical: 16),
          child: buildSettingsList(),
        ),
      ),
    );
  }
}

// Helper widget for displaying settings sections with titles
class _SettingsSection extends StatelessWidget {
  final String title;
  final List<Widget> children;

  const _SettingsSection({required this.title, required this.children});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(24, 16, 24, 8),
          child: Text(
            title.tr(),
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              color: Theme.of(context).colorScheme.primary,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
        ...children,
        const SizedBox(height: 16),
      ],
    );
  }
}
