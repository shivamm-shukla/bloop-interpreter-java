package bloop.ast;

public class BinaryOpNode implements Expression{

    private final Expression left;
    private final String  operator;
    private final Expression right;

    public BinaryOpNode(Expression left, String operator, Expression right)
    {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Object evaluate(Object env) {
       Object leftValue = left.evaluate(env);
       Object rightValue = right.evaluate(env);

       double l = (double) leftValue;
       double r = (double) rightValue;
       switch(operator) {
           case "+":
               return l + r;
           case "-":
               return l - r;
           case "*":
               return l * r;
           case  "/":
               return l / r;
           case ">":
               return l > r;
           case  "<":
               return l < r;
           case "==":
               if (leftValue instanceof Double){
                   return l == r;
               }
               return leftValue.equals(rightValue);
       }
       throw new RuntimeException("Unknown operator: " + operator);
    }

}
