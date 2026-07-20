import 'dart:ui';
import 'package:flutter/material.dart';

/// Stub for window_manager — desktop window management is not supported on WearOS.

enum TitleBarStyle { hidden, normal }

class WindowOptions {
  final Size size;
  final Size? minimumSize;
  final Size? maximumSize;
  final bool center;
  final Color backgroundColor;
  final bool skipTaskbar;
  final TitleBarStyle titleBarStyle;
  final bool windowButtonVisibility;
  final bool alwaysOnTop;

  const WindowOptions({
    this.size = const Size(360, 640),
    this.minimumSize,
    this.maximumSize,
    this.center = false,
    this.backgroundColor = Colors.transparent,
    this.skipTaskbar = false,
    this.titleBarStyle = TitleBarStyle.normal,
    this.windowButtonVisibility = true,
    this.alwaysOnTop = false,
  });
}

class WindowManager {
  static final WindowManager instance = WindowManager._();
  WindowManager._();

  Future<void> ensureInitialized() async {}
  Future<void> waitUntilReadyToShow(WindowOptions options, VoidCallback callback) async {}
  Future<void> show() async {}
  Future<void> focus() async {}
  Future<void> hide() async {}
  Future<void> setAlwaysOnTop(bool value) async {}
  Future<void> setResizable(bool value) async {}
  Future<void> setMaximizable(bool value) async {}
  Future<void> setMinimumSize(Size size) async {}
  Future<void> setOpacity(double opacity) async {}
  Future<void> setAsFrameless() async {}
  Future<Rect> getBounds() async => Rect.zero;
}

final windowManager = WindowManager.instance;

mixin WindowListener {}
