// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_SnAccount _$SnAccountFromJson(Map<String, dynamic> json) => _SnAccount(
  id: (json['id'] as num).toInt(),
  name: json['name'] as String,
  nick: json['nick'] as String,
  language: json['language'] as String,
  isSuperuser: json['is_superuser'] as bool,
  profile: SnAccountProfile.fromJson(json['profile'] as Map<String, dynamic>),
  createdAt: DateTime.parse(json['created_at'] as String),
  updatedAt: DateTime.parse(json['updated_at'] as String),
  deletedAt:
      json['deleted_at'] == null
          ? null
          : DateTime.parse(json['deleted_at'] as String),
);

Map<String, dynamic> _$SnAccountToJson(_SnAccount instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'nick': instance.nick,
      'language': instance.language,
      'is_superuser': instance.isSuperuser,
      'profile': instance.profile.toJson(),
      'created_at': instance.createdAt.toIso8601String(),
      'updated_at': instance.updatedAt.toIso8601String(),
      'deleted_at': instance.deletedAt?.toIso8601String(),
    };

_SnAccountProfile _$SnAccountProfileFromJson(Map<String, dynamic> json) =>
    _SnAccountProfile(
      id: (json['id'] as num).toInt(),
      firstName: json['first_name'] as String?,
      middleName: json['middle_name'] as String?,
      lastName: json['last_name'] as String?,
      bio: json['bio'] as String?,
      pictureId: json['picture_id'] as String?,
      picture:
          json['picture'] == null
              ? null
              : SnCloudFile.fromJson(json['picture'] as Map<String, dynamic>),
      backgroundId: json['background_id'] as String?,
      background:
          json['background'] == null
              ? null
              : SnCloudFile.fromJson(
                json['background'] as Map<String, dynamic>,
              ),
      createdAt: DateTime.parse(json['created_at'] as String),
      updatedAt: DateTime.parse(json['updated_at'] as String),
      deletedAt:
          json['deleted_at'] == null
              ? null
              : DateTime.parse(json['deleted_at'] as String),
    );

Map<String, dynamic> _$SnAccountProfileToJson(_SnAccountProfile instance) =>
    <String, dynamic>{
      'id': instance.id,
      'first_name': instance.firstName,
      'middle_name': instance.middleName,
      'last_name': instance.lastName,
      'bio': instance.bio,
      'picture_id': instance.pictureId,
      'picture': instance.picture?.toJson(),
      'background_id': instance.backgroundId,
      'background': instance.background?.toJson(),
      'created_at': instance.createdAt.toIso8601String(),
      'updated_at': instance.updatedAt.toIso8601String(),
      'deleted_at': instance.deletedAt?.toIso8601String(),
    };

_SnAccountStatus _$SnAccountStatusFromJson(Map<String, dynamic> json) =>
    _SnAccountStatus(
      id: json['id'] as String,
      attitude: (json['attitude'] as num).toInt(),
      isOnline: json['is_online'] as bool,
      isInvisible: json['is_invisible'] as bool,
      isNotDisturb: json['is_not_disturb'] as bool,
      isCustomized: json['is_customized'] as bool,
      label: json['label'] as String? ?? "",
      clearedAt:
          json['cleared_at'] == null
              ? null
              : DateTime.parse(json['cleared_at'] as String),
      accountId: (json['account_id'] as num).toInt(),
      createdAt: DateTime.parse(json['created_at'] as String),
      updatedAt: DateTime.parse(json['updated_at'] as String),
      deletedAt:
          json['deleted_at'] == null
              ? null
              : DateTime.parse(json['deleted_at'] as String),
    );

Map<String, dynamic> _$SnAccountStatusToJson(_SnAccountStatus instance) =>
    <String, dynamic>{
      'id': instance.id,
      'attitude': instance.attitude,
      'is_online': instance.isOnline,
      'is_invisible': instance.isInvisible,
      'is_not_disturb': instance.isNotDisturb,
      'is_customized': instance.isCustomized,
      'label': instance.label,
      'cleared_at': instance.clearedAt?.toIso8601String(),
      'account_id': instance.accountId,
      'created_at': instance.createdAt.toIso8601String(),
      'updated_at': instance.updatedAt.toIso8601String(),
      'deleted_at': instance.deletedAt?.toIso8601String(),
    };
