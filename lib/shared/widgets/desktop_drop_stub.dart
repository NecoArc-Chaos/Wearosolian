import 'package:cross_file/cross_file.dart';
import 'package:flutter/widgets.dart';

/// Stub replacement for desktop_drop.
/// On WearOS, drag-and-drop from desktop is not supported,
/// so this widget simply renders its child as-is.
class DropTarget extends StatelessWidget {
  final Widget child;
  final void Function(DropDoneDetails)? onDragEntered;
  final void Function(DropDoneDetails)? onDragExited;
  final void Function(DropDoneDetails)? onDragDone;

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

class DropDoneDetails {
  final List<XFile> files;
  const DropDoneDetails({this.files = const []});
}
