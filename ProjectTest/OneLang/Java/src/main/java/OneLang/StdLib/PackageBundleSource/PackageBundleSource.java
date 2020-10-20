public class PackageBundleSource implements PackageSource {
    public PackageBundle bundle;
    
    public PackageBundleSource(PackageBundle bundle)
    {
        this.bundle = bundle;
    }
    
    public PackageBundle getPackageBundle(PackageId[] ids, Boolean cachedOnly) {
        throw new Error("Method not implemented.");
    }
    
    public PackageBundle getAllCached() {
        return this.bundle;
    }
}