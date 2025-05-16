import 'package:web/web.dart' as web;
import 'package:flutter/material.dart';

class UniversalImage extends StatelessWidget {
  final String uri;
  final String? blurHash;
  final BoxFit fit;
  final double? width;
  final double? height;
  // No cache optimization for web
  final bool noCacheOptimization;

  const UniversalImage({
    super.key,
    required this.uri,
    this.blurHash,
    this.fit = BoxFit.cover,
    this.width,
    this.height,
    this.noCacheOptimization = false,
  });

  @override
  Widget build(BuildContext context) {
    return HtmlElementView.fromTagName(
      tagName: 'img',
      onElementCreated: (element) {
        element as web.HTMLImageElement;
        element.src = uri;
        element.style.width = width?.toString() ?? '100%';
        element.style.height = height?.toString() ?? '100%';
        element.style.objectFit = switch (fit) {
          BoxFit.cover || BoxFit.fitWidth || BoxFit.fitHeight => 'cover',
          BoxFit.fill => 'fill',
          BoxFit.contain => 'contain',
          BoxFit.none => 'none',
          _ => 'cover',
        };
      },
    );
  }
}
