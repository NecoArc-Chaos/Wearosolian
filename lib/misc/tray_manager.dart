import 'package:flutter/widgets.dart';

/// Stub for tray_manager — desktop system tray is not supported on WearOS.
class TrayManager {
  static final TrayManager instance = TrayManager._();
  TrayManager._();

  Future<void> initSystemTray(WidgetRef ref) async {}
  Future<void> destroy() async {}
}
