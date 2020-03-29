﻿using System;
using System.IO;
using System.Threading.Tasks;
using Generator;
using One;
using One.Ast;
using One.Transforms;
using Parsers;
using Utils;

namespace CSharp
{
    class Program
    {
        public static void processWorkspace(Workspace workspace, Package projectPkg) {
            new FillParent().visitPackage(projectPkg);
            FillAttributesFromTrivia.processPackage(projectPkg);
            ResolveImports.processWorkspace(workspace);
            new ResolveGenericTypeIdentifiers().visitPackage(projectPkg);
            new ConvertToMethodCall().visitPackage(projectPkg);
            new ResolveUnresolvedTypes().visitPackage(projectPkg);
            new ResolveIdentifiers().visitPackage(projectPkg);
            new InstanceOfImplicitCast().visitPackage(projectPkg);
            new DetectMethodCalls().visitPackage(projectPkg);
            //foreach (var file in projectPkg.files.Values)
            //    Console.WriteLine(TSOverviewGenerator.generate(file));
            new InferTypes().visitPackage(projectPkg);
            new CollectInheritanceInfo().visitPackage(projectPkg);
        }


        static async Task Main(string[] args)
        {
            Console.WriteLine("START");
            var baseDir = "../../../../../";

            var compiler = new Compiler();
            await compiler.init($"{baseDir}packages/");
            compiler.setupNativeResolver(File.ReadAllText($"{baseDir}langs/NativeResolvers/typescript.ts"));
            compiler.newWorkspace();
            compiler.addOverlayPackage("js-yaml");

            var projDir = $"{baseDir}src/";
            foreach (var file in Directory.GetFiles(projDir, "*.ts", SearchOption.AllDirectories))
                compiler.addProjectFile(file.Replace(projDir, ""), File.ReadAllText(file));

            //processWorkspace(compiler.workspace, compiler.projectPkg);
            compiler.processWorkspace();
            var genCsharp = new CsharpGenerator().generate(compiler.projectPkg);

            var allMatch = true;
            foreach (var genFile in genCsharp) {
                var tsGenPath = $"{baseDir}test/artifacts/ProjectTest/OneLang/CSharp/{genFile.path.replace(".ts", ".cs")}";
                var csGenPath = $"{baseDir}test/artifacts/ProjectTest/OneLang/CSharp_Regen_CSharp/{genFile.path.replace(".ts", ".cs")}";
                var tsGenContent = File.ReadAllText(tsGenPath);
                var csGenContent = genFile.content;

                new DirectoryInfo(System.IO.Path.GetDirectoryName(csGenPath)).Create();
                File.WriteAllText(csGenPath, genFile.content);
                
                if (tsGenContent != csGenContent) {
                    console.error($"Content does not match: {genFile.path}");
                    allMatch = false;
                }
            }

            Console.WriteLine(allMatch ? "[+} SUCCESS! All generated files are the same" : "[!] FAIL! Not all files are the same");

            Console.WriteLine("DONE");
        }
    }
}
