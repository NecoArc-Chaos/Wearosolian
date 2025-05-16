import 'dart:convert';

import 'package:crypto/crypto.dart';
import 'package:web/web.dart';

Future<String> getUdid() async {
  final userAgent = window.navigator.userAgent;
  final bytes = utf8.encode(userAgent);
  final hash = sha256.convert(bytes);
  return hash.toString();
}
