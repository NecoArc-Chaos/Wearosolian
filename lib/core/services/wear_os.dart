import 'package:flutter/material.dart';
import 'package:wear/wear.dart';

/// Returns true if the current device is a Wear OS device.
/// Detection is done by checking if the screen is unusually small (< 300dp),
/// which is typical for smartwatches.
bool isWearDevice(BuildContext context) {
  final size = MediaQuery.of(context).size;
  return size.shortestSide < 300;
}

/// Returns true if the watch face is round.
/// Requires a [WatchShape] ancestor in the widget tree.
bool isRoundWatch(WearShape shape) => shape == WearShape.round;

/// A helper widget that provides Wear OS shape info and calls [builder]
/// with both [isWear] and [isRound] flags.
class WearAwareBuilder extends StatelessWidget {
  final Widget Function(BuildContext context, bool isWear, bool isRound) builder;

  const WearAwareBuilder({super.key, required this.builder});

  @override
  Widget build(BuildContext context) {
    final isWear = isWearDevice(context);
    if (!isWear) {
      return builder(context, false, false);
    }
    return WatchShape(
      builder: (context, shape, _) =>
          builder(context, true, shape == WearShape.round),
    );
  }
}

/// Edge insets for round watch faces to avoid content being hidden by the bezel.
EdgeInsets watchRoundPadding({double horizontal = 16, double vertical = 20}) =>
    EdgeInsets.symmetric(horizontal: horizontal, vertical: vertical);
