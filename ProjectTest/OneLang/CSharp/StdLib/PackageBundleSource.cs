using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using StdLib;

namespace StdLib
{
    public class PackageBundleSource : PackageSource {
        public PackageBundle bundle;
        
        public PackageBundleSource(PackageBundle bundle)
        {
            this.bundle = bundle;
        }
        
        public Task<PackageBundle> getPackageBundle(PackageId[] ids, bool cachedOnly) {
            throw new Error("Method not implemented.");
        }
        
        public async Task<PackageBundle> getAllCached() {
            return await Promise.resolve(this.bundle);
        }
    }
}