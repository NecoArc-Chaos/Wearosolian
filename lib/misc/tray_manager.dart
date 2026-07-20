import 'package:hooks_riverpod/hooks_riverpod.dart';

/// Stub for tray_manager — desktop system tray is not supported on WearOS.

class MenuItem {
  final String key;
  final String label;
  final MenuItem? menu;

  const MenuItem({required this.key, required this.label, this.menu});
}

mixin TrayListener {}

class TrayManager {
  static final TrayManager instance = TrayManager._();
  TrayManager._();

  Future<void> initSystemTray(WidgetRef ref) async {}
  Future<void> destroy() async {}
  Future<void> popUpContextMenu() async {}
}

final trayManager = TrayManager.instance;

class TrayService {
  static final TrayService instance = TrayService._();
  TrayService._();

  Future<void> initialize({
    required WidgetRef ref,
    required void Function() onTrayIconRightMouseUp,
    required void Function(MenuItem) onTrayMenuItemClick,
  }) async {}

  void handleAction(MenuItem menuItem) {}
  Future<void> dispose({required WidgetRef ref}) async {}
}
