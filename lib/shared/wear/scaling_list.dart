import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:island/core/services/wear_os.dart';

/// A scrollable list that scales items near the edges on round watches,
/// inspired by Wear Material's TransformingLazyColumn (used in Orbit).
///
/// On square watches or phones, this behaves like a normal ListView.
class WearScalingList extends StatefulWidget {
  final int itemCount;
  final Widget? Function(BuildContext context, int index) itemBuilder;
  final ScrollController? controller;
  final EdgeInsetsGeometry? padding;
  final double? itemExtent;

  const WearScalingList({
    super.key,
    required this.itemCount,
    required this.itemBuilder,
    this.controller,
    this.padding,
    this.itemExtent,
  });

  @override
  State<WearScalingList> createState() => _WearScalingListState();
}

class _WearScalingListState extends State<WearScalingList> {
  late ScrollController _scrollController;
  double _scrollOffset = 0;

  @override
  void initState() {
    super.initState();
    _scrollController = widget.controller ?? ScrollController();
    _scrollController.addListener(_onScroll);
  }

  @override
  void didUpdateWidget(WearScalingList oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.controller != widget.controller) {
      _scrollController.removeListener(_onScroll);
      _scrollController = widget.controller ?? ScrollController();
      _scrollController.addListener(_onScroll);
    }
  }

  @override
  void dispose() {
    if (widget.controller == null) _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (!mounted) return;
    setState(() {
      _scrollOffset = _scrollController.hasClients
          ? _scrollController.offset
          : 0;
    });
  }

  double _scaleForOffset(
      double itemCenter, double viewportCenter, double viewportHeight) {
    final distance = (itemCenter - viewportCenter).abs();
    final half = viewportHeight / 2;
    const minScale = 0.7;
    final normalized = (distance / half).clamp(0.0, 1.0);
    final t = math.sin(normalized * math.pi / 2);
    return 1.0 - (1.0 - minScale) * t;
  }

  @override
  Widget build(BuildContext context) {
    if (!isWearDevice(context)) {
      return ListView.builder(
        controller: _scrollController,
        itemCount: widget.itemCount,
        padding: widget.padding,
        itemExtent: widget.itemExtent,
        itemBuilder: widget.itemBuilder,
      );
    }

    return _WearListLayout(
      scrollController: _scrollController,
      scrollOffset: _scrollOffset,
      itemCount: widget.itemCount,
      itemBuilder: widget.itemBuilder,
      itemExtent: widget.itemExtent,
      scaleForOffset: _scaleForOffset,
    );
  }
}

class _WearListLayout extends StatelessWidget {
  final ScrollController scrollController;
  final double scrollOffset;
  final int itemCount;
  final Widget? Function(BuildContext, int) itemBuilder;
  final double? itemExtent;
  final double Function(double, double, double) scaleForOffset;

  const _WearListLayout({
    required this.scrollController,
    required this.scrollOffset,
    required this.itemCount,
    required this.itemBuilder,
    required this.itemExtent,
    required this.scaleForOffset,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final h = itemExtent ?? 64.0;
        final vh = constraints.maxHeight;
        return ListView.builder(
          controller: scrollController,
          itemCount: itemCount,
          padding: EdgeInsets.symmetric(vertical: vh / 2 - h / 2),
          itemExtent: h,
          itemBuilder: (context, index) {
            final itemCenter = index * h + h / 2 - scrollOffset;
            final viewportCenter = vh / 2;
            final scale = scaleForOffset(itemCenter, viewportCenter, vh);
            final opacity = ((scale - 0.7) / 0.3).clamp(0.2, 1.0);

            return Transform.scale(
              scale: scale,
              child: Opacity(
                opacity: opacity,
                child: itemBuilder(context, index),
              ),
            );
          },
        );
      },
    );
  }
}
