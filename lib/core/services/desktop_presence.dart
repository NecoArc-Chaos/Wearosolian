import 'dart:async';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:island/shared/stubs/island_desktop_presence_stub.dart';

class NowPlayingState {
  final ExternalNowPlayingEvent? event;
  final Map<String, dynamic>? activityData;
  final bool isPublishing;
  final String? lastError;

  const NowPlayingState({
    this.event,
    this.activityData,
    this.isPublishing = false,
    this.lastError,
  });

  String? get title => event?.title;
  String? get artist => event?.artist;
  String? get album => event?.album;
  dynamic get playbackState => event?.state;
  dynamic get source => event?.source;
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
