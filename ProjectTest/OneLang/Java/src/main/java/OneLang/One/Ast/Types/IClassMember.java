public interface IClassMember {
    Visibility getVisibility();
    void setVisibility(Visibility value);
    
    Boolean getIsStatic();
    void setIsStatic(Boolean value);
}