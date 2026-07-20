import 'package:flutter/material.dart';
import 'package:island/core/services/wear_os.dart';

/// Enables swipe-to-go-back on Wear OS devices,
/// similar to Orbit's SwipeDismissableNavHost.
///
/// Detects horizontal swipe gestures from the left edge
/// and pops the current navigation route.
class WearSwipeBack extends StatefulWidget {
  final Widget child;

  const WearSwipeBack({super.key, required this.child});

  @override
  State<WearSwipeBack> createState() => _WearSwipeBackState();
}

class _WearSwipeBackState extends State<WearSwipeBack>
    with SingleTickerProviderStateMixin {
  late AnimationController _dismissController;
  late Animation<double> _dismissAnimation;
  double _dragOffset = 0;
  bool _isDragging = false;

  static const _dismissThreshold = 80.0;

  @override
  void initState() {
    super.initState();
    _dismissController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 200),
    );
    _dismissAnimation = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(parent: _dismissController, curve: Curves.easeOut),
    );
  }

  @override
  void dispose() {
    _dismissController.dispose();
    super.dispose();
  }

  void _onHorizontalDragStart(DragStartDetails details) {
    if (!Navigator.of(context).canPop()) return;
    // Only respond to left-edge swipes
    if (details.localPosition.dx > 40) return;
    _isDragging = true;
  }

  void _onHorizontalDragUpdate(DragUpdateDetails details) {
    if (!_isDragging) return;
    setState(() {
      _dragOffset = (details.primaryDelta ?? 0) + _dragOffset;
      _dragOffset = _dragOffset.clamp(0.0, 200.0);
      _dismissController.value = (_dragOffset / _dismissThreshold).clamp(0.0, 1.0);
    });
  }

  void _onHorizontalDragEnd(DragEndDetails details) {
    if (!_isDragging) return;
    _isDragging = false;

    if (_dragOffset > _dismissThreshold) {
      Navigator.of(context).pop();
      _dragOffset = 0;
      _dismissController.reset();
    } else {
      _dismissController.reverse().then((_) {
        if (mounted) {
          setState(() => _dragOffset = 0);
        }
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (!isWearDevice(context)) return widget.child;

    final canPop = Navigator.of(context).canPop();

    return Stack(
      children: [
        // Swipe indicator on the left edge
        if (canPop && _dragOffset > 0)
          Positioned(
            left: 0,
            top: 0,
            bottom: 0,
            child: AnimatedBuilder(
              animation: _dismissAnimation,
              builder: (context, child) {
                return Container(
                  width: 12,
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      colors: [
                        Theme.of(context).colorScheme.primary.withOpacity(
                          _dismissAnimation.value * 0.3,
                        ),
                        Colors.transparent,
                      ],
                    ),
                  ),
                );
              },
            ),
          ),

        // Main content with swipe gesture
        GestureDetector(
          onHorizontalDragStart: _onHorizontalDragStart,
          onHorizontalDragUpdate: _onHorizontalDragUpdate,
          onHorizontalDragEnd: _onHorizontalDragEnd,
          child: Transform.translate(
            offset: Offset(_dragOffset, 0),
            child: widget.child,
          ),
        ),
      ],
    );
  }
}
