from OneLang.One.Compiler import Compiler
from OneLang.Generator.CsharpGenerator import CsharpGenerator
import glob

baseDir = "../../../../../"

compiler = Compiler()
compiler.init(f"{baseDir}packages/")
with open(f"{baseDir}langs/NativeResolvers/typescript.ts", "rt") as f: nativeRes = f.read()
compiler.setup_native_resolver(nativeRes)
compiler.new_workspace()
compiler.add_overlay_package("js-yaml")

projDir = f"{baseDir}src/"

for file in sorted(glob.glob(f"{projDir}/**/*.ts", recursive=True)):
    with open(file, "rt") as f: content = f.read()
    compiler.add_project_file(file.replace(projDir, ""), content)

compiler.process_workspace()
genCsharp = CsharpGenerator().generate(compiler.project_pkg)
print(genCsharp)
