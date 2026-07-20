import 'dart:async';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:solar_network_sdk/solar_network_sdk.dart';

/// Stub for desktop call window — separate call windows are not supported on WearOS.

class CallWindowArgs {
  final String roomId;
  final bool cameraEnabled;

  const CallWindowArgs({required this.roomId, this.cameraEnabled = false});
}

Future<void> notifyCallEnded(String roomId) async {}

void setupCallChannelHandler() {}

CallWindowArgs? parseCallWindowArgs(String raw) => null;

Future<dynamic> createCallWindow(
  SnChatRoom room, {
  bool cameraEnabled = false,
  bool microphoneEnabled = false,
}) async {
  return null;
}

class CallWindowApp extends HookConsumerWidget {
  final CallWindowArgs args;

  const CallWindowApp({super.key, required this.args});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return const SizedBox.shrink();
  }
}
