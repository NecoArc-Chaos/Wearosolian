import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:island/models/file.dart';
import 'package:island/models/user.dart';

part 'realm.freezed.dart';
part 'realm.g.dart';

@freezed
abstract class SnRealm with _$SnRealm {
  const factory SnRealm({
    required int id,
    required String slug,
    required String name,
    required String description,
    required String? verifiedAs,
    required DateTime? verifiedAt,
    required bool isCommunity,
    required bool isPublic,
    required String? pictureId,
    required SnCloudFile? picture,
    required String? backgroundId,
    required SnCloudFile? background,
    required int accountId,
    required DateTime createdAt,
    required DateTime updatedAt,
    required DateTime? deletedAt,
  }) = _SnRealm;

  factory SnRealm.fromJson(Map<String, dynamic> json) =>
      _$SnRealmFromJson(json);
}

@freezed
abstract class SnRealmMember with _$SnRealmMember {
  const factory SnRealmMember({
    required int realmId,
    required SnRealm? realm,
    required int accountId,
    required SnAccount? account,
    required int role,
    required DateTime? joinedAt,
    required DateTime createdAt,
    required DateTime updatedAt,
    required DateTime? deletedAt,
  }) = _SnRealmMember;

  factory SnRealmMember.fromJson(Map<String, dynamic> json) =>
      _$SnRealmMemberFromJson(json);
}
