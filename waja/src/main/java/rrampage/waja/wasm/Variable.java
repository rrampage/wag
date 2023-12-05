package rrampage.waja.wasm;

import rrampage.waja.wasm.data.DataType;

import static rrampage.waja.utils.ConversionUtils.*;

public sealed interface Variable {
    void setVal(long val);
    static Variable newVariable(DataType dataType, long val) {
        return switch (dataType) {
            case I32 -> new I32Variable(longToInt(val));
            case I64 -> new I64Variable(val);
            case F32 -> new F32Variable(longToFloat(val));
            case F64 -> new F64Variable(longToDouble(val));
        };
    }

    static String debug(Variable variable) {
        return switch (variable) {
            case F32Variable v -> v.getType().toString() + " " + v.getVal();
            case F64Variable v -> v.getType().toString() + " " + v.getVal();
            case I32Variable v -> v.getType().toString() + " " + v.getVal();
            case I64Variable v -> v.getType().toString() + " " + v.getVal();
        };
    }
}

final class I32Variable implements Variable {
    private int val;
    I32Variable(int val) {
        this.val = val;
    }
    public DataType getType() {return DataType.I32;}
    public int getVal() { return val;}
    public void setVal(long val) { this.val = longToInt(val);}
}
final class I64Variable implements Variable {
    private long val;
    I64Variable(long val) {
        this.val = val;
    }
    public long getVal() { return val;}
    public void setVal(long val) { this.val = val;}
    public DataType getType() {return DataType.I64;}
}
final class F32Variable implements Variable {
    private float val;
    F32Variable(float val) {
        this.val = val;
    }
    public float getVal() { return val;}
    public void setVal(long val) { this.val = longToFloat(val);}
    public DataType getType() {return DataType.F32;}
}
final class F64Variable implements Variable {
    private double val;
    F64Variable(double val) {
        this.val = val;
    }
    public double getVal() { return val;}
    public void setVal(long val) { this.val = longToDouble(val);}
    public DataType getType() {return DataType.F64;}
}
