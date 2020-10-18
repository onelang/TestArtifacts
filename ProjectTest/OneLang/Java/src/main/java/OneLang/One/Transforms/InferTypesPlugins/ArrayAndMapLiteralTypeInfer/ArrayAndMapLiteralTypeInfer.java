import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Arrays;

public class ArrayAndMapLiteralTypeInfer extends InferTypesPlugin {
    public ArrayAndMapLiteralTypeInfer()
    {
        super("ArrayAndMapLiteralTypeInfer");
        
    }
    
    protected IType inferArrayOrMapItemType(Expression[] items, IType expectedType, Boolean isMap)
    {
        var itemTypes = new ArrayList<IType>();
        for (var item : items) {
            if (!itemTypes.stream().anyMatch(t -> TypeHelper.equals(t, item.getType())))
                itemTypes.add(item.getType());
        }
        
        var literalType = isMap ? this.main.currentFile.literalTypes.map : this.main.currentFile.literalTypes.array;
        
        IType itemType = null;
        if (itemTypes.size() == 0) {
            if (expectedType == null) {
                this.errorMan.warn("Could not determine the type of an empty " + (isMap ? "MapLiteral" : "ArrayLiteral") + ", using AnyType instead");
                itemType = AnyType.instance;
            }
            else if (expectedType instanceof ClassType && ((ClassType)expectedType).decl == literalType.decl)
                itemType = ((ClassType)expectedType).getTypeArguments()[0];
            else
                itemType = AnyType.instance;
        }
        else if (itemTypes.size() == 1)
            itemType = itemTypes.get(0);
        else if (!(expectedType instanceof AnyType)) {
            this.errorMan.warn("Could not determine the type of " + (isMap ? "a MapLiteral" : "an ArrayLiteral") + "! Multiple types were found: " + Arrays.stream(itemTypes.stream().map(x -> x.repr()).toArray(String[]::new)).collect(Collectors.joining(", ")) + ", using AnyType instead");
            itemType = AnyType.instance;
        }
        return itemType;
    }
    
    public Boolean canDetectType(Expression expr)
    {
        return expr instanceof ArrayLiteral || expr instanceof MapLiteral;
    }
    
    public Boolean detectType(Expression expr)
    {
        // make this work: `<{ [name: string]: SomeObject }> {}`
        if (expr.parentNode instanceof CastExpression)
            expr.setExpectedType(((CastExpression)expr.parentNode).newType);
        else if (expr.parentNode instanceof BinaryExpression && ((BinaryExpression)expr.parentNode).operator == "=" && ((BinaryExpression)expr.parentNode).right == expr)
            expr.setExpectedType(((BinaryExpression)expr.parentNode).left.actualType);
        else if (expr.parentNode instanceof ConditionalExpression && (((ConditionalExpression)expr.parentNode).whenTrue == expr || ((ConditionalExpression)expr.parentNode).whenFalse == expr))
            expr.setExpectedType(((ConditionalExpression)expr.parentNode).whenTrue == expr ? ((ConditionalExpression)expr.parentNode).whenFalse.actualType : ((ConditionalExpression)expr.parentNode).whenTrue.actualType);
        
        if (expr instanceof ArrayLiteral) {
            var itemType = this.inferArrayOrMapItemType(((ArrayLiteral)expr).items, ((ArrayLiteral)expr).expectedType, false);
            ((ArrayLiteral)expr).setActualType(new ClassType(this.main.currentFile.literalTypes.array.decl, new IType[] { itemType }));
        }
        else if (expr instanceof MapLiteral) {
            var itemType = this.inferArrayOrMapItemType(Arrays.stream(((MapLiteral)expr).items).map(x -> x.value).toArray(Expression[]::new), ((MapLiteral)expr).expectedType, true);
            ((MapLiteral)expr).setActualType(new ClassType(this.main.currentFile.literalTypes.map.decl, new IType[] { itemType }));
        }
        
        return true;
    }
}