public interface IResolvedImportable extends IImportable {
    SourceFile getParentFile();
    void setParentFile(SourceFile value);
}