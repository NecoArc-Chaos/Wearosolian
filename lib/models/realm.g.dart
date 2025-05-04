// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'realm.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_SnRealm _$SnRealmFromJson(Map<String, dynamic> json) => _SnRealm(
  id: (json['id'] as num).toInt(),
  slug: json['slug'] as String,
  name: json['name'] as String,
  description: json['description'] as String,
  verifiedAs: json['verified_as'] as String?,
  verifiedAt:
      json['verified_at'] == null
          ? null
          : DateTime.parse(json['verified_at'] as String),
  isCommunity: json['is_community'] as bool,
  isPublic: json['is_public'] as bool,
  pictureId: json['picture_id'] as String?,
  picture:
      json['picture'] == null
          ? null
          : SnCloudFile.fromJson(json['picture'] as Map<String, dynamic>),
  backgroundId: json['background_id'] as String?,
  background:
      json['background'] == null
          ? null
          : SnCloudFile.fromJson(json['background'] as Map<String, dynamic>),
  accountId: (json['account_id'] as num).toInt(),
  createdAt: DateTime.parse(json['created_at'] as String),
  updatedAt: DateTime.parse(json['updated_at'] as String),
  deletedAt:
      json['deleted_at'] == null
          ? null
          : DateTime.parse(json['deleted_at'] as String),
);

Map<String, dynamic> _$SnRealmToJson(_SnRealm instance) => <String, dynamic>{
  'id': instance.id,
  'slug': instance.slug,
  'name': instance.name,
  'description': instance.description,
  'verified_as': instance.verifiedAs,
  'verified_at': instance.verifiedAt?.toIso8601String(),
  'is_community': instance.isCommunity,
  'is_public': instance.isPublic,
  'picture_id': instance.pictureId,
  'picture': instance.picture?.toJson(),
  'background_id': instance.backgroundId,
  'background': instance.background?.toJson(),
  'account_id': instance.accountId,
  'created_at': instance.createdAt.toIso8601String(),
  'updated_at': instance.updatedAt.toIso8601String(),
  'deleted_at': instance.deletedAt?.toIso8601String(),
};

_SnRealmMember _$SnRealmMemberFromJson(Map<String, dynamic> json) =>
    _SnRealmMember(
      realmId: (json['realm_id'] as num).toInt(),
      realm:
          json['realm'] == null
              ? null
              : SnRealm.fromJson(json['realm'] as Map<String, dynamic>),
      accountId: (json['account_id'] as num).toInt(),
      account:
          json['account'] == null
              ? null
              : SnAccount.fromJson(json['account'] as Map<String, dynamic>),
      role: (json['role'] as num).toInt(),
      joinedAt:
          json['joined_at'] == null
              ? null
              : DateTime.parse(json['joined_at'] as String),
      createdAt: DateTime.parse(json['created_at'] as String),
      updatedAt: DateTime.parse(json['updated_at'] as String),
      deletedAt:
          json['deleted_at'] == null
              ? null
              : DateTime.parse(json['deleted_at'] as String),
    );

Map<String, dynamic> _$SnRealmMemberToJson(_SnRealmMember instance) =>
    <String, dynamic>{
      'realm_id': instance.realmId,
      'realm': instance.realm?.toJson(),
      'account_id': instance.accountId,
      'account': instance.account?.toJson(),
      'role': instance.role,
      'joined_at': instance.joinedAt?.toIso8601String(),
      'created_at': instance.createdAt.toIso8601String(),
      'updated_at': instance.updatedAt.toIso8601String(),
      'deleted_at': instance.deletedAt?.toIso8601String(),
    };
