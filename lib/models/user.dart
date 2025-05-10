import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:island/models/file.dart';

part 'user.freezed.dart';
part 'user.g.dart';

@freezed
abstract class SnAccount with _$SnAccount {
  const factory SnAccount({
    required int id,
    required String name,
    required String nick,
    required String language,
    required bool isSuperuser,
    required SnAccountProfile profile,
    required DateTime createdAt,
    required DateTime updatedAt,
    required DateTime? deletedAt,
  }) = _SnAccount;

  factory SnAccount.fromJson(Map<String, dynamic> json) =>
      _$SnAccountFromJson(json);
}

@freezed
abstract class SnAccountProfile with _$SnAccountProfile {
  const factory SnAccountProfile({
    required int id,
    required String? firstName,
    required String? middleName,
    required String? lastName,
    required String? bio,
    required String? pictureId,
    required SnCloudFile? picture,
    required String? backgroundId,
    required SnCloudFile? background,
    required DateTime createdAt,
    required DateTime updatedAt,
    required DateTime? deletedAt,
  }) = _SnAccountProfile;

  factory SnAccountProfile.fromJson(Map<String, dynamic> json) =>
      _$SnAccountProfileFromJson(json);
}

@freezed
abstract class SnAccountStatus with _$SnAccountStatus {
  const factory SnAccountStatus({
    required String id,
    required int attitude,
    required bool isOnline,
    required bool isInvisible,
    required bool isNotDisturb,
    required bool isCustomized,
    @Default("") String label,
    required DateTime? clearedAt,
    required int accountId,
    required DateTime createdAt,
    required DateTime updatedAt,
    required DateTime? deletedAt,
  }) = _SnAccountStatus;

  factory SnAccountStatus.fromJson(Map<String, dynamic> json) =>
      _$SnAccountStatusFromJson(json);
}
