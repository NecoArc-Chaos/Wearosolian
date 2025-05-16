import 'package:flutter_udid/flutter_udid.dart';

Future<String> getUdid() async {
  return await FlutterUdid.consistentUdid;
}
