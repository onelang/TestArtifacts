from OneLangStdLib import *
import re
import OneLang.index as index
import OneLang.Generator.IGenerator as iGen
import OneLang.One.Compiler as comp

class SelfTestRunner:
    def __init__(self, base_dir):
        self.base_dir = base_dir
    
    def run_test(self, generator):
        console.log("[-] SelfTestRunner :: START")
        compiler = comp.Compiler()
        compiler.init(f'''{self.base_dir}packages/''')
        compiler.setup_native_resolver(index.OneFile.read_text(f'''{self.base_dir}langs/NativeResolvers/typescript.ts'''))
        compiler.new_workspace()
        
        proj_dir = f'''{self.base_dir}src/'''
        for file in list(filter(lambda x: x.endswith(".ts"), index.OneFile.list_files(proj_dir, True))):
            compiler.add_project_file(file, index.OneFile.read_text(f'''{proj_dir}/{file}'''))
        
        compiler.process_workspace()
        generated = generator.generate(compiler.project_pkg)
        
        lang_name = generator.get_lang_name()
        ext = f'''.{generator.get_extension()}'''
        
        all_match = True
        for gen_file in generated:
            ts_gen_path = f'''{self.base_dir}test/artifacts/ProjectTest/OneLang/{lang_name}/{re.sub("\\.ts$", ext, gen_file.path)}'''
            re_gen_path = f'''{self.base_dir}test/artifacts/ProjectTest/OneLang/{lang_name}_Regen_{lang_name}/{re.sub("\\.ts$", ext, gen_file.path)}'''
            ts_gen_content = index.OneFile.read_text(ts_gen_path)
            re_gen_content = gen_file.content
            
            index.OneFile.write_text(re_gen_path, gen_file.content)
            
            if ts_gen_content != re_gen_content:
                console.error(f'''Content does not match: {gen_file.path}''')
                all_match = False
        
        console.log("[+} SUCCESS! All generated files are the same" if all_match else "[!] FAIL! Not all files are the same")
        console.log("[-] SelfTestRunner :: DONE")