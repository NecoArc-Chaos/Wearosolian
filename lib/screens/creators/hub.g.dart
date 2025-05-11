// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hub.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$publisherStatsHash() => r'315705881d116b2aeac93f94f5ee2bc816d9f0f6';

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

/// See also [publisherStats].
@ProviderFor(publisherStats)
const publisherStatsProvider = PublisherStatsFamily();

/// See also [publisherStats].
class PublisherStatsFamily extends Family<AsyncValue<SnPublisherStats?>> {
  /// See also [publisherStats].
  const PublisherStatsFamily();

  /// See also [publisherStats].
  PublisherStatsProvider call(String? uname) {
    return PublisherStatsProvider(uname);
  }

  @override
  PublisherStatsProvider getProviderOverride(
    covariant PublisherStatsProvider provider,
  ) {
    return call(provider.uname);
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'publisherStatsProvider';
}

/// See also [publisherStats].
class PublisherStatsProvider
    extends AutoDisposeFutureProvider<SnPublisherStats?> {
  /// See also [publisherStats].
  PublisherStatsProvider(String? uname)
    : this._internal(
        (ref) => publisherStats(ref as PublisherStatsRef, uname),
        from: publisherStatsProvider,
        name: r'publisherStatsProvider',
        debugGetCreateSourceHash:
            const bool.fromEnvironment('dart.vm.product')
                ? null
                : _$publisherStatsHash,
        dependencies: PublisherStatsFamily._dependencies,
        allTransitiveDependencies:
            PublisherStatsFamily._allTransitiveDependencies,
        uname: uname,
      );

  PublisherStatsProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.uname,
  }) : super.internal();

  final String? uname;

  @override
  Override overrideWith(
    FutureOr<SnPublisherStats?> Function(PublisherStatsRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: PublisherStatsProvider._internal(
        (ref) => create(ref as PublisherStatsRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        uname: uname,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<SnPublisherStats?> createElement() {
    return _PublisherStatsProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is PublisherStatsProvider && other.uname == uname;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, uname.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin PublisherStatsRef on AutoDisposeFutureProviderRef<SnPublisherStats?> {
  /// The parameter `uname` of this provider.
  String? get uname;
}

class _PublisherStatsProviderElement
    extends AutoDisposeFutureProviderElement<SnPublisherStats?>
    with PublisherStatsRef {
  _PublisherStatsProviderElement(super.provider);

  @override
  String? get uname => (origin as PublisherStatsProvider).uname;
}

// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
