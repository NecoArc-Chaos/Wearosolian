import 'package:flutter/material.dart';
import 'package:wear/wear.dart' as wear;
import 'package:island/core/services/wear_os.dart';

/// Wraps the app with AmbientMode support for always-on display.
///
/// In ambient mode, the UI is simplified: backgrounds become black,
/// content is reduced to essential information with minimal white text.
class WearAmbientMode extends StatelessWidget {
  final Widget child;

  const WearAmbientMode({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    if (!isWearDevice(context)) return child;

    return wear.AmbientMode(
      builder: (context, mode, child) {
        if (mode == wear.AmbientMode.active) {
          return child!;
        }
        // In ambient mode: dark background, simplified UI
        return _AmbientWrapper(child: child!);
      },
      child: child,
    );
  }
}

class _AmbientWrapper extends StatelessWidget {
  final Widget child;
  const _AmbientWrapper({required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.black,
      child: child,
    );
  }
}

/// A widget that adapts its appearance for ambient mode.
/// Use this to hide non-essential elements when the watch is in ambient mode.
class AmbientAware extends StatelessWidget {
  final WidgetBuilder builder;

  const AmbientAware({super.key, required this.builder});

  @override
  Widget build(BuildContext context) {
    try {
      final mode = wear.AmbientMode.of(context);
      if (mode == wear.AmbientMode.active) {
        return builder(context);
      }
      // In ambient/low-power mode: render a simplified view
      return const SizedBox.shrink();
    } catch (_) {
      return builder(context);
    }
  }
}
