import 'package:flutter/widgets.dart';

/// Stub replacement for desktop_drop's DropTarget.
/// On WearOS, drag-and-drop from desktop is not supported,
/// so this widget simply renders its child as-is.
class DropTarget extends StatelessWidget {
  final Widget child;
  final void Function(DropTargetDetails)? onDragEntered;
  final void Function(DropTargetDetails)? onDragExited;
  final void Function(DropTargetDetails)? onDragDone;

  const DropTarget({
    super.key,
    required this.child,
    this.onDragEntered,
    this.onDragExited,
    this.onDragDone,
  });

  @override
  Widget build(BuildContext context) => child;
}

class DropTargetDetails {
  final List<dynamic> files;
  const DropTargetDetails({this.files = const []});
}
