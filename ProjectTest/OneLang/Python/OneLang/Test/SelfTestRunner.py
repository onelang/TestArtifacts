from OneLangStdLib import *
from OneFile import *
import OneLang.Generator.IGenerator as iGen
import OneLang.One.Compiler as comp
import OneLang.Test.PackageStateCapture as packStateCapt
import re

class CompilerHooks:
    def __init__(self, compiler, base_dir):
        self.stage = 0
        self.compiler = compiler
        self.base_dir = base_dir
    
    def after_stage(self, stage_name):
        state = packStateCapt.PackageStateCapture(self.compiler.project_pkg)
        stage_fn = f'''{self.base_dir}/test/artifacts/ProjectTest/OneLang/stages/{self.stage}_{stage_name}.txt'''
        self.stage = self.stage + 1
        stage_summary = state.get_summary()
        expected = OneFile.read_text(stage_fn)
        if stage_summary != expected:
            OneFile.write_text(stage_fn + "_diff.txt", stage_summary)
            raise Error(f'''Stage result differs from expected: {stage_name} -> {stage_fn}''')
        else:
            console.log(f'''[+] Stage passed: {stage_name}''')

class SelfTestRunner:
    def __init__(self, base_dir):
        self.base_dir = base_dir
    
    def run_test(self, generator):
        console.log("[-] SelfTestRunner :: START")
        compiler = comp.Compiler()
        compiler.init(f'''{self.base_dir}packages/''')
        compiler.setup_native_resolver(OneFile.read_text(f'''{self.base_dir}langs/NativeResolvers/typescript.ts'''))
        compiler.new_workspace("OneLang")
        
        proj_dir = f'''{self.base_dir}src/'''
        for file in list(filter(lambda x: x.endswith(".ts"), OneFile.list_files(proj_dir, True))):
            compiler.add_project_file(file, OneFile.read_text(f'''{proj_dir}/{file}'''))
        
        compiler.hooks = CompilerHooks(compiler, self.base_dir)
        
        compiler.process_workspace()
        generated = generator.generate(compiler.project_pkg)
        
        lang_name = generator.get_lang_name()
        ext = f'''.{generator.get_extension()}'''
        
        all_match = True
        for gen_file in generated:
            fn = re.sub("\\.ts$", ext, gen_file.path)
            proj_base = f'''{self.base_dir}test/artifacts/ProjectTest/OneLang'''
            ts_gen_path = f'''{proj_base}/{lang_name}/{fn}'''
            re_gen_path = f'''{proj_base}/{lang_name}_Regen/{fn}'''
            ts_gen_content = OneFile.read_text(ts_gen_path)
            re_gen_content = gen_file.content
            
            if ts_gen_content != re_gen_content:
                OneFile.write_text(re_gen_path, gen_file.content)
                console.error(f'''Content does not match: {gen_file.path}''')
                all_match = False
            else:
                console.log(f'''[+] Content matches: {gen_file.path}''')
        
        console.log("[+} SUCCESS! All generated files are the same" if all_match else "[!] FAIL! Not all files are the same")
        console.log("[-] SelfTestRunner :: DONE")
        return all_match