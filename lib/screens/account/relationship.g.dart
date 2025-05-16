// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'relationship.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$sendFriendRequestHash() => r'0fc0a3866b64df8b547f831fdb7db47929e2c9ff';

/// See also [sendFriendRequest].
@ProviderFor(sendFriendRequest)
final sendFriendRequestProvider =
    AutoDisposeFutureProvider<List<SnRelationship>>.internal(
      sendFriendRequest,
      name: r'sendFriendRequestProvider',
      debugGetCreateSourceHash:
          const bool.fromEnvironment('dart.vm.product')
              ? null
              : _$sendFriendRequestHash,
      dependencies: null,
      allTransitiveDependencies: null,
    );

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef SendFriendRequestRef =
    AutoDisposeFutureProviderRef<List<SnRelationship>>;
String _$relationshipListNotifierHash() =>
    r'ad352e8b10641820d5acac27b26ad1bb0b59b67f';

/// See also [RelationshipListNotifier].
@ProviderFor(RelationshipListNotifier)
final relationshipListNotifierProvider = AutoDisposeAsyncNotifierProvider<
  RelationshipListNotifier,
  CursorPagingData<SnRelationship>
>.internal(
  RelationshipListNotifier.new,
  name: r'relationshipListNotifierProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product')
          ? null
          : _$relationshipListNotifierHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$RelationshipListNotifier =
    AutoDisposeAsyncNotifier<CursorPagingData<SnRelationship>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
