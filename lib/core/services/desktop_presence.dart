import 'dart:async';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class NowPlayingState {
  const NowPlayingState({
    this.isPublishing = false,
    this.lastError,
  });

  final bool isPublishing;
  final String? lastError;

  String? get title => null;
  String? get artist => null;
  String? get album => null;
  dynamic get playbackState => null;
  dynamic get source => null;
}

final desktopPresenceProvider = Provider<DesktopPresenceService?>((ref) {
  return null;
});

final desktopNowPlayingProvider = Provider<DesktopNowPlayingService?>((ref) {
  return null;
});

final desktopNowPlayingStateProvider = StreamProvider<NowPlayingState?>((ref) {
  return const Stream.empty();
});

class DesktopPresenceService {
  DesktopPresenceService(this._ref);
  final Ref _ref;

  Future<void> start() async {}
  Future<void> dispose() async {}
}

class DesktopNowPlayingService {
  DesktopNowPlayingService(this._ref);
  final Ref _ref;

  dynamic get lastEvent => null;
  Map<String, dynamic>? get currentActivityData => null;
  Stream<NowPlayingState> get stateStream => const Stream.empty();

  Future<void> start() async {}
  Future<void> dispose() async {}
}
