import 'package:flutter/material.dart';
import 'package:island/core/services/wear_os.dart';
import 'dart:math' as math;

/// Wraps a list item with scaling transform for round watch edge effect.
///
/// Usage in any itemBuilder:
/// ```dart
/// itemBuilder: (context, index, post) {
///   return WearScalingItem(index: index, itemCount: items.length) {
///     return PostItem(post: post);
///   };
/// }
/// ```
class WearScalingItem extends StatelessWidget {
  final int index;
  final int itemCount;
  final Widget child;

  const WearScalingItem({
    super.key,
    required this.index,
    required this.itemCount,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    if (!isWearDevice(context)) return child;

    final viewportHeight = MediaQuery.of(context).size.height;
    // Estimate item position relative to viewport center
    // index 0 is top, itemCount-1 is bottom
    final middleIndex = itemCount / 2;
    final distanceFromCenter = (index - middleIndex).abs();
    final maxDistance = itemCount / 2;

    if (maxDistance <= 0) return child;

    final normalized = (distanceFromCenter / maxDistance).clamp(0.0, 1.0);
    const minScale = 0.78;
    final t = math.sin(normalized * math.pi / 2);
    final scale = 1.0 - (1.0 - minScale) * t;
    final opacity = (scale - minScale) / (1.0 - minScale);

    return Opacity(
      opacity: (0.3 + opacity * 0.7).clamp(0.3, 1.0),
      child: Transform.scale(
        scale: scale,
        child: child,
      ),
    );
  }
}
