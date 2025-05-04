// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'detail.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$realmIdentityHash() => r'eac6e829b5b46bcfadbf201ab6f918d78c894b9f';

/// Copied from Dart SDK
class _SystemHash {
  _SystemHash._();

  static int combine(int hash, int value) {
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + value);
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + ((0x0007ffff & hash) << 10));
    return hash ^ (hash >> 6);
  }

  static int finish(int hash) {
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + ((0x03ffffff & hash) << 3));
    // ignore: parameter_assignments
    hash = hash ^ (hash >> 11);
    return 0x1fffffff & (hash + ((0x00003fff & hash) << 15));
  }
}

/// See also [realmIdentity].
@ProviderFor(realmIdentity)
const realmIdentityProvider = RealmIdentityFamily();

/// See also [realmIdentity].
class RealmIdentityFamily extends Family<AsyncValue<SnRealmMember?>> {
  /// See also [realmIdentity].
  const RealmIdentityFamily();

  /// See also [realmIdentity].
  RealmIdentityProvider call(String realmSlug) {
    return RealmIdentityProvider(realmSlug);
  }

  @override
  RealmIdentityProvider getProviderOverride(
    covariant RealmIdentityProvider provider,
  ) {
    return call(provider.realmSlug);
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'realmIdentityProvider';
}

/// See also [realmIdentity].
class RealmIdentityProvider extends AutoDisposeFutureProvider<SnRealmMember?> {
  /// See also [realmIdentity].
  RealmIdentityProvider(String realmSlug)
    : this._internal(
        (ref) => realmIdentity(ref as RealmIdentityRef, realmSlug),
        from: realmIdentityProvider,
        name: r'realmIdentityProvider',
        debugGetCreateSourceHash:
            const bool.fromEnvironment('dart.vm.product')
                ? null
                : _$realmIdentityHash,
        dependencies: RealmIdentityFamily._dependencies,
        allTransitiveDependencies:
            RealmIdentityFamily._allTransitiveDependencies,
        realmSlug: realmSlug,
      );

  RealmIdentityProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.realmSlug,
  }) : super.internal();

  final String realmSlug;

  @override
  Override overrideWith(
    FutureOr<SnRealmMember?> Function(RealmIdentityRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: RealmIdentityProvider._internal(
        (ref) => create(ref as RealmIdentityRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        realmSlug: realmSlug,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<SnRealmMember?> createElement() {
    return _RealmIdentityProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is RealmIdentityProvider && other.realmSlug == realmSlug;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, realmSlug.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin RealmIdentityRef on AutoDisposeFutureProviderRef<SnRealmMember?> {
  /// The parameter `realmSlug` of this provider.
  String get realmSlug;
}

class _RealmIdentityProviderElement
    extends AutoDisposeFutureProviderElement<SnRealmMember?>
    with RealmIdentityRef {
  _RealmIdentityProviderElement(super.provider);

  @override
  String get realmSlug => (origin as RealmIdentityProvider).realmSlug;
}

// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
