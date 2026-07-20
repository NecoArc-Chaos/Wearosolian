/// Stub for island_desktop_presence — desktop presence features are not available on WearOS.

enum ExternalNowPlayingState { playing, paused, stopped }
enum ExternalNowPlayingSource { music, spotify, other }

class ExternalNowPlayingEvent {
  final String? title;
  final String? artist;
  final String? album;
  final ExternalNowPlayingState state;
  final ExternalNowPlayingSource source;
  final String? sourceAppName;
  final String? sourceBundleIdentifier;
  final String? uniqueIdentifier;
  final String? catalogId;
  final String? providerKey;
  final String? providerReferenceId;
  final String? titleUrl;
  final String? subtitleUrl;
  final String? artworkUrl;
  final String? artworkUrlLarge;
  final String? artworkHash;
  final double? playbackRate;
  final Duration? duration;
  final Duration? position;

  const ExternalNowPlayingEvent({
    this.title,
    this.artist,
    this.album,
    this.state = ExternalNowPlayingState.stopped,
    this.source = ExternalNowPlayingSource.other,
    this.sourceAppName,
    this.sourceBundleIdentifier,
    this.uniqueIdentifier,
    this.catalogId,
    this.providerKey,
    this.providerReferenceId,
    this.titleUrl,
    this.subtitleUrl,
    this.artworkUrl,
    this.artworkUrlLarge,
    this.artworkHash,
    this.playbackRate,
    this.duration,
    this.position,
  });
}

class IslandDesktopPresence {
  Stream<ExternalNowPlayingEvent> get externalNowPlayingEvents =>
      const Stream.empty();

  Future<void> startExternalNowPlayingMonitoring({
    Duration pollInterval = const Duration(seconds: 2),
    String? executablePath,
  }) async {}

  Future<void> stopExternalNowPlayingMonitoring() async {}

  Future<void> setAuthToken({String? token, String? serverURL}) async {}
}
