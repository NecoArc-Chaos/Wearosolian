import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_quill/flutter_quill.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class QuillContent extends HookConsumerWidget {
  final Document document;
  const QuillContent({super.key, required this.document});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final controller = useMemoized(() => QuillController.basic());

    useEffect(() {
      controller.document = document;
      controller.readOnly = true;
      return null;
    }, [document]);

    return QuillEditor.basic(
      controller: controller,
      config: const QuillEditorConfig(),
    );
  }
}
