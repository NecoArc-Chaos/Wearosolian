import 'package:flutter_webrtc/flutter_webrtc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:dio/dio.dart';
import 'package:island/pods/network.dart';

part 'call.g.dart';
part 'call.freezed.dart';

@freezed
sealed class CallState with _$CallState {
  const factory CallState({
    required bool isMuted,
    required bool isConnected,
    String? error,
  }) = _CallState;
}

@riverpod
class CallNotifier extends _$CallNotifier {
  RTCPeerConnection? _peerConnection;
  MediaStream? _localStream;
  final _localRenderer = RTCVideoRenderer();

  @override
  CallState build() {
    return const CallState(isMuted: false, isConnected: false);
  }

  Future<void> initialize() async {
    try {
      await _localRenderer.initialize();

      // Get user media (audio)
      _localStream = await navigator.mediaDevices.getUserMedia({
        'audio': true,
        'video': false,
      });

      // Create peer connection
      _peerConnection = await createPeerConnection({
        'iceServers': [
          {'urls': 'stun:stun.l.google.com:19302'},
          // Add your Cloudflare TURN servers here
        ],
      });

      // Add local stream to peer connection
      _localStream!.getTracks().forEach((track) {
        _peerConnection!.addTrack(track, _localStream!);
      });

      // Handle incoming tracks
      _peerConnection!.onTrack = (RTCTrackEvent event) {
        if (event.track.kind == 'audio') {
          // Handle remote audio track
        }
      };

      state = state.copyWith(isConnected: true);
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> createSession() async {
    try {
      final apiClient = ref.read(apiClientProvider);
      final response = await apiClient.post(
        'YOUR_CLOUDFLARE_CALLS_ENDPOINT/sessions',
        options: Options(headers: {'Content-Type': 'application/json'}),
      );

      if (response.statusCode == 200) {
        // Handle session creation
      }
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  void toggleMute() {
    state = state.copyWith(isMuted: !state.isMuted);
    _localStream?.getAudioTracks().forEach((track) {
      track.enabled = !state.isMuted;
    });
  }

  void dispose() {
    _localStream?.dispose();
    _peerConnection?.dispose();
    _localRenderer.dispose();
  }
}
