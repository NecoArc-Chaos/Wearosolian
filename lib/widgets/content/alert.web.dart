// ignore_for_file: avoid_web_libraries_in_flutter

import 'dart:developer';
import 'dart:js' as js;

import 'package:dio/dio.dart';
import 'package:easy_localization/easy_localization.dart';

String _parseRemoteError(DioException err) {
  log('${err.requestOptions.method} ${err.requestOptions.uri} ${err.message}');
  if (err.response?.data is String) return err.response?.data;
  if (err.response?.data?['errors'] != null) {
    final errors = err.response?.data['errors'] as Map<String, dynamic>;
    return errors.values
        .map(
          (ele) =>
              (ele as List<dynamic>).map((ele) => ele.toString()).join('\n'),
        )
        .join('\n');
  }
  return err.message ?? err.toString();
}

void showErrorAlert(dynamic err) async {
  final text = switch (err) {
    String _ => err,
    DioException _ => _parseRemoteError(err),
    Exception _ => err.toString(),
    _ => err.toString(),
  };
  js.context.callMethod('swal', ['somethingWentWrong'.tr(), text, 'error']);
}

void showInfoAlert(String message, String title) async {
  js.context.callMethod('swal', [title, message, 'info']);
}

Future<bool> showConfirmAlert(String message, String title) async {
  final result = await js.context.callMethod('swal', [
    title,
    message,
    'question',
    {'buttons': true},
  ]);
  return result == true;
}
