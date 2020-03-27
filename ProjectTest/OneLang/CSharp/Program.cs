using System;
using System.IO;
using System.Threading.Tasks;
using Generator;
using One;

namespace CSharp
{
    class Program
    {
        static async Task Main(string[] args)
        {
            Console.WriteLine("START");

            var compiler = new Compiler();
            await compiler.init("packages/");
            compiler.setupNativeResolver(File.ReadAllText("langs/NativeResolvers/typescript.ts"));
            compiler.newWorkspace();
            compiler.addOverlayPackage("js-yaml");

            var projDir = "dir";
            foreach (var file in Directory.GetFiles(projDir, "*.ts", SearchOption.AllDirectories))
                compiler.addProjectFile(file, File.ReadAllText($"{projDir}/${file}"));

            compiler.processWorkspace();
            var genCsharp = CsharpGenerator.generate(compiler.projectPkg);

            Console.WriteLine("DONE");
        }
    }
}
