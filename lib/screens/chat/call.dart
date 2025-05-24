import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:island/pods/call.dart';

class CallScreen extends HookConsumerWidget {
  const CallScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final callState = ref.watch(callNotifierProvider);
    final callNotifier = ref.read(callNotifierProvider.notifier);

    useEffect(() {
      callNotifier.initialize();
      return null;
    }, []);

    return Scaffold(
      appBar: AppBar(title: const Text('Audio Call')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (callState.error != null)
              Text(callState.error!, style: const TextStyle(color: Colors.red)),
            IconButton(
              icon: Icon(callState.isMuted ? Icons.mic_off : Icons.mic),
              onPressed: callNotifier.toggleMute,
            ),
            if (callState.isConnected)
              const Text('Connected')
            else
              const CircularProgressIndicator(),
          ],
        ),
      ),
    );
  }
}
