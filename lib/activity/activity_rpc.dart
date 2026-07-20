import 'dart:async';
import 'package:dio/dio.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:island/core/config.dart';
import 'package:island/core/network.dart';
import 'package:island/core/websocket.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:solar_network_sdk/solar_network_sdk.dart';

part 'activity_rpc.g.dart';

class ActivityRpcServer {
  ActivityRpcServer(Map<String, Function> handlers);

  void updateHandlers(Map<String, Function> newHandlers) {}
  Future<void> start() async {}
  Future<void> stop() async {}
}

class ServerState {
  final String status;
  final List<String> activities;
  final String? currentActivityManualId;
  final Map<String, dynamic>? currentActivityData;
  final List<Map<String, dynamic>> recentPackets;

  ServerState({
    required this.status,
    this.activities = const [],
    this.currentActivityManualId,
    this.currentActivityData,
    this.recentPackets = const [],
  });

  ServerState copyWith({
    String? status,
    List<String>? activities,
    String? currentActivityManualId,
    Map<String, dynamic>? currentActivityData,
    List<Map<String, dynamic>>? recentPackets,
  }) {
    return ServerState(
      status: status ?? this.status,
      activities: activities ?? this.activities,
      currentActivityManualId:
          currentActivityManualId ?? this.currentActivityManualId,
      currentActivityData: currentActivityData ?? this.currentActivityData,
      recentPackets: recentPackets ?? this.recentPackets,
    );
  }
}

class ServerStateNotifier extends Notifier<ServerState> {
  late ActivityRpcServer server;
  late Dio apiClient;

  @override
  ServerState build() {
    apiClient = ref.watch(apiClientProvider);
    server = ActivityRpcServer({});
    return ServerState(status: 'Disabled');
  }

  void clearPackets() {}
  Future<void> toggleServer(bool enabled) async {}
  Future<void> start() async {}
  void updateStatus(String status) {}
  void addActivity(String activity) {}
  void setCurrentActivity(String? id, Map<String, dynamic>? data) {}
}

final rpcServerStateProvider =
    NotifierProvider<ServerStateNotifier, ServerState>(ServerStateNotifier.new);

final rpcServerProvider = Provider<ActivityRpcServer>((ref) {
  final notifier = ref.watch(rpcServerStateProvider.notifier);
  return notifier.server;
});

@riverpod
Future<List<SnPresenceActivity>> presenceActivities(
  Ref ref,
  String uname,
) async {
  final apiClient = ref.watch(apiClientProvider);
  final response = await apiClient.get('/passport/activities/$uname');
  final data = response.data as List<dynamic>;
  final activities = data
      .map((json) => SnPresenceActivity.fromJson(json))
      .toList();

  if (activities.isNotEmpty) {
    final accountId = activities.first.accountId;
    final websocket = ref.watch(websocketProvider);
    final subscription = websocket.dataStream.listen((packet) {
      if (packet.type == 'account.presence.activities.updated' &&
          packet.data?['account_id'] == accountId) {
        ref.invalidateSelf();
      }
    });
    ref.onDispose(subscription.cancel);
  }

  return activities;
}
