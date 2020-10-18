public interface PackageSource {
    PackageBundle getPackageBundle(PackageId[] ids, Boolean cachedOnly);
    PackageBundle getAllCached();
}